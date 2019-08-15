package com.example.xinnews;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.example.xinnews.database.NewsEntry;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class NewsCrawler {
    static private final int defaultCrawlSize = 15;
    static private final String LOG_TAG = "NewsCrawler";
    static private final String baseUrl = "https://api2.newsminer.net/svc/news/queryNewsList";
    static private final String encoding = "UTF-8";
    static private final String[] availableCategories = {"娱乐", "军事", "教育", "文化", "健康", "财经", "体育", "汽车", "科技", "社会"};

    static boolean isLegalCategory(String category) {
        for (String availableCategory: availableCategories)
            if (availableCategory.equals(category))
                return true;
        return false;
    }

    private static String getUrlContent(String urlAddress) throws Exception {
        URL url = new URL(urlAddress);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), encoding));
        String incoming;
        StringBuilder result = new StringBuilder();
        while ((incoming = bufferedReader.readLine()) != null)
            result.append(incoming);
        return result.toString();
    }

    static boolean checkConnectionToApi() {
        try {
            String content = getUrlContent(baseUrl);
            Log.i(LOG_TAG, "Connection success, size = " + content.length() + ".");
        } catch (Exception exception) {
            Log.e(LOG_TAG, "Checking connection to API failed.");
            return false;
        }
        return true;
    }

    public static Bitmap getImageFromUrl(String urlAddress) throws Exception {
        InputStream input = new URL(urlAddress).openStream();
        return BitmapFactory.decodeStream(input);
    }

    private static JSONArray getNewsJsonArray(int size, String startDate, String endDate, String words, String categories) throws Exception {
        if (size == 0) size = defaultCrawlSize;
        StringBuilder queryUrl = new StringBuilder(baseUrl + "?");
        queryUrl.append("size=").append(size);
        if (startDate != null) queryUrl.append("&startDate=").append(startDate);
        if (endDate != null) queryUrl.append("&endDate=").append(endDate);
        if (words != null) queryUrl.append("&words=").append(words);
        if (categories != null) queryUrl.append("&categories").append(categories);
        String jsonContent = getUrlContent(queryUrl.toString());
        JSONObject globalContent = new JSONObject(jsonContent);
        return globalContent.getJSONArray("data");
    }

    public static ArrayList<NewsEntry> getNewsEntryArray(int size, String startDate, String endDate, String words, String categories) throws Exception {
        JSONArray jsonArray = getNewsJsonArray(size, startDate, endDate, words, categories);
        ArrayList<NewsEntry> entries = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); ++ i)
            entries.add(new NewsEntry(jsonArray.getJSONObject(i)));
        return entries;
    }
}