package com.example.xinnews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import androidx.core.content.FileProvider;
import com.example.xinnews.database.NewsEntry;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Bridge {
    static private final String LOG_TAG = "Bridge";
    static private final String BASE_URL = "https://api2.newsminer.net/svc/news/queryNewsList";
    static private final String ENCODING = "UTF-8";
    static private final String PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    static private final int TIME_OUT = 2500;

    static private File systemCacheDir = null;

    static void setSystemCacheDir(File path) {
        systemCacheDir = path;
    }

    static private String getUrlContent(String urlAddress) throws Exception {
        URL url = new URL(urlAddress);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(TIME_OUT);
        connection.setReadTimeout(TIME_OUT);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), ENCODING));
        String incoming;
        StringBuilder result = new StringBuilder();
        while ((incoming = bufferedReader.readLine()) != null)
            result.append(incoming);
        return result.toString();
    }

    static ArrayList<Uri> generateAllImagesUri(String newsId, Context context, int count) {
        ArrayList<Uri> imageUris = new ArrayList<>();
        for (int i = 0; i < count; ++ i) {
            File imageFile = new File(systemCacheDir, Utility.tagger(newsId, i));
            Uri imageUri = FileProvider.getUriForFile(context, PROVIDER_AUTHORITY, imageFile);
            imageUris.add(imageUri);
        }
        return imageUris;
    }

    static ArrayList<NewsEntry> getRecommendNewsEntryArray(int size, int page) throws Exception {
        if (!BehaviorTracer.hasViewedNews() || (BehaviorTracer.getNewsViewedCountLastTime() == 0 && page > 1))
            return getNewsEntryArray(size, Utility.getCurrentDate(), null, null, page);

        ArrayList<NewsEntry> recommendNews = new ArrayList<>();
        ArrayList<String> topKeywords;
        if (page == 1)
            topKeywords = BehaviorTracer.getTopKeywords();
        else
            topKeywords = BehaviorTracer.getTopKeywordsLastTime();
        for (String keyword: topKeywords)
            recommendNews.addAll(getNewsEntryArray(size / BehaviorTracer.KEYWORD_TOTAL, Utility.getCurrentDate(), keyword, null, page));
        return recommendNews;
    }

    static private Bitmap getImageFromUrl(String urlAddress) throws Exception {
        InputStream input = new URL(urlAddress).openStream();
        return BitmapFactory.decodeStream(input);
    }

    static private void saveToInternalStorage(File dir, String newsImageId, Bitmap bitmap) throws FileNotFoundException {
        File imagePath = new File(dir, newsImageId);
        FileOutputStream fileOutputStream = null;
        fileOutputStream = new FileOutputStream(imagePath);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
    }

    static private Bitmap loadImageFromStorage(File dir, String newsImageId) throws FileNotFoundException {
        Bitmap bitmap = null;
        File imagePath = new File(dir, newsImageId);
        bitmap = BitmapFactory.decodeStream(new FileInputStream(imagePath));
        return bitmap;
    }

    static Bitmap loadResourceFromPath(String newsImageId, String webPath) throws Exception {
        Bitmap bitmap = null;
        try {
            bitmap = loadImageFromStorage(systemCacheDir, newsImageId);
        } catch (FileNotFoundException exception) {
            bitmap = getImageFromUrl(webPath);
            saveToInternalStorage(systemCacheDir, newsImageId, bitmap);
        }
        return bitmap;
    }

    static private JSONArray getNewsJsonArray(int size, String endDate, String words, String categories, int page) throws Exception {
        if (size == 0) size = Utility.pageSize;
        StringBuilder queryUrl = new StringBuilder(BASE_URL + "?");
        queryUrl.append("size=").append(size);
        if (endDate != null) queryUrl.append("&endDate=").append(endDate);
        if (words != null) queryUrl.append("&words=").append(words);
        if (categories != null) queryUrl.append("&categories=").append(categories);
        queryUrl.append("&page=").append(page);
        Log.d(LOG_TAG, "Querying API: " + queryUrl.toString());
        String jsonContent = getUrlContent(queryUrl.toString());
        JSONObject globalContent = new JSONObject(jsonContent);
        return globalContent.getJSONArray("data");
    }

    static ArrayList<NewsEntry> getNewsEntryArray(int size, String endDate, String words, String categories, int page) throws Exception {
        JSONArray jsonArray = getNewsJsonArray(size, endDate, words, categories, page);
        ArrayList<NewsEntry> entries = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); ++ i) {
            NewsEntry newsEntry = new NewsEntry(jsonArray.getJSONObject(i));
            entries.add(newsEntry);
        }
        return entries;
    }
}
