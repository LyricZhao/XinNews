package com.example.xinnews.database;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Parameter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity(tableName = "news_table")
public class NewsEntry {

    private static final String LOG_TAG = "NewsEntry";

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "newsId")
    String newsId;

    @ColumnInfo(name = "title")
    String title;

    @ColumnInfo(name = "content")
    String content;

    @ColumnInfo(name = "category")
    String category;

    @ColumnInfo(name = "publishTime")
    String publishTime;

    @ColumnInfo(name = "keywords")
    String keywords;

    @ColumnInfo(name = "images")
    String images;

    @ColumnInfo(name = "videos")
    String videos;

    @ColumnInfo(name = "publisher")
    String publisher;

    @ColumnInfo(name = "viewed")
    boolean viewed;

    @ColumnInfo(name = "favorite")
    boolean favorite;

    public NewsEntry() { }

    public NewsEntry(@NonNull JSONObject news) throws JSONException {
        this.newsId = news.getString("newsID");
        this.title = news.getString("title");
        this.category = news.getString("category");
        this.content = news.getString("content");
        this.publishTime = news.getString("publishTime");
        this.keywords = news.getJSONArray("keywords").toString();
        this.images = dataGenerate(news.getString("image"), true);
        this.videos = dataGenerate(news.getString("video"), false);
        this.publisher = news.getString("publisher") + " "; // TODO: maybe we can remove the space later
        this.viewed = false;
        this.favorite = false;
    }

    public String getNewsId() {
        return newsId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public String getKeywords() {
        return keywords;
    }

    public String getImages() {
        return images;
    }

    public String getVideos() {
        return videos;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getCategory() {
        return category;
    }

    public void logInfo() {
        Log.i(LOG_TAG, "News " + newsId + ":");
        Log.i(LOG_TAG, "  " + getTitle());
        Log.i(LOG_TAG, "  " + getCategory());
        Log.i(LOG_TAG, "  " + getContent());
        Log.i(LOG_TAG, "  " + getPublisher());
        Log.i(LOG_TAG, "  " + getPublishTime());
    }

    public String loadData(String address) {
        // TODO: download web data and store, here it's the image/video data
        return address;
    }

    private String dataGenerate(String sources, boolean load) throws JSONException {
        if (!load) return sources;
        if (sources.length() < 3) return "";
        Matcher matcher = Pattern.compile("(.+?)[\\]|,]").matcher(sources.substring(1));
        JSONArray targetJson = new JSONArray();
        while (matcher.find())
            targetJson.put(matcher.group(1));
        return targetJson.toString();
    }

    // TODO: functions of getting information
}
