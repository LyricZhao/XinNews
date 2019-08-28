package com.example.xinnews;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

// TODO: limit the cache size and add new policy for caching
// Mapping: id + index to map
public class PicsCache {
    static HashMap<String, Bitmap> hashMap = new HashMap<>();
    private static final String LOG_TAG = "PicsCache";

    private static String tagger(String newsId, int index) {
        return newsId + "_" + index;
    }

    private static Bitmap find(String tag, String path) {
        Bitmap bitmap = hashMap.get(tag);
        if (bitmap == null) {
            try {
                hashMap.put(tag, Bridge.loadResourceFromPath(path));
                bitmap = hashMap.get(tag);
            } catch (Exception exception) {
                exception.printStackTrace();
                return null;
            }
        }
        return bitmap;
    }

    public static ArrayList<Bitmap> getBitmaps(String newsId, ArrayList<String> paths) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        for (int i = 0; i < paths.size(); ++ i) {
            String tag = tagger(newsId, i);
            bitmaps.add(find(tag, paths.get(i)));
        }
        return bitmaps;
    }

    public static Bitmap getCoverBitmap(String newsId, String path) {
        return find(tagger(newsId, 0), path);
    }
}
