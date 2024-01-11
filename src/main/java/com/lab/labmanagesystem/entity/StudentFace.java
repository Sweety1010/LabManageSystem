package com.lab.labmanagesystem.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class StudentFace implements Serializable {
    Long id;
    Long studentId;
    String name;
    String face;
    String photo;
}
