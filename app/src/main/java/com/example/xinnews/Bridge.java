package com.example.xinnews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import com.example.xinnews.database.NewsEntry;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

public class Bridge {
    static private final int defaultCrawlSize = 10;
    static private final String LOG_TAG = "Bridge";
    static private final String baseUrl = "https://api2.newsminer.net/svc/news/queryNewsList";
    static private final String encoding = "UTF-8";
    static private final String[] availableCategories = {"娱乐", "军事", "教育", "文化", "健康", "财经", "体育", "汽车", "科技", "社会"};
    static private File systemCacheDir = null;

    static void setSystemCacheDir(File path) {
        systemCacheDir = path;
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

    private static String tagger(String newsId, int index) {
        return newsId + "_" + index;
    }

    public static Uri generateCoverImageUri(String newsId, Context context) {
        File coverImage = new File(systemCacheDir, tagger(newsId, 0));
        return GenericFileProvider.getUriForFile(context.getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", coverImage);
    }

    public static ArrayList<Uri> generateAllImagesUri(String newsId, Context context, int count) {
        ArrayList<Uri> imageUris = new ArrayList<>();
        Context appContext = context.getApplicationContext();
        String providerId = BuildConfig.APPLICATION_ID + ".provider";
        for (int i = 0; i < count; ++ i) {
            File imageFile = new File(systemCacheDir, tagger(newsId, i));
            Uri imageUri = GenericFileProvider.getUriForFile(appContext, providerId, imageFile);
            imageUris.add(imageUri);
        }
        return imageUris;
    }

    public static ArrayList<NewsEntry> getRecommendNewsEntryArray(int size) throws Exception {
        if (!BehaviorTracer.hasViewedNews())
            return getNewsEntryArray(size, null, null, null, null);
        ArrayList<NewsEntry> recommendNews = new ArrayList<>();
        ArrayList<String> topKeywords = BehaviorTracer.getTopKeywords();
        for (String keyword: topKeywords)
            recommendNews.addAll(getNewsEntryArray(size, null, null, keyword, null));
        return recommendNews;
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

    public static String saveToInternalStorage(File dir, String newsImageId, Bitmap bitmap) {
        File imagePath = new File(dir, newsImageId);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        } catch (Exception exception) {
            Log.e(LOG_TAG, exception.toString());
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException exception) {
                Log.e(LOG_TAG, exception.toString());
            }
        }
        return imagePath.getAbsolutePath();
    }

    public static Bitmap loadImageFromStorage(File dir, String newsImageId) throws FileNotFoundException {
        Bitmap bitmap = null;
        File imagePath = new File(dir, newsImageId);
        bitmap = BitmapFactory.decodeStream(new FileInputStream(imagePath));
        return bitmap;
    }

    public static Bitmap loadResourceFromPath(String newsImageId, String webPath) throws Exception {
        Bitmap bitmap = null;
        assert systemCacheDir!= null;
        try {
            bitmap = loadImageFromStorage(systemCacheDir, newsImageId);
        } catch (FileNotFoundException exception) {
            bitmap = getImageFromUrl(webPath);
            saveToInternalStorage(systemCacheDir, newsImageId, bitmap);
        }
        return bitmap;
    }

    private static JSONArray getNewsJsonArray(int size, String startDate, String endDate, String words, String categories) throws Exception {
        if (size == 0) size = defaultCrawlSize;
        StringBuilder queryUrl = new StringBuilder(baseUrl + "?");
        queryUrl.append("size=").append(size);
        if (startDate != null) queryUrl.append("&startDate=").append(startDate);
        if (endDate != null) queryUrl.append("&endDate=").append(endDate);
        if (words != null) queryUrl.append("&words=").append(words);
        if (categories != null) queryUrl.append("&categories=").append(categories);
        Log.d(LOG_TAG, "Querying url is " + queryUrl.toString());
        String jsonContent = getUrlContent(queryUrl.toString());
        JSONObject globalContent = new JSONObject(jsonContent);
        return globalContent.getJSONArray("data");
    }

    public static ArrayList<NewsEntry> getNewsEntryArray(int size, String startDate, String endDate, String words, String categories) throws Exception {
        JSONArray jsonArray = getNewsJsonArray(size, startDate, endDate, words, categories);
        ArrayList<NewsEntry> entries = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); ++ i) {
//            Log.d(LOG_TAG, jsonArray.getJSONObject(i).toString());
            entries.add(new NewsEntry(jsonArray.getJSONObject(i)));
        }

        return entries;
    }
}
