package com.lab.labmanagesystem.context;

import com.lab.labmanagesystem.vo.FaceRecognitionVO;

import java.util.List;

public class BaseContext {

    public static ThreadLocal<List<FaceRecognitionVO>> threadLocal = new ThreadLocal<>();

    public static void setInfo(List<FaceRecognitionVO> info){
        threadLocal.set(info);
    }

    public static List<FaceRecognitionVO> getInfo(){
        return threadLocal.get();
    }

}
