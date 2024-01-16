package com.lab.labmanagesystem.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.lab.labmanagesystem.constant.RedisKeyConstant;
import com.lab.labmanagesystem.dto.InformationDeleteDTO;
import com.lab.labmanagesystem.dto.InformationSubmitDTO;
import com.lab.labmanagesystem.entity.Student;
import com.lab.labmanagesystem.entity.StudentFace;
import com.lab.labmanagesystem.mapper.StudentFaceMapper;
import com.lab.labmanagesystem.mapper.StudentMapper;
import com.lab.labmanagesystem.service.StudentService;
import com.lab.labmanagesystem.vo.StudentInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class StudentServiceImpl implements StudentService {

    @Autowired
    StudentMapper studentMapper;

    @Autowired
    StudentFaceMapper studentFaceMapper;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 获取学生信息
     */
    public List<StudentInfoVO> getStudentInfo() {

        // 获取学生信息
        List<Student> studentList = studentMapper.getStudent();

        List<StudentInfoVO> studentInfoVOList = new ArrayList<>();

        for(Student student : studentList){
            StudentInfoVO studentInfoVO = new StudentInfoVO();
            studentInfoVO.setEnrollmentYear(String.valueOf(student.getEnrollmentYear()));
            studentInfoVO.setSex(student.getSex());
            studentInfoVO.setProvince(student.getProvince());
            studentInfoVO.setDesk(student.getDesk());
            studentInfoVO.setPhone(student.getPhone());
            studentInfoVO.setName(student.getName());

            // 获取头像
            Long id = student.getId();
            String photo = studentFaceMapper.getById(id);

            studentInfoVO.setPhoto(photo);

            studentInfoVOList.add(studentInfoVO);
        }

        return studentInfoVOList;
    }

    /**
     * 更新学生信息
     * @param informationSubmitDTO
     */
    public void informationEdit(InformationSubmitDTO informationSubmitDTO) {
        // 1. 更新数据库中的信息
        Student student = new Student();
        BeanUtils.copyProperties(informationSubmitDTO, student);
        student.setUpdateTime(LocalDateTime.now());

        studentMapper.update(student);

        // 2. 更新redis中的信息
        student = studentMapper.getByName(student.getName());

        // 更新单个值student 先删后增
        String studentJson = JSONObject.toJSON(student).toString();
        // 删除redis键
        redisTemplate.delete(student.getId() + RedisKeyConstant.KEY_SINGLE_STUDENT);
        redisTemplate.opsForList().remove(RedisKeyConstant.KEY_STUDENT, 0, studentJson);
        // 添加redis键
        // 添加单个值
        redisTemplate.opsForValue().set(student.getId() + RedisKeyConstant.KEY_SINGLE_STUDENT, studentJson);
        // 添加进列表
        redisTemplate.opsForList().leftPush(RedisKeyConstant.KEY_STUDENT, studentJson);

    }

    /**
     * 删除人员
     * @param informationDeleteDTO
     */
    public void delete(InformationDeleteDTO informationDeleteDTO) {
        // 获取student与studentFace对象
        Student student = studentMapper.getByName(informationDeleteDTO.getName());
        StudentFace studentFace = studentFaceMapper.getByName(informationDeleteDTO.getName());

        // 1. 删除表student
        studentMapper.delete(informationDeleteDTO.getName());

        // 2. 删除表studentFace
        studentFaceMapper.delete(informationDeleteDTO.getName());

        // 3. 删除redis中的对应数值
        // 删除student单个值
        redisTemplate.delete(student.getId() + RedisKeyConstant.KEY_SINGLE_STUDENT);

        // 删除studentFace单个值
        redisTemplate.delete(student.getId() + RedisKeyConstant.KEY_SINGLE_STUDENT_FACE);

        // 删除student列表中的值
        String studentJson = JSONObject.toJSON(student).toString();
        redisTemplate.opsForList().remove(RedisKeyConstant.KEY_STUDENT, 0, studentJson);

        // 删除studentFace列表中的值
        String studentFaceJson = JSONObject.toJSON(studentFace).toString();
        redisTemplate.opsForList().remove(RedisKeyConstant.KEY_STUDENT_FACE, 0, studentFaceJson);

    }
}
