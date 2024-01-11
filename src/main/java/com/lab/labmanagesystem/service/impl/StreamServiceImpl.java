package com.lab.labmanagesystem.service.impl;

import cn.hutool.extra.pinyin.PinyinUtil;
import com.alibaba.fastjson.JSON;
import com.arcsoft.face.*;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.ErrorInfo;
import com.arcsoft.face.enums.ImageFormat;
import com.arcsoft.face.toolkit.ImageInfo;
import com.lab.labmanagesystem.config.ArcFaceAutoConfiguration;
import com.lab.labmanagesystem.constant.RedisKeyConstant;
import com.lab.labmanagesystem.entity.FaceResultInfo;
import com.lab.labmanagesystem.entity.StudentFace;
import com.lab.labmanagesystem.factory.FaceEngineFactory;
import com.lab.labmanagesystem.service.FaceCaptureService;
import com.lab.labmanagesystem.service.FaceEngineService;
import com.lab.labmanagesystem.service.RabbitMQService;
import com.lab.labmanagesystem.service.StreamService;
import com.lab.labmanagesystem.utils.SweetyUtil;
import jakarta.servlet.ServletOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.CvPoint;
import org.bytedeco.opencv.opencv_core.CvScalar;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.bytedeco.opencv.opencv_imgproc.CvFont;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sourceforge.pinyin4j.PinyinHelper;

import static org.bytedeco.ffmpeg.global.avutil.AV_LOG_QUIET;
import static org.bytedeco.opencv.global.opencv_core.cvPoint;
import static org.bytedeco.opencv.global.opencv_core.cvScalar;

@Service
@Slf4j
public class StreamServiceImpl implements StreamService {

    @Value("${config.arcface-sdk.app-id}")
    public String appId;

    @Value("${config.arcface-sdk.sdk-key}")
    public String sdkKey;

    @Value("${config.arcface-sdk.detect-pool-size}")
    public Integer detectPooSize;

    @Value("${config.arcface-sdk.compare-pool-size}")
    public Integer comparePooSize;

    @Autowired
    RabbitMQService rabbitMQService;

    @Autowired
    FaceEngineService faceEngineService;

    @Autowired
    FaceCaptureService faceCaptureService;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * VIDEO模式人脸检测引擎，用于预览帧人脸追踪
     */
    private FaceEngine ftEngine;

    /**
     * 用于人脸识别的引擎池
     */
    private GenericObjectPool<FaceEngine> frEnginePool;

    private ExecutorService frService = Executors.newFixedThreadPool(20);

    private static final CvScalar color = cvScalar(0, 0, 255, 0);

    private static CvFont font;

    private ConcurrentHashMap<String, byte[]> faceFeatureRegistry = new ConcurrentHashMap<>();

    private volatile ConcurrentHashMap<Integer, FaceResultInfo> faceResultRegistry = new ConcurrentHashMap<>();

    /**
     * 视频流处理
     * @param grabber
     * @param servletOutputStream
     */
    public void servletStreamPlayer(FrameGrabber grabber, ServletOutputStream servletOutputStream) throws Exception {

        // 初始化人脸识别视频流
        initEngine();

        // 初始化注册人脸
        initFace();

        // 设置字体
        font = new CvFont();
        opencv_imgproc.cvInitFont(font, opencv_imgproc.CV_FONT_HERSHEY_PLAIN, 4.0, 4.0, 0, 3, 1);

        // 设置全局日志等级为AV_LOG_QUIET 屏蔽FFmpegFrameRecorder输出日志
        avutil.av_log_set_level(AV_LOG_QUIET);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

        // 超时时间(15秒)
        grabber.setOption("stimeout", "15000000");
        grabber.setOption("threads", "1");
        // 设置缓存大小，提高画质、减少卡顿花屏
        grabber.setOption("buffer_size", "1020000");
        // 读写超时，适用于所有协议的通用读写超时
        grabber.setOption("rw_timeout", "15000000");
        // 探测视频流信息，为空默认5000000微秒
        grabber.setOption("probesize", "15000000");
        //设置超时时间
        // 解析视频流信息，为空默认5000000微秒
        grabber.setOption("analyzeduration", "15000000");
        grabber.start();

        // 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制） ?overrun_nonfatal=1&fifo_size=50000000
        //这里udp地址增加参数扩大udp缓存
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, grabber.getImageWidth(), grabber.getImageHeight(), 0);
        // 直播流格式
        // 转码
        recorder.setFormat("flv");
        recorder.setInterleaved(false);
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setVideoOption("crf", "26");
        recorder.setVideoOption("threads", "1");
        double frameRate = grabber.getFrameRate();
        recorder.setFrameRate(frameRate);// 设置帧率
        recorder.setGopSize(25);// 设置gop,关键帧
        int videoBitrate = grabber.getVideoBitrate();
        recorder.setVideoBitrate(videoBitrate);// 设置码率500kb/s，画质
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setTrellis(1);
        recorder.setMaxDelay(0);// 设置延迟
        recorder.setAudioChannels(grabber.getAudioChannels());
        recorder.start();

