package com.lab.labmanagesystem.service;

import com.lab.labmanagesystem.dto.LoginDTO;

public interface LoginService {

    /**
     * 登录请求
     * @param loginDTO
     */
    int login(LoginDTO loginDTO);
}
