package com.lab.labmanagesystem.service;


import org.bytedeco.opencv.opencv_core.IplImage;

public interface RabbitMQService {

    /**
     * 获取消息队列当前队列的数量
     */
    int getMessageNums(String queue);

    /**
     * 向rabbitmq发送ipl图像
     * @param iplImage
     */
    void sendIplImage(IplImage iplImage);
}
