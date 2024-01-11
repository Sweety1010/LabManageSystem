package com.lab.labmanagesystem.service.impl;

import com.arcsoft.face.*;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.DetectOrient;
import com.arcsoft.face.enums.ImageFormat;
import com.arcsoft.face.toolkit.ImageInfo;
import com.google.common.collect.Lists;
import com.lab.labmanagesystem.config.ArcFaceAutoConfiguration;
import com.lab.labmanagesystem.constant.MessageConstant;
import com.lab.labmanagesystem.entity.*;
import com.lab.labmanagesystem.exception.FaceException;
import com.lab.labmanagesystem.factory.FaceEngineFactory;
import com.lab.labmanagesystem.service.FaceEngineService;
import com.lab.labmanagesystem.utils.SweetyUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.CvPoint;
import org.bytedeco.opencv.opencv_core.CvScalar;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.bytedeco.opencv.global.opencv_core.cvPoint;
import static org.bytedeco.opencv.global.opencv_core.cvScalar;

@Slf4j
@Service
public class FaceEngineServiceImpl implements FaceEngineService {

    @Value("${config.arcface-sdk.app-id}")
    public String appId;

    @Value("${config.arcface-sdk.sdk-key}")
    public String sdkKey;

    @Value("${config.arcface-sdk.detect-pool-size}")
    public Integer detectPooSize;

    @Value("${config.arcface-sdk.compare-pool-size}")
    public Integer comparePooSize;

    private ExecutorService compareExecutorService;

    //通用人脸识别引擎池
    private GenericObjectPool<FaceEngine> faceEngineGeneralPool;

    //人脸比对引擎池
    private GenericObjectPool<FaceEngine> faceEngineComparePool;

    // VIDEO模式人脸检测引擎，用于预览帧人脸追踪
    private FaceEngine ftEngine;

    private static CvScalar color = cvScalar(0, 0, 255, 0);

    @PostConstruct
    public void init() {
        GenericObjectPoolConfig detectPoolConfig = new GenericObjectPoolConfig();
        detectPoolConfig.setMaxIdle(detectPooSize);
        detectPoolConfig.setMaxTotal(detectPooSize);
        detectPoolConfig.setMinIdle(detectPooSize);
        detectPoolConfig.setLifo(false);
        EngineConfiguration detectCfg = new EngineConfiguration();
        FunctionConfiguration detectFunctionCfg = new FunctionConfiguration();
        detectFunctionCfg.setSupportFaceDetect(true);//开启人脸检测功能
        detectFunctionCfg.setSupportFaceRecognition(true);//开启人脸识别功能
        detectFunctionCfg.setSupportAge(true);//开启年龄检测功能
        detectFunctionCfg.setSupportGender(true);//开启性别检测功能
        detectFunctionCfg.setSupportLiveness(true);//开启活体检测功能
        detectCfg.setFunctionConfiguration(detectFunctionCfg);
        detectCfg.setDetectMode(DetectMode.ASF_DETECT_MODE_IMAGE);//图片检测模式，如果是连续帧的视频流图片，那么改成VIDEO模式
        detectCfg.setDetectFaceOrientPriority(DetectOrient.ASF_OP_0_ONLY);//人脸旋转角度
        faceEngineGeneralPool = new GenericObjectPool(new FaceEngineFactory(appId, sdkKey, null,detectCfg), detectPoolConfig);//底层库算法对象池

        //初始化特征比较线程池
        GenericObjectPoolConfig comparePoolConfig = new GenericObjectPoolConfig();
        comparePoolConfig.setMaxIdle(comparePooSize);
        comparePoolConfig.setMaxTotal(comparePooSize);
        comparePoolConfig.setMinIdle(comparePooSize);
        comparePoolConfig.setLifo(false);
        EngineConfiguration compareCfg = new EngineConfiguration();
        FunctionConfiguration compareFunctionCfg = new FunctionConfiguration();
        compareFunctionCfg.setSupportFaceRecognition(true);//开启人脸识别功能
        compareCfg.setFunctionConfiguration(compareFunctionCfg);
        compareCfg.setDetectMode(DetectMode.ASF_DETECT_MODE_IMAGE);//图片检测模式，如果是连续帧的视频流图片，那么改成VIDEO模式
        compareCfg.setDetectFaceOrientPriority(DetectOrient.ASF_OP_0_ONLY);//人脸旋转角度
        faceEngineComparePool = new GenericObjectPool(new FaceEngineFactory(appId, sdkKey, null,compareCfg), comparePoolConfig);//底层库算法对象池
        compareExecutorService = Executors.newFixedThreadPool(comparePooSize);

        //引擎配置
        ftEngine = new FaceEngine(ArcFaceAutoConfiguration.CACHE_LIB_FOLDER);
        int activeCode = ftEngine.activeOnline(appId, sdkKey);
        EngineConfiguration ftEngineCfg = new EngineConfiguration();
        ftEngineCfg.setDetectMode(DetectMode.ASF_DETECT_MODE_VIDEO);
        ftEngineCfg.setFunctionConfiguration(FunctionConfiguration.builder().supportFaceDetect(true).build());
        int ftInitCode = ftEngine.init(ftEngineCfg);
    }

