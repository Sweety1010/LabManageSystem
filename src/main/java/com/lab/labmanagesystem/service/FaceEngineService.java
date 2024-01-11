package com.lab.labmanagesystem.service;


import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.toolkit.ImageInfo;
import com.lab.labmanagesystem.entity.CompareFaceInfo;
import com.lab.labmanagesystem.entity.ProcessInfo;
import com.lab.labmanagesystem.entity.Student;
import com.lab.labmanagesystem.entity.StudentFace;
import org.bytedeco.opencv.opencv_core.IplImage;

import java.util.List;


public interface FaceEngineService {

    /**
     * 人脸检测 提取人脸信息---人脸位置、年龄、性别、活体等
     * @param imageInfo
     * @return
     */
    List<FaceInfo> detectFaces(ImageInfo imageInfo);

    /**
     * 处理人脸信息
     * @param imageInfo
     * @param faceInfoList
     * @return
     */
    List<ProcessInfo> process(ImageInfo imageInfo, List<FaceInfo> faceInfoList);

    /**
     * 提取人脸特征
     * @param imageInfo
     * @param faceInfo
     * @return
     */
    byte[] extractFaceFeature(ImageInfo imageInfo,FaceInfo faceInfo);

    /**
     * 人脸特征匹配
     * @param feature
     * @param studentFaceList
     * @param passRate
     * @return
     */
    List<CompareFaceInfo> faceRecognition(byte[] feature, List<StudentFace> studentFaceList, float passRate);

    /**
     * 视频流检测
     */
    void preview(IplImage iplImage);
}
