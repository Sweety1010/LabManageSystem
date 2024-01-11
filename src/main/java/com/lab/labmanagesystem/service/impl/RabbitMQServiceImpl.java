package com.lab.labmanagesystem.service.impl;
//
import com.lab.labmanagesystem.constant.RabbitMQConstant;
import com.lab.labmanagesystem.service.RabbitMQService;
import com.lab.labmanagesystem.utils.SweetyUtil;
import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

//
@Service
@Slf4j
public class RabbitMQServiceImpl implements RabbitMQService {

    private final RabbitTemplate rabbitTemplate;

    private final String imageQueueName = RabbitMQConstant.LAB_QUEUE_NAME;

    private int messageNums;

    public RabbitMQServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 获取消息队列当前队列的数量
     * @return
     */
    public int getMessageNums(String queue){
        return rabbitTemplate.execute(channel -> {
            try {
                // 获取队列消息数量
                AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(queue);
                return declareOk.getMessageCount();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        });
    }

    /**
     * 向rabbitmq发送ipl图像
     * @param iplImage
     */
    public void sendIplImage(IplImage iplImage) {

        // 获取消息队列当前队列的数量
//        messageNums = getMessageNums(imageQueueName);
////        log.info(String.valueOf(messageNums));
//
//        // 出错或消息积压
//        if(messageNums == -1 || messageNums > 2){
//            return;
//        }

//        Long s = System.currentTimeMillis();

        String imageData = SweetyUtil.iplImage2String(iplImage);

        rabbitTemplate.convertAndSend(RabbitMQConstant.LAB_QUEUE_NAME, imageData);

//        log.info("时间差：{}", System.currentTimeMillis() - s);
    }
}