    /**
     * 人脸检测
     * @param imageInfo
     * @return
     */
    public List<FaceInfo> detectFaces(ImageInfo imageInfo) {
        FaceEngine faceEngine = null;
        try{
            faceEngine = faceEngineGeneralPool.borrowObject();
            if(faceEngine == null){
                throw new FaceException(MessageConstant.FACE_ENGINE_NULL);
            }

            // 人脸检测得到人脸列表
            List<FaceInfo> faceInfoList = new ArrayList<>();

            // 人脸检测
            int errorCode = faceEngine.detectFaces(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfoList);

            // 人脸检测成功
            if(errorCode == 0){
                return faceInfoList;
            }
            // 人脸检测失败
            else{
                throw new FaceException(MessageConstant.FACE_DETECT_FAIL + errorCode);
            }

        } catch (Exception e) {
            log.error("", e);
        } finally {
            if (faceEngine != null) {
                //释放引擎对象
                faceEngineGeneralPool.returnObject(faceEngine);
            }
        }

        return null;
    }

    /**
     * 处理人脸检测结果
     * @param faceInfoList
     * @return
     */
    public List<ProcessInfo> process(ImageInfo imageInfo, List<FaceInfo> faceInfoList) {
        FaceEngine faceEngine = null;
        try {
            //获取引擎对象
            faceEngine = faceEngineGeneralPool.borrowObject();
            if (faceEngine == null) {
                throw new FaceException(MessageConstant.FACE_ENGINE_NULL);
            }

            int errorCode = faceEngine.process(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfoList, FunctionConfiguration.builder().supportAge(true).supportGender(true).supportLiveness(true).build());
            if(errorCode == 0){
                // 处理人脸信息
                List<ProcessInfo> processInfoList = Lists.newLinkedList();

                //性别列表
                List<GenderInfo> genderInfoList = new ArrayList<GenderInfo>();
                faceEngine.getGender(genderInfoList);

                //年龄列表
                List<AgeInfo> ageInfoList = new ArrayList<AgeInfo>();
                faceEngine.getAge(ageInfoList);
                //活体结果列表
                List<LivenessInfo> livenessInfoList = new ArrayList<LivenessInfo>();
                faceEngine.getLiveness(livenessInfoList);

                for (int i = 0; i < genderInfoList.size(); i++) {
                    ProcessInfo processInfo = new ProcessInfo();
                    processInfo.setGender(genderInfoList.get(i).getGender());
                    processInfo.setAge(ageInfoList.get(i).getAge());
                    processInfo.setLiveness(livenessInfoList.get(i).getLiveness());
                    processInfoList.add(processInfo);
                }
                return processInfoList;
            }

        } catch (Exception e) {
            log.error("", e);
        } finally {
            if (faceEngine != null) {
                //释放引擎对象
                faceEngineGeneralPool.returnObject(faceEngine);
            }
        }

        return null;
    }

    /**
     * 提取人脸特征
     * @param imageInfo
     * @param faceInfo
     * @return
     */
    public byte[] extractFaceFeature(ImageInfo imageInfo, FaceInfo faceInfo) {
        FaceEngine faceEngine = null;

        try {
            faceEngine = faceEngineGeneralPool.borrowObject();
            if(faceEngine == null){
                throw new FaceException(MessageConstant.FACE_ENGINE_NULL);
            }

            FaceFeature faceFeature = new FaceFeature();

            // 提取人脸特征
            int errorCode = faceEngine.extractFaceFeature(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfo, faceFeature);
            if(errorCode == 0){
                return faceFeature.getFeatureData();
            }
            else{
                log.error(MessageConstant.FACE_FEATURE_FAIL + errorCode);
            }

        } catch (Exception e){
            log.error("", e);
        } finally {
            if(faceEngine != null){
                //释放引擎对象
                faceEngineGeneralPool.returnObject(faceEngine);
            }
        }

        return null;
    }

    /**
     * 视频流识别
     * @param iplImage
     */
    public void preview(IplImage iplImage){

    }

    /**
     * 人脸特征匹配
     */
    public List<CompareFaceInfo> faceRecognition(byte[] feature, List<StudentFace> studentFaceList, float passRate) {
        FaceEngine faceEngine = null;
        List<CompareFaceInfo> similarList = new ArrayList<>();

        FaceFeature targetFaceFeature = new FaceFeature();
        targetFaceFeature.setFeatureData(feature);

        try{
            faceEngine = faceEngineComparePool.borrowObject();
            for(StudentFace studentFace : studentFaceList){
                FaceFeature sourceFaceFeature = new FaceFeature();

                // 将字符串特征转换为byte[]
                String featureStr = studentFace.getFace();
                byte[] featureByte = SweetyUtil.string2byte(featureStr);
                sourceFaceFeature.setFeatureData(featureByte);

                FaceSimilar faceSimilar = new FaceSimilar();
                faceEngine.compareFaceFeature(targetFaceFeature, sourceFaceFeature, faceSimilar);

                // 筛选相似值大于passRate
                if(faceSimilar.getScore() > passRate){
                    CompareFaceInfo compareFaceInfo = new CompareFaceInfo();
                    compareFaceInfo.setId(studentFace.getStudentId());
                    compareFaceInfo.setName(studentFace.getName());
                    compareFaceInfo.setSimilar(faceSimilar.getScore());

                    similarList.add(compareFaceInfo);
                }

            }
        } catch (Exception e){
            log.error("", e);
        } finally {
            if (faceEngine != null) {
                faceEngineComparePool.returnObject(faceEngine);
            }
        }

        Collections.sort(similarList, (o1, o2) -> {
            return Float.compare(o2.getSimilar(), o1.getSimilar());
        });

        return similarList;
    }
}
