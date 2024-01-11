package com.lab.labmanagesystem.vo;

import com.arcsoft.face.Rect;
import lombok.Data;

@Data
public class FaceCaptureVO {
    private Rect rect;
    private int orient;
    private int faceId = -1;
    private int age = -1;
    private int gender = -1;
    private int liveness = -1;
}
