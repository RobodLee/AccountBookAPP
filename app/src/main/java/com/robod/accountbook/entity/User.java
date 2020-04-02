package com.robod.accountbook.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户的实体类
 * @author Robod Lee
 * @date 2020/2/5 17:59
 */
@Getter
@Setter
@ToString
public class User implements Serializable {

    private Integer id;
    private String phoneNumber; //手机号
    private String username;    //用户名
    private String password;    //密码

}