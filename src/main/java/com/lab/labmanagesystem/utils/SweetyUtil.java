package com.lab.labmanagesystem.utils;

import com.arcsoft.face.toolkit.ImageFactory;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.opencv_core.CvMat;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.bytedeco.opencv.opencv_core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

@Slf4j
public class SweetyUtil {
    /**
     * byte[]拼接为字符串，逗号分割
     * @param bytes
     * @return
     */
    public static String byte2String(byte[] bytes){

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(bytes[i]);
            if (i != bytes.length - 1) {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    /**
     * 字符串通过逗号分割为byte[]
     * @param str
     * @return
     */
    public static byte[] string2byte(String str){
        String[] strArray = str.split(",");

        byte[] byteArray = new byte[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            byteArray[i] = Byte.parseByte(strArray[i].trim());
        }

        return byteArray;
    }

    /**
     * 将iplImage转换为byte[]
     * @param iplImage
     * @return
     */
    public static byte[] iplImage2byte(IplImage iplImage){
        BytePointer bytePointer = new BytePointer(iplImage.imageData());
        byte[] data = new byte[iplImage.imageSize()];
        bytePointer.get(data);
        return data;
    }

    /**
     * 将bufferedImage转换为byte[]
     * @param image
     * @param format
     * @return
     */
    public static byte[] bufferedImage2byte(BufferedImage image, String format) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, format, baos);
        } catch (IOException e) {
            // 处理异常
        }
        return baos.toByteArray();
    }

    /**
     * 将iplImage转换为string的image
     * @param iplImage
     * @return
     */
    public static String iplImage2String(IplImage iplImage){
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        Frame frame = converter.convert(iplImage);
        Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();
        BufferedImage bufferedImage = java2DFrameConverter.convert(frame);

        String data = "iplImage2String异常";

        // 将 BufferedImage 保存为 PNG 格式的字节流
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            // 将字节流编码为 Base64 字符串
            String base64String = Base64.getEncoder().encodeToString(imageBytes);

            // 格式化为数据 URL
            data = "data:image/png;base64," + base64String;
        } catch (Exception e){
            e.printStackTrace();
        }

        return data;
    }

    public static void copyImage(String sourcePath, String targetPath){
        // 创建源图片文件对象
        File sourceFile = new File(sourcePath);

        try {

            // 创建输入流读取源图片数据
            FileInputStream fis = new FileInputStream(sourceFile);

            // 创建输出流写入目标图片数据
            FileOutputStream fos = new FileOutputStream(targetPath);

            // 定义缓冲区，用于存储读取到的数据
            byte[] buffer = new byte[1024];
            int len;

            // 从输入流中读取数据，并写入输出流
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }

            // 关闭输入流和输出流
            fis.close();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
