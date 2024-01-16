package com.lab.labmanagesystem.controller;

import com.lab.labmanagesystem.dto.InformationDeleteDTO;
import com.lab.labmanagesystem.dto.InformationSubmitDTO;
import com.lab.labmanagesystem.result.Result;
import com.lab.labmanagesystem.service.StudentService;
import com.lab.labmanagesystem.vo.StudentInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class StudentController {

    @Autowired
    StudentService studentService;

    @GetMapping("/getStudentInfo")
    public Result<List<StudentInfoVO>> getStudentInfo(){
//        log.info("学生信息查询");

        List<StudentInfoVO> studentInfoVOList = studentService.getStudentInfo();

        return Result.success(studentInfoVOList);
    }

    @PostMapping("/informationEdit")
    public Result informationEdit(@RequestBody InformationSubmitDTO informationSubmitDTO){
        log.info("信息更改：{}", informationSubmitDTO);

        studentService.informationEdit(informationSubmitDTO);

        return Result.success();
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody InformationDeleteDTO informationDeleteDTO){
        log.info("删除：{}", informationDeleteDTO);

        studentService.delete(informationDeleteDTO);

        return Result.success();
    }


}
