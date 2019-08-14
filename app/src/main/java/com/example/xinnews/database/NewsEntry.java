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
    String newsId;

    @ColumnInfo(name = "title")
    String title;

    @ColumnInfo(name = "content")
    String content;

    @ColumnInfo(name = "category")
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
        this.content = news.getString("content");
        this.publishTime = news.getString("publishTime");
        this.keywords = news.getJSONArray("keywords").toString();
        this.images = dataGenerate(news.getString("image"), true);
        this.videos = dataGenerate(news.getString("video"), false);
        this.publisher = news.getString("publisher");
        this.viewed = false;
        this.favorite = false;
    }

    public String loadData(String address) {
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
