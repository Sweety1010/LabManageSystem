package com.lab.labmanagesystem;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.CvPoint;
import org.bytedeco.opencv.opencv_imgproc.CvFont;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class LabManageSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabManageSystemApplication.class, args);

        Loader.load(opencv_imgproc.class);
        Loader.load(CvPoint.class);
        Loader.load(CvFont.class);

        log.info("实验室人员管理系统启动成功");
    }

}
