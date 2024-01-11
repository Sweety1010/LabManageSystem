package com.lab.labmanagesystem.controller;

import com.lab.labmanagesystem.constant.MessageConstant;
import com.lab.labmanagesystem.exception.StreamException;
import com.lab.labmanagesystem.result.Result;
import com.lab.labmanagesystem.service.StreamService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
@Slf4j
public class StreamController {

    // rtsp视频流：   rtsp://admin:admin@192.168.1.101

    @Autowired
    StreamService streamService;

    @GetMapping("/stream")
    public Result stream(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = false) String address) throws Exception {

        response.addHeader("Content-Disposition", "attachment;filename=\"" + "127.0.0.1" + "\"");
        response.setContentType("video/x-flv");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("accept_ranges", "bytes");
        response.setHeader("pragma", "no-cache");
        response.setHeader("cache_control", "no-cache");
        response.setHeader("transfer_encoding", "CHUNKED");

        response.setStatus(200);
        FrameGrabber grabber;

        if(address == null){
            throw new StreamException(MessageConstant.VIDEO_ADDRESS_NULL);
        }

        grabber = new FFmpegFrameGrabber(address);

        streamService.servletStreamPlayer(grabber, response.getOutputStream());

        return Result.success();
    }

}
