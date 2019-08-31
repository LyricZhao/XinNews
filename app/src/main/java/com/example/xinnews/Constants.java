package com.example.xinnews;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Constants {
    static public final String homePage = "主页";
    static public final String favorite = "收藏";
    static public final String recommend = "推荐";
    static public final String search = "搜索";
    static public final int searchId = 3;
    static public final String categorySettings = "管理";
    static public final int navigationOffset = 5;
    static public final String[] categories = {"娱乐", "军事", "教育", "文化", "健康", "财经", "体育", "汽车", "科技", "社会"};
    static public final int allCategoriesCount = 10;
    static public final int pageSize = 15;

    static public String getCurrentDate() {
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }
}