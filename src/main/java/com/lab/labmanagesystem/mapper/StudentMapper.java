package com.lab.labmanagesystem.mapper;

import com.lab.labmanagesystem.entity.Student;
import com.lab.labmanagesystem.entity.StudentFace;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentMapper {

    /**
     * 保存个人基本信息
     * @param student
     */
    void save(Student student);

    /**
     * 读取个人基本信息
     * @return
     */
    @Select("select * from student")
    List<Student> getStudent();
}
