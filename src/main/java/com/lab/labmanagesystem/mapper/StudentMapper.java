package com.lab.labmanagesystem.mapper;

import com.lab.labmanagesystem.entity.Student;
import com.lab.labmanagesystem.entity.StudentFace;
import org.apache.ibatis.annotations.Delete;
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
    @Select("select * from student order by enrollment_year desc")
    List<Student> getStudent();

    /**
     * 更新个人信息
     * @param student
     */
    void update(Student student);

    /**
     * 根据名字删除人员
     * @param name
     */
    @Delete("delete from student where name = #{name}")
    void delete(String name);

    /**
     * 根据名字获取对象
     * @param name
     * @return
     */
    @Select("select * from student where name = #{name}")
    Student getByName(String name);
}
