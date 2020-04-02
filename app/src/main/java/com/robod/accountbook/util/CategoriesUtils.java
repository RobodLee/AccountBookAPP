package com.robod.accountbook.util;

import com.robod.accountbook.R;

/**
 * @author Robod Lee
 * @date 2020/2/6 18:22
 * 收支记录的分类的名称和图标路径的数组
 */
public class CategoriesUtils {

    /**
     * 收入
     */
    public static int[] incomeCategoryIcon = {R.drawable.salary, R.drawable.red_packet, R.drawable.part_time,R.drawable.other};

    public static String[] incomeCategoryName = {"工资","收红包","兼职","其它"};

    /**
     * 支出
     */
    public static int[] expenditureCategoryIcon = {R.drawable.eat,R.drawable.snack,R.drawable.clothes,R.drawable.traffic,
            R.drawable.phone_internet_fee,R.drawable.study,R.drawable.commodity,R.drawable.medical,R.drawable.amusement,
            R.drawable.electronic,R.drawable.sport,R.drawable.other};

    public static String[] expenditureCategoryName = {"吃饭","零食","衣服","交通","话费网费","学习"
            ,"日用品","医疗","娱乐","电器数码","运动","其它"};

}
