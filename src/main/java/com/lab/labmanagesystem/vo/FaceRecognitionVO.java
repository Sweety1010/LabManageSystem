package com.lab.labmanagesystem.vo;

import com.arcsoft.face.Rect;
import lombok.Data;

@Data
public class FaceRecognitionVO {

    private Rect rect;
    private String name;
    private float similar;
    private String sex;
    private String grade;
    private String desk;
    private String province;
    private String photo;
}
