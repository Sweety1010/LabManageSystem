package com.lab.labmanagesystem.service;

import jakarta.servlet.ServletOutputStream;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;

public interface StreamService {

    /**
     * 视频流处理
     * @param grabber
     * @param outputStream
     */
    void servletStreamPlayer(FrameGrabber grabber, ServletOutputStream outputStream) throws Exception;
}
