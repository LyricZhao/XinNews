package com.example.xinnews;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;

// TODO: limit the cache size
public class PicsCache {
    static HashMap<String, ArrayList<Bitmap>> hashMap = new HashMap<>();

    public static void add(String newsId, Bitmap bitmap) {
        ArrayList<Bitmap> arrayList = hashMap.get(newsId);
        if (arrayList == null)
            hashMap.put(newsId, arrayList = new ArrayList<Bitmap>());
        arrayList.add(bitmap);
    }

    public static ArrayList<Bitmap> getBitmaps(String newsId) {
        return hashMap.get(newsId);
    }

    public static Bitmap getCoverBitmap(String newsId) {
        ArrayList<Bitmap> arrayList = hashMap.get(newsId);
        if (arrayList != null)
            return arrayList.get(0);
        return null;
    }
}
