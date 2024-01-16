package com.lab.labmanagesystem.mapper;

import com.lab.labmanagesystem.entity.Login;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LoginMapper {

    /**
     * 根据用户名查询账号密码
     * @param username
     * @return
     */
    @Select("select * from login where username = #{username}")
    Login getByUsername(String username);
}
