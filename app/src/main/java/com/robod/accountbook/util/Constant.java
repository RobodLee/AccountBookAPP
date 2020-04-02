package com.robod.accountbook.util;

import java.text.DecimalFormat;

/**
 * @author Robod Lee
 * 用户访问服务器的几个路径
 */
public class Constant {

    //注册
    public static final String USER_REGISTER = "/user/register?";

    //登录
    public static final String USER_LOGIN =  "/user/login?";

    //更新数据
    public static final String UPLOAD_RECORDS = "/record/uploadRecords";

    //下载数据
    public static final String DOWNLOAD_RECORDS = "/record/downloadRecords?phoneNumber=";

    //平年
    public static final double[] DAY_OF_MONTH_COMMON_YEAR =
            {1.0,31.0,28.0,31.0,30.0,31.0,30.0,31.0,31.0,30.0,31.0,30.0,31.0};
    //闰年
    public static final double[] DAY_OF_MONTH_LEAP_YEAR =
            {1.0,31.0,29.0,31.0,30.0,31.0,30.0,31.0,31.0,30.0,31.0,30.0,31.0};

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");
    
}
