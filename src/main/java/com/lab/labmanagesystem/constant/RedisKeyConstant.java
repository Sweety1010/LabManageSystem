package com.lab.labmanagesystem.constant;

public class RedisKeyConstant {
    // List<FaceInfo>存入redis中的KEY
    public static final String KEY_FACE = "DETECT_FACE_INFO";

    // List<ProcessInfo>存入redis中的KEY
    public static final String KEY_PROCESS = "DETECT_PROCESS_INFO";

    // 人脸特征
    public static final String KEY_FACE_FEATURE = "GET_FACE_FEATURE";

    // 人脸位置信息
    public static final String KEY_FACE_RECT = "DETECT_FACE_RECT";

    // List<StudentFace>
    public static final String KEY_STUDENT_FACE = "KEY_STUDENT_FACE";

    // List<Student>
    public static final String KEY_STUDENT = "KEY_STUDENT";

    // Student
    public static final String KEY_SINGLE_STUDENT = "KEY_SINGLE_STUDENT";

    // StudentFace
    public static final String KEY_SINGLE_STUDENT_FACE = "KEY_SINGLE_STUDENT_FACE";
}
