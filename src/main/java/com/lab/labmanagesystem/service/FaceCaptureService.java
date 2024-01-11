package com.lab.labmanagesystem.service;

import com.arcsoft.face.toolkit.ImageInfo;
import com.lab.labmanagesystem.dto.FaceCaptureDTO;
import com.lab.labmanagesystem.dto.InformationSubmitDTO;
import com.lab.labmanagesystem.vo.FaceCaptureVO;
import com.lab.labmanagesystem.vo.FaceRecognitionVO;

import java.util.List;

public interface FaceCaptureService {
    /**
     * 人脸检测 提取人脸信息---人脸位置、年龄、性别、活体等
     */
    List<FaceCaptureVO> faceDetect(FaceCaptureDTO faceCaptureDTO);

    /**
     * 提取人脸特征
     * @param image
     * @return 返回人脸数量
     */
    int getFaceFeature(String image);

    /**
     * 保存个人信息+人脸特征
     * @param informationSubmitDTO
     */
    void saveInfomation(InformationSubmitDTO informationSubmitDTO);

    /**
     * 人脸信息预加载
     */
    void preloading();

    /**
     * 人脸识别（与redis中的人脸特征匹配）
     * @param faceCaptureDTO
     * @return
     */
    List<FaceRecognitionVO> faceRecognition(FaceCaptureDTO faceCaptureDTO);

    /**
     * 人脸识别
     * @param rgbData
     * @return
     */
    List<FaceRecognitionVO> faceRecognitionByImageInfo(ImageInfo rgbData);
}
