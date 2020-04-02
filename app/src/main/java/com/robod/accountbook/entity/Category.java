package com.robod.accountbook.entity;

/**
 * 添加记录列表的每一个分类子项，里面包含一个图标的Id和分类的名称
 * @author Robod Lee
 * @date 2020/2/6 18:32
 */
public class Category {

    private int imageId;    //图片的资源路径
    private String name;    //分类的名称

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
