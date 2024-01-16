package com.lab.labmanagesystem.mapper;

import com.lab.labmanagesystem.entity.StudentFace;
import org.apache.ibatis.annotations.Delete;
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

    /**
     * 根据id查询student face
     * @param id
     * @return
     */
    @Select("select photo from student_face where student_id = #{id}")
    String getById(Long id);

    /**
     * 根据名字删除人员
     * @param name
     */
    @Delete("delete from student_face where name = #{name}")
    void delete(String name);

    /**
     * 根据名字获取
     * @param name
     * @return
     */
    @Select("select * from student_face where name = #{name}")
    StudentFace getByName(String name);
}
