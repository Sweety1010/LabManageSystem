package com.lab.labmanagesystem.entity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class Student {
    private Long id;

    private String name;

    private String phone;

    private String sex;

    private String grade;

    private Integer enrollmentYear;

    private String desk;

    private String province;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
