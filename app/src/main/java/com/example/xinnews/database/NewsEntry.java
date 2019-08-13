package com.example.xinnews.database;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Entity(tableName = "news_table")
public class NewsEntry {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "newsId")
    private String newsId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "category")
    private String publishTime;

    @ColumnInfo(name = "keywords")
    private String keywords;

    @ColumnInfo(name = "images")
    private String images;

    @ColumnInfo(name = "videos")
    private String videos;

    @ColumnInfo(name = "publisher")
    private String publisher;

    @ColumnInfo(name = "viewed")
    private boolean viewed;

    NewsEntry(JSONObject news, boolean viewed, boolean loadImage, boolean loadVideo) throws JSONException {
        this.newsId = news.getString("newsID");
        this.title = news.getString("title");
        this.content = news.getString("content");
        this.publishTime = news.getString("publishTime");
        this.keywords = news.getJSONArray("keywords").toString();
        this.images = dataGenerate(news.getString("image"), loadImage);
        this.videos = dataGenerate(news.getString("video"), loadVideo);
        this.publisher = news.getString("publisher");
        this.viewed = viewed;
    }

    private String loadData(String address) {
        // TODO: download web data and store
        return address;
    }

    private String dataGenerate(String sources, boolean load) throws JSONException {
        if (!load) return sources;
        JSONArray sourceJson = new JSONArray(sources);
        JSONArray targetJson = new JSONArray();
        for (int i = 0; i < sources.length(); ++ i)
            targetJson.put(loadData(sourceJson.getString(i)));
        return targetJson.toString();
    }

    // TODO: functions of getting information
}