        long startTime = 0;
        long videoTS = 0;
        long lastTime = startTime;

        for(; ;){
            Frame frame = grabber.grab();

            if(frame == null){
                continue;
            }

            IplImage iplImage = converter.convert(frame);

            if(iplImage != null){
                // 处理图像
                // 处理视频流
                preview(iplImage);
                frame = converter.convert(iplImage);
            }

            if(startTime == 0){
                startTime = System.currentTimeMillis();
            }
            videoTS = 1000 * (System.currentTimeMillis() - startTime);

            // 判断时间偏移
            if (videoTS > recorder.getTimestamp()) {
                recorder.setTimestamp((videoTS));
            }
            recorder.record(frame);

            if (outputStream.size() > 0) {
                byte[] bytes = outputStream.toByteArray();
                servletOutputStream.write(bytes);
                outputStream.reset();
            }

        }
    }

    /**
     * 初始化注册人脸
     */
    private void initFace() {
        // 从redis中读取人脸特征 studentFace
        List<String> studentFaceLists = redisTemplate.opsForList().range(RedisKeyConstant.KEY_STUDENT_FACE, 0, -1);
        for(String studentFaceStr : studentFaceLists){
            StudentFace studentFace = JSON.parseObject(studentFaceStr, StudentFace.class);

            byte[] feature = SweetyUtil.string2byte(studentFace.getFace());

            // 保存至conCurrentHashMap
            faceFeatureRegistry.put(PinyinUtil.getPinyin(studentFace.getName()), feature);
        }
    }

    /**
     * 初始化人脸识别视频流
     */
    private void initEngine() {
        // 引擎配置
        ftEngine = new FaceEngine(ArcFaceAutoConfiguration.CACHE_LIB_FOLDER);
        int activeCode = ftEngine.activeOnline(appId, sdkKey);
        EngineConfiguration ftEngineCfg = new EngineConfiguration();
        ftEngineCfg.setDetectMode(DetectMode.ASF_DETECT_MODE_VIDEO);
        ftEngineCfg.setFunctionConfiguration(FunctionConfiguration.builder().supportFaceDetect(true).build());
        int ftInitCode = ftEngine.init(ftEngineCfg);

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(comparePooSize);
        poolConfig.setMaxTotal(comparePooSize);
        poolConfig.setMinIdle(comparePooSize);
        poolConfig.setLifo(false);
        EngineConfiguration frEngineCfg = new EngineConfiguration();
        frEngineCfg.setFunctionConfiguration(FunctionConfiguration.builder().supportFaceRecognition(true).build());
        frEnginePool = new GenericObjectPool(new FaceEngineFactory( appId, sdkKey, null, frEngineCfg), poolConfig);//底层库算法对象池

        if (!(activeCode == ErrorInfo.MOK.getValue() || activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue())) {
            log.error("activeCode: " + activeCode);
            throw new RuntimeException("activeCode: " + activeCode);
        }
        if (ftInitCode != ErrorInfo.MOK.getValue()) {
            log.error("ftInitEngine: " + ftInitCode);
            throw new RuntimeException("ftInitEngine: " + ftInitCode);
        }
    }

    public void preview(IplImage iplImage){
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setWidth(iplImage.width());
        imageInfo.setHeight(iplImage.height());
        imageInfo.setImageFormat(ImageFormat.CP_PAF_BGR24);
        byte[] imageData = new byte[iplImage.imageSize()];
        iplImage.imageData().get(imageData);
        imageInfo.setImageData(imageData);

        List<FaceInfo> faceInfoList = new ArrayList<>();
        // 人脸检测
        if(ftEngine != null){
            int code = ftEngine.detectFaces(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfoList);
        }

        for(FaceInfo faceInfo : faceInfoList){
            // 绘制人脸框
            int x = faceInfo.getRect().getLeft();
            int y = faceInfo.getRect().getTop();
            int xMax = faceInfo.getRect().getRight();
            int yMax = faceInfo.getRect().getBottom();

            CvPoint pt1 = cvPoint(x, y);
            CvPoint pt2 = cvPoint(xMax, yMax);
            opencv_imgproc.cvRectangle(iplImage, pt1, pt2, color, 3, 4, 0);

            // 进行人脸识别 获取FaceResultInfo
            FaceResultInfo faceResultInfo = faceResultRegistry.get(faceInfo.getFaceId());
            if(faceResultInfo == null){
                faceResultInfo = new FaceResultInfo();
                faceResultRegistry.put(faceInfo.getFaceId(), faceResultInfo);
                frService.submit(new FaceInfoRunnable(faceInfo, imageInfo, faceResultInfo));
            }

            if(faceResultInfo != null){
                try{
                    CvPoint pt3 = cvPoint(x, y - 5);

                    if(faceResultInfo.getName() == null){
                        opencv_imgproc.cvPutText(iplImage, "Unknown", pt3, font, color);
                    }
                    else{
                        opencv_imgproc.cvPutText(iplImage, faceResultInfo.getName(), pt3, font, color);
                    }

                } catch (Exception e){
                    e.printStackTrace();
                }
            }

        }
    }

    private class FaceInfoRunnable implements Runnable{
        private FaceInfo faceInfo;

        private ImageInfo imageInfo;

        private FaceResultInfo faceResultInfo;

        public FaceInfoRunnable(FaceInfo faceInfo, ImageInfo imageInfo, FaceResultInfo faceResultInfo) {
            this.faceInfo = faceInfo;
            this.imageInfo = imageInfo;
            this.faceResultInfo = faceResultInfo;
        }

        @Override
        public void run(){
            FaceEngine frEngine = null;
            try{
                frEngine = frEnginePool.borrowObject();
                if(frEngine != null){
                    FaceFeature faceFeature = new FaceFeature();
                    int resCode = frEngine.extractFaceFeature(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(),
                            imageInfo.getImageFormat(), faceInfo, faceFeature);

                    if(resCode == 0){
                        float similar = 0.0F;

                        Iterator<Map.Entry<String, byte[]>> iterator = faceFeatureRegistry.entrySet().iterator();
                        for (; iterator.hasNext(); ) {
                            Map.Entry<String, byte[]> next = iterator.next();
                            FaceFeature faceFeatureTarget = new FaceFeature();
                            faceFeatureTarget.setFeatureData(next.getValue());

                            FaceSimilar faceSimilar = new FaceSimilar();
                            frEngine.compareFaceFeature(faceFeatureTarget, faceFeature, faceSimilar);
                            if (faceSimilar.getScore() > similar) {
                                similar = faceSimilar.getScore();
                                faceResultInfo.setName(next.getKey());
                            }
                        }

//                        log.info("相似度：{}", similar);
                        if(similar >= 0.7f){
                            faceResultInfo.setSimilar(similar);
                            faceResultInfo.setFlag(true);
                            faceResultRegistry.put(faceInfo.getFaceId(), faceResultInfo);
                        }
                        else{
                            faceResultRegistry.remove(faceInfo.getFaceId());
                        }

                    }

                }
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                if(frEngine != null){
                    frEnginePool.returnObject(frEngine);
                }
            }

        }
    }
}
