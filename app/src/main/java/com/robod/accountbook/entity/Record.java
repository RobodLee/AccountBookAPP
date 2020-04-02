package com.robod.accountbook.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Robod Lee
 * @date 2020/2/5 17:02
 * 收支记录的实体类
 */
@Setter
@Getter
@ToString
public class Record extends LitePalSupport implements Serializable {

    private int id;             //主键
    private String category;    //分类的名称
    private String content;     //备注
    private double money;       //金额,大于等于0代表收入，用绿色表示;小于0代表支出，用红色表示，等于0用灰色表示
    //状态，0:已同步到服务器，1:未同步到服务器，
    // 2:之前同步到服务器现在本地删除，同步时让服务器删除这条记录,3.之前同步到服务器现在在本地修改，同步时让服务器修改这条记录
    private int status;
    private Date date;          //记录的日期
    private String dateString;  //日期的字符串2020-04-01
    private String uuid;        //每条记录的唯一标识符

}
