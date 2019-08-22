package com.example.xinnews.database;

import android.graphics.Bitmap;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.example.xinnews.Bridge;
import com.example.xinnews.MainActivity;
import com.example.xinnews.PicsCache;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.transform.sax.TemplatesHandler;
import java.util.ArrayList;
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

    @Ignore
    ArrayList<Bitmap> bitmapsCache = new ArrayList<>();

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

    public boolean hasImage() {
        return !images.equals("");
    }

    public void logInfo() {
        Log.i(LOG_TAG, "News " + newsId + ":");
        Log.i(LOG_TAG, "  " + getTitle());
        Log.i(LOG_TAG, "  " + getCategory());
        Log.i(LOG_TAG, "  " + getContent());
        Log.i(LOG_TAG, "  " + getPublisher());
        Log.i(LOG_TAG, "  " + getPublishTime());
    }

    public String loadData(String address, int index) {
        // TODO: download web data and store, here it's the image/video data
        Bridge newsCrawler = new Bridge();
        Log.d(LOG_TAG,getTitle() + ": Loading image ...");
        try {
            Bitmap bitmap = Bridge.getImageFromUrl(address);
            PicsCache.add(getNewsId(), bitmap);
            Log.d(LOG_TAG, String.valueOf(index));
            Log.d(LOG_TAG, this.toString());
//            Bridge.saveToInternalStorage(getNewsId() + index, bitmap);
        } catch (Exception exception) {
            Log.e(LOG_TAG, exception.toString());
        }
        return address;
    }

    // TODO: differ image and video
    private String dataGenerate(String sources, boolean load) throws JSONException {
        if (!load) return sources;
        if (sources.length() < 3) return "";
        Matcher matcher = Pattern.compile("(.+?)[\\]|,]").matcher(sources.substring(1));
        JSONArray targetJson = new JSONArray();
        int index = 0;
        while (matcher.find())
            targetJson.put(loadData(matcher.group(1), index ++));
        return targetJson.toString();
    }

    // TODO: functions of getting information
}
