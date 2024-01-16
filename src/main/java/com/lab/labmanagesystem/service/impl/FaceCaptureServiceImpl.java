package com.lab.labmanagesystem.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;;
import com.arcsoft.face.Rect;
import com.google.common.collect.Lists;
import com.lab.labmanagesystem.constant.MessageConstant;
import com.lab.labmanagesystem.constant.RedisKeyConstant;
import com.lab.labmanagesystem.dto.FaceCaptureDTO;
import com.lab.labmanagesystem.dto.InformationSubmitDTO;
import com.lab.labmanagesystem.entity.CompareFaceInfo;
import com.lab.labmanagesystem.entity.ProcessInfo;
import com.lab.labmanagesystem.entity.Student;
import com.lab.labmanagesystem.entity.StudentFace;
import com.lab.labmanagesystem.exception.FaceException;
import com.lab.labmanagesystem.mapper.StudentFaceMapper;
import com.lab.labmanagesystem.mapper.StudentMapper;
import com.lab.labmanagesystem.service.FaceCaptureService;
import com.lab.labmanagesystem.service.FaceEngineService;
import com.lab.labmanagesystem.utils.AliOssUtil;
import com.lab.labmanagesystem.utils.Base64Util;
import com.lab.labmanagesystem.utils.SweetyUtil;
import com.lab.labmanagesystem.vo.FaceCaptureVO;
import com.lab.labmanagesystem.vo.FaceRecognitionVO;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.toolkit.ImageFactory;
import com.arcsoft.face.toolkit.ImageInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FaceCaptureServiceImpl implements FaceCaptureService {

    @Autowired
    FaceEngineService faceEngineService;

    @Autowired
    StudentMapper studentMapper;

    @Autowired
    StudentFaceMapper studentFaceMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 人脸信息预加载
     */
    public void preloading() {
        log.info("人脸信息预加载...");

        // 读取学生信息
        List<Student> studentList = studentMapper.getStudent();

        // 读取人脸信息
        List<StudentFace> studentFaceList = studentFaceMapper.getStudentFace();

        // 先清除redis中的key
        redisTemplate.delete(RedisKeyConstant.KEY_STUDENT_FACE);
        redisTemplate.delete(RedisKeyConstant.KEY_STUDENT);

        // student保存至redis中
        for(Student student : studentList){
            String studentJson = JSONObject.toJSON(student).toString();
            // 存储完整列表
            redisTemplate.opsForList().leftPush(RedisKeyConstant.KEY_STUDENT, studentJson);

            // 存储单个值
            redisTemplate.opsForValue().set(student.getId() + RedisKeyConstant.KEY_SINGLE_STUDENT, studentJson);
        }

        // studentFace保存至redis中
        for(StudentFace studentFace : studentFaceList){
            String studentFaceJson = JSONObject.toJSON(studentFace).toString();
            // 存储完整列表
            redisTemplate.opsForList().leftPush(RedisKeyConstant.KEY_STUDENT_FACE, studentFaceJson);

            // 存储单个值
            redisTemplate.opsForValue().set(studentFace.getStudentId() + RedisKeyConstant.KEY_SINGLE_STUDENT_FACE, studentFaceJson);

            log.info("预加载人员信息：{}", studentFace.getName());
        }

    }

    /**
     * 人脸检测 提取人脸信息---人脸位置、年龄、性别、活体等
     */
    public List<FaceCaptureVO> faceDetect(FaceCaptureDTO faceCaptureDTO) {
//        log.info(faceCaptureDTO.getImage());

        String image = faceCaptureDTO.getImage();
        byte[] bytes = Base64Util.base64ToBytes(image);

        ImageInfo rgbData = ImageFactory.getRGBData(bytes);

        // 创建返回列表
        List<FaceCaptureVO> faceCaptureVOList = Lists.newLinkedList();

        // 人脸检测
        List<FaceInfo> faceInfoList = faceEngineService.detectFaces(rgbData);

        // 检测不为空
        if(!faceInfoList.isEmpty()){
            // 处理人脸信息
            List<ProcessInfo> processInfoList = faceEngineService.process(rgbData, faceInfoList);

            for(int i = 0; i < faceInfoList.size(); i++){
                FaceCaptureVO faceCaptureVO = new FaceCaptureVO();

                FaceInfo faceInfo = faceInfoList.get(i);

                faceCaptureVO.setRect(faceInfo.getRect());
                faceCaptureVO.setOrient(faceInfo.getOrient());
                faceCaptureVO.setFaceId(faceInfo.getFaceId());

                if(!processInfoList.isEmpty()){
                    ProcessInfo processInfo = processInfoList.get(i);

                    faceCaptureVO.setAge(processInfo.getAge());
                    faceCaptureVO.setGender(processInfo.getGender());
                    faceCaptureVO.setLiveness(processInfo.getLiveness());
                }
                faceCaptureVOList.add(faceCaptureVO);
            }
        }

        return faceCaptureVOList;
    }

    /**
     * 提取人脸特征
     * @param image
     * @return 人脸数量
     */
    public int getFaceFeature(String image) {
//        String image = faceCaptureDTO.getImage();
        byte[] bytes = Base64Util.base64ToBytes(image);

        ImageInfo rgbData = ImageFactory.getRGBData(bytes);

        // 人脸检测
        List<FaceInfo> faceInfoList = faceEngineService.detectFaces(rgbData);

        // 判断faceInfoList的长度
        if(!faceInfoList.isEmpty()) {

            // 人脸数量不唯一
            if(faceInfoList.size() > 1){
                redisTemplate.delete(RedisKeyConstant.KEY_FACE_FEATURE);
                redisTemplate.delete(RedisKeyConstant.KEY_FACE_RECT);
                return faceInfoList.size();
            }

            FaceInfo faceInfo = faceInfoList.get(0);
            // 提取人脸特征
            byte[] faceFeature = faceEngineService.extractFaceFeature(rgbData, faceInfo);
            // 将提取结果特征拼接成字符串
            String feature = SweetyUtil.byte2String(faceFeature);
            // 将人脸特征存入redis中
            redisTemplate.opsForValue().set(RedisKeyConstant.KEY_FACE_FEATURE, feature);
            // 设定180秒缓存时间
//            redisTemplate.expire(RedisKeyConstant.KEY_FACE_FEATURE, 180, TimeUnit.SECONDS);

            // 将人脸的位置信息存入redis
            int left = faceInfo.getRect().getLeft();
            int right = faceInfo.getRect().getRight();
            int top = faceInfo.getRect().getTop();
            int bottom = faceInfo.getRect().getBottom();
            String rect = left + "," + right + "," + top + "," + bottom;
            redisTemplate.opsForValue().set(RedisKeyConstant.KEY_FACE_RECT, rect);
            // 设定180秒缓存时间
//            redisTemplate.expire(RedisKeyConstant.KEY_FACE_RECT, 180, TimeUnit.SECONDS);

            return 1;
        }
        else{
            redisTemplate.delete(RedisKeyConstant.KEY_FACE_FEATURE);
            redisTemplate.delete(RedisKeyConstant.KEY_FACE_RECT);
            // 未检测到人脸
            return 0;
        }
    }

    /**
     * 保存个人信息+人脸信息
     * @param informationSubmitDTO
     */
    public void saveInfomation(InformationSubmitDTO informationSubmitDTO) {

        // 1. 将个人信息保存至student表中
        Student student = new Student();
        BeanUtils.copyProperties(informationSubmitDTO, student);

        student.setCreateTime(LocalDateTime.now());
        student.setUpdateTime(LocalDateTime.now());

        studentMapper.save(student);

        //-------------------------------------------------------------------
        // 2. 将人脸信息保存至student_face表中
        StudentFace studentFace = new StudentFace();

        Long studentId = student.getId();
        studentFace.setStudentId(studentId);

        studentFace.setName(student.getName());

        // 从redis中获取人脸特征
        String feature = (String) redisTemplate.opsForValue().get(RedisKeyConstant.KEY_FACE_FEATURE);
        studentFace.setFace(feature);

        // 再从原图中截取图片，得到头像
        String rectInfo = (String) redisTemplate.opsForValue().get(RedisKeyConstant.KEY_FACE_RECT);

        // 顺序为left,right,top,bottom
        int[] rect = Arrays.stream(rectInfo.split(","))
                .mapToInt(Integer::parseInt)
                .toArray();


        byte[] image = Base64Util.base64ToBytes(informationSubmitDTO.getPhoto());
        // 修建图片 并将图片上传至阿里云Oss
        try{
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(image));

            // 处理越界 加大人脸范围
            double k_w = 0.2;
            double k_h = 0.4;
            int x = rect[0] - k_w * (rect[1] - rect[0]) > 0 ? (int) (rect[0] - k_w * (rect[1] - rect[0])) : 0;
            int y = rect[2] - k_h * (rect[3] - rect[2]) > 0 ? (int) (rect[2] - k_h * (rect[3] - rect[2])) : 0;
            int width = (int) ((1 + 2 * k_w) * (rect[1] - rect[0]));
            int height = (int) ((1 + k_h + 0.15) * (rect[3] - rect[2]));
            if(x + width > originalImage.getWidth()){
                width = originalImage.getWidth() - x - 1;
            }
            if(y + height > originalImage.getHeight()){
                height = originalImage.getHeight() - y - 1;
            }

            // 裁剪图片
            BufferedImage newImage = originalImage.getSubimage(x, y, width, height);

            // 将图片上传至aliOss服务器
            String objName = studentFace.getName() + ".jpg";
            byte[] picture = SweetyUtil.bufferedImage2byte(newImage, "jpg");

            String filePath = aliOssUtil.upload(picture, objName);
            studentFace.setPhoto(filePath);

        } catch (IOException e){
            e.printStackTrace();
        }

        // 将studentFace写入sql
        studentFaceMapper.saveFace(studentFace);

        // 将student和studentFace添加至redis中
        saveStudent2Redis(student, studentFace);
    }

    /**
     * 人脸识别
     * @param faceCaptureDTO
     * @return
     */
    public List<FaceRecognitionVO> faceRecognition(FaceCaptureDTO faceCaptureDTO) {
        String image = faceCaptureDTO.getImage();
        byte[] bytes = Base64Util.base64ToBytes(image);

        ImageInfo rgbData = ImageFactory.getRGBData(bytes);

        return faceRecognitionByImageInfo(rgbData);
    }

    public List<FaceRecognitionVO> faceRecognitionByImageInfo(ImageInfo rgbData){
        List<FaceRecognitionVO> faceRecognitionVOList = Lists.newLinkedList();

        // 检测人脸
        List<FaceInfo> faceInfoList = faceEngineService.detectFaces(rgbData);

        // 人脸不为空 准备进行人脸匹配
        if(!faceInfoList.isEmpty()) {

            // 人脸特征列表
            List<StudentFace> studentFaceList = new ArrayList<>();
            // 从redis中读取人脸特征 studentFace
            List<String> studentFaceLists = redisTemplate.opsForList().range(RedisKeyConstant.KEY_STUDENT_FACE, 0, -1);
            for(String studentFaceStr : studentFaceLists){
                StudentFace studentFace = JSON.parseObject(studentFaceStr, StudentFace.class);
                studentFaceList.add(studentFace);
            }

            for(FaceInfo faceInfo : faceInfoList){
                FaceRecognitionVO faceRecognitionVO = new FaceRecognitionVO();

                // 设置人脸位置
                faceRecognitionVO.setRect(faceInfo.getRect());

                // 提取人脸特征
                byte[] feature = faceEngineService.extractFaceFeature(rgbData, faceInfo);

                if(feature != null){
                    // 将人脸特征与人脸库进行匹配
                    List<CompareFaceInfo> compareFaceInfoList = faceEngineService.faceRecognition(feature, studentFaceList, 0.7f);

                    // 成功匹配到人脸
                    if(compareFaceInfoList.size() > 0){
                        CompareFaceInfo compareFaceInfo = compareFaceInfoList.get(0);

                        // 设置姓名，相似度
                        faceRecognitionVO.setName(compareFaceInfo.getName());
                        faceRecognitionVO.setSimilar(compareFaceInfo.getSimilar());

                        // 从redis中读取个人信息与人脸特征信息
                        String studentStr = (String) redisTemplate.opsForValue().get(compareFaceInfo.getId() + RedisKeyConstant.KEY_SINGLE_STUDENT);
                        Student student = JSON.parseObject(studentStr, Student.class);

                        String studentFaceStr = (String) redisTemplate.opsForValue().get(compareFaceInfo.getId() + RedisKeyConstant.KEY_SINGLE_STUDENT_FACE);
                        StudentFace studentFace = JSON.parseObject(studentFaceStr, StudentFace.class);

                        // 设置头像，性别，年级，工位，省份
                        faceRecognitionVO.setPhoto(studentFace.getPhoto());
                        faceRecognitionVO.setSex(student.getSex());
                        faceRecognitionVO.setGrade(student.getGrade());
                        faceRecognitionVO.setDesk(student.getDesk());
                        faceRecognitionVO.setProvince(student.getProvince());
                    }
                    faceRecognitionVOList.add(faceRecognitionVO);
                }

//                faceRecognitionVOList.add(faceRecognitionVO);
            }
        }

        return faceRecognitionVOList;
    }

    private void saveStudent2Redis(Student student, StudentFace studentFace){
        // 存储student
        String studentJson = JSONObject.toJSON(student).toString();
        // 添加进入列表
        redisTemplate.opsForList().leftPush(RedisKeyConstant.KEY_STUDENT, studentJson);
        // 添加单个值
        redisTemplate.opsForValue().set(student.getId() + RedisKeyConstant.KEY_SINGLE_STUDENT, studentJson);

        // 存储studentFace
        String studentFaceJson = JSONObject.toJSON(studentFace).toString();
        // 添加进入列表
        redisTemplate.opsForList().leftPush(RedisKeyConstant.KEY_STUDENT_FACE, studentFaceJson);
        // 添加单个值
        redisTemplate.opsForValue().set(studentFace.getStudentId() + RedisKeyConstant.KEY_SINGLE_STUDENT_FACE, studentFaceJson);
    }


}
