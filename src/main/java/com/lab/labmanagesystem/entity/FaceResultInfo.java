package com.lab.labmanagesystem.entity;

import lombok.Data;

@Data
public class FaceResultInfo {
    private boolean flag = false;
    private String name;
    private float similar;
}
