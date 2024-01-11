package com.lab.labmanagesystem.controller;

import com.lab.labmanagesystem.dto.FaceCaptureDTO;
import com.lab.labmanagesystem.dto.InformationSubmitDTO;
import com.lab.labmanagesystem.result.Result;
import com.lab.labmanagesystem.service.FaceCaptureService;
import com.lab.labmanagesystem.utils.Base64Util;
import com.lab.labmanagesystem.vo.FaceCaptureVO;
import com.lab.labmanagesystem.vo.FaceRecognitionVO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class FaceController {

    @Autowired
    FaceCaptureService faceCaptureService;

    /**
     * 从数据库中加载  名字-对应人脸数据到  redis中
     */
    @PostConstruct
    public void initFace(){
        faceCaptureService.preloading();
    }

    /**
     * 人脸检测 提取人脸信息---人脸位置、年龄、性别、活体等
     * @return
     */
    @PostMapping("/faceDetect")
    public Result<List<FaceCaptureVO>> faceDetect(@RequestBody FaceCaptureDTO faceCaptureDTO){
//        log.info(faceCaptureDTO.getImage());

        List<FaceCaptureVO> faceCaptureVOList = faceCaptureService.faceDetect(faceCaptureDTO);

        return Result.success(faceCaptureVOList);
    }

    @PostMapping("/getFaceFeature")
    public Result getFaceFeature(@RequestBody FaceCaptureDTO faceCaptureDTO){
//        log.info(faceCaptureDTO.getImage());

        faceCaptureService.getFaceFeature(faceCaptureDTO.getImage());

        return Result.success();
    }

    @PostMapping("/informationSubmit")
    public Result informationSave(@RequestBody InformationSubmitDTO informationSubmitDTO){
//        log.info("提交信息:{}", informationSubmitDTO);

        // 获取人脸特征  检查人脸数量是否符合要求  存入redis
        int faceNums = faceCaptureService.getFaceFeature(informationSubmitDTO.getPhoto());

        // 确保只有一张人脸
        if(faceNums == 1){
            faceCaptureService.saveInfomation(informationSubmitDTO);
        }

        return Result.success(faceNums);
    }

    @PostMapping("/faceRecognition")
    public Result<List<FaceRecognitionVO>> faceRecognition(@RequestBody FaceCaptureDTO faceCaptureDTO){
//        log.info(faceCaptureDTO.toString());

        List<FaceRecognitionVO> faceRecognitionVOList = faceCaptureService.faceRecognition(faceCaptureDTO);

        return Result.success(faceRecognitionVOList);
    }
}
