package com.lab.labmanagesystem.controller;

import com.lab.labmanagesystem.dto.LoginDTO;
import com.lab.labmanagesystem.result.Result;
import com.lab.labmanagesystem.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class LoginController {

    @Autowired
    LoginService loginService;

    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO){
        log.info("登录请求：{}", loginDTO);

        int code = loginService.login(loginDTO);

        return Result.success(code);
    }
}
