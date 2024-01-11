package com.lab.labmanagesystem.entity;

import com.arcsoft.face.FaceInfo;
import lombok.Data;

@Data
public class FacePreviewInfo {
    private FaceInfo faceInfo;
    private int age;
    private boolean liveNess;
}
