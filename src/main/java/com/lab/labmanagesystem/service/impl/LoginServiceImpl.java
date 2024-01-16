package com.lab.labmanagesystem.service.impl;

import com.lab.labmanagesystem.dto.LoginDTO;
import com.lab.labmanagesystem.entity.Login;
import com.lab.labmanagesystem.mapper.LoginMapper;
import com.lab.labmanagesystem.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Autowired
    LoginMapper loginMapper;

    /**
     * 登录请求
     */
    public int login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        Login login = loginMapper.getByUsername(username);

        if(login == null){
            // 账号不存在
            return 100;
        }

        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if(!password.equals(login.getPassword())){
            // 密码错误
            return 101;
        }

        return 200;
    }
}
