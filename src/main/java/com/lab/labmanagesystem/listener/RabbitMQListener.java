package com.lab.labmanagesystem.listener;

import com.arcsoft.face.toolkit.ImageFactory;
import com.arcsoft.face.toolkit.ImageInfo;
import com.lab.labmanagesystem.constant.RabbitMQConstant;
import com.lab.labmanagesystem.context.BaseContext;
import com.lab.labmanagesystem.dto.FaceCaptureDTO;
import com.lab.labmanagesystem.service.FaceCaptureService;
import com.lab.labmanagesystem.service.RabbitMQService;
import com.lab.labmanagesystem.service.impl.FaceCaptureServiceImpl;
import com.lab.labmanagesystem.vo.FaceCaptureVO;
import com.lab.labmanagesystem.vo.FaceRecognitionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class RabbitMQListener {

    @Autowired
    FaceCaptureService faceCaptureService;

    @Autowired
    RabbitMQService rabbitMQService;

    public static List<FaceRecognitionVO> faceRecognitionVOList;

    /**
     * 监听视频流图像消息队列
     * @param imageData
     */
    @RabbitListener(queues = RabbitMQConstant.LAB_QUEUE_NAME)
    public void listenLabQueue(String imageData){

//        Long s = System.currentTimeMillis();

//        int messageNums = rabbitMQService.getMessageNums(RabbitMQConstant.LAB_QUEUE_NAME);
//        log.info(String.valueOf(messageNums));
//        if(messageNums == -1 || messageNums > 2){
//            return;
//        }

        FaceCaptureDTO faceCaptureDTO = new FaceCaptureDTO();
        faceCaptureDTO.setImage(imageData);

        faceRecognitionVOList = faceCaptureService.faceRecognition(faceCaptureDTO);

//        log.info(String.valueOf(System.currentTimeMillis() - s));

    }

    public static List<FaceRecognitionVO> getFaceRecognitionVOList(){
        return faceRecognitionVOList;
    }
}
