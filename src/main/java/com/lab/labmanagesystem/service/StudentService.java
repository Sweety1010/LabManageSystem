package com.lab.labmanagesystem.service;


import com.lab.labmanagesystem.dto.InformationDeleteDTO;
import com.lab.labmanagesystem.dto.InformationSubmitDTO;
import com.lab.labmanagesystem.vo.StudentInfoVO;

import java.util.List;

public interface StudentService {

    /**
     * 获取学生信息
     */
    List<StudentInfoVO> getStudentInfo();

    /**
     * 更新学生信息
     * @param informationSubmitDTO
     */
    void informationEdit(InformationSubmitDTO informationSubmitDTO);

    /**
     * 删除人员
     * @param informationDeleteDTO
     */
    void delete(InformationDeleteDTO informationDeleteDTO);
}
