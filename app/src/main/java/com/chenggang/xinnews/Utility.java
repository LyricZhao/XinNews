package com.chenggang.xinnews;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

class Utility {
    static final String homePage = "主页";
    static final String favorite = "收藏";
    static final String recommend = "推荐";
    static final String search = "搜索";
    static final int searchId = 3;
    static final String categorySettings = "管理";
    static final int navigationOffset = 5;
    static final String[] categories = {"娱乐", "军事", "教育", "文化", "健康", "财经", "体育", "汽车", "科技", "社会"};
    static final int allCategoriesCount = 10;
    static final int pageSize = 15;

    static String getCurrentDate() {
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    static String tagger(String newsId, int index) {
        return newsId + "_" + index;
    }
}