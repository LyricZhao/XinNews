package com.example.xinnews.database;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity(tableName = "news_table")
public class NewsEntry implements Parcelable {

    private static final int BRIEF_MIN_LENGTH = 50;
    private static final String LOG_TAG = "NewsEntry";
    private static final String shareFormat = "标题：%s\n摘要：%s\n发布时间：%s\n作者：%s\n正文链接：%s\n来自欣闻客户端";

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

    @ColumnInfo(name = "link")
    String link;

    public NewsEntry() { }

    public NewsEntry(@NonNull JSONObject news) throws JSONException {
        this.newsId = news.getString("newsID");
        this.title = news.getString("title");
        this.category = news.getString("category");
        this.content = news.getString("content");
        this.publishTime = news.getString("publishTime");
        this.link = news.getString("url");
        this.keywords = news.getJSONArray("keywords").toString();
        this.images = dataGenerate(news.getString("image"));
        this.videos = dataGenerate(news.getString("video"));
        this.publisher = news.getString("publisher") + " "; // TODO: maybe we can remove the space later
        this.viewed = false;
        this.favorite = false;
    }

    protected NewsEntry(Parcel in) {
        newsId = Objects.requireNonNull(in.readString());
        title = in.readString();
        content = in.readString();
        category = in.readString();
        publishTime = in.readString();
        keywords = in.readString();
        images = in.readString();
        videos = in.readString();
        publisher = in.readString();
        link = in.readString();
        viewed = in.readByte() != 0;
        favorite = in.readByte() != 0;
    }

    public static final Creator<NewsEntry> CREATOR = new Creator<NewsEntry>() {
        @Override
        public NewsEntry createFromParcel(Parcel in) {
            return new NewsEntry(in);
        }

        @Override
        public NewsEntry[] newArray(int size) {
            return new NewsEntry[size];
        }
    };

    @NonNull
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

    public String getLink() {
        return link;
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

    public int getImageCount() {
        try {
            if (!hasImage()) return 0;
            return new JSONArray(images).length();
        } catch (Exception exception) {
            Log.e(LOG_TAG, exception.toString());
            return 0;
        }
    }

    public String getCoverImagePath() throws JSONException {
        JSONArray jsonArray = new JSONArray(images);
        return jsonArray.getString(0);
    }

    public ArrayList<String> getAllImagePaths() throws JSONException {
        JSONArray jsonArray = new JSONArray(images);
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); ++ i)
            arrayList.add(jsonArray.getString(i));
        return arrayList;
    }

    public String generateSubtitle() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("来源: " );
        stringBuilder.append(publisher);
        stringBuilder.append(" / ");
        stringBuilder.append(category);
        stringBuilder.append("  ");
        stringBuilder.append(publishTime);
        return stringBuilder.toString();
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

    private String dataGenerate(String sources) throws JSONException {
        if (sources.length() < 3) return "";
        Matcher matcher = Pattern.compile("(.+?)[\\]|,]").matcher(sources.substring(1));
        JSONArray targetJson = new JSONArray();
        while (matcher.find())
            targetJson.put(matcher.group(1));
        return targetJson.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(newsId);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(category);
        dest.writeString(publishTime);
        dest.writeString(keywords);
        dest.writeString(images);
        dest.writeString(videos);
        dest.writeString(publisher);
        dest.writeString(link);
        dest.writeByte((byte) (viewed ? 1 : 0));
        dest.writeByte((byte) (favorite ? 1 : 0));
    }

    public String getBriefContent() {
        int length = content.length();
        return content.substring(0, Math.min(length, BRIEF_MIN_LENGTH)) + "...";
    }

    public String getShareContent() {
        return String.format(shareFormat, title, getBriefContent(), publishTime, publisher, link);
    }
}
