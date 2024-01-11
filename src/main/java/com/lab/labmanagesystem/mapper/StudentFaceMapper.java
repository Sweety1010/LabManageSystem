package com.lab.labmanagesystem.mapper;

import com.lab.labmanagesystem.entity.StudentFace;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentFaceMapper {

    /**
     * 保存人脸数据及头像
     * @param studentFace
     */
    @Insert("insert into student_face (student_id, name, face, photo) " +
            "values " +
            "(#{studentId},#{name},#{face},#{photo})")
    void saveFace(StudentFace studentFace);

    /**
     * 读取人脸数据
     * @return
     */
    @Select("select * from student_face")
    List<StudentFace> getStudentFace();
}
