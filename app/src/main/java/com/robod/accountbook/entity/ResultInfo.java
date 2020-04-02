package com.robod.accountbook.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 浏览器返回到客户端的实体类
 * @author Robod Lee
 */
@Getter
@Setter
@ToString
public class ResultInfo<T> {

    private boolean flag;   //正常返回true,异常返回false
    private T data;    //返回给客户端的数据
    private String errorMsg;    //异常的信息

}
