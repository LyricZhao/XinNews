package com.chenggang.xinnews;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;

// TODO: limit the cache size
class PicsCache {
    static private HashMap<String, Bitmap> hashMap = new HashMap<>();
    static private final String LOG_TAG = "PicsCache";

    static private Bitmap find(String tag, String path) throws Exception {
        Bitmap bitmap = hashMap.get(tag);
        if (bitmap == null) {
            hashMap.put(tag, Bridge.loadResourceFromPath(tag, path));
            bitmap = hashMap.get(tag);
        }
        return bitmap;
    }

    static ArrayList<Bitmap> getBitmaps(String newsId, ArrayList<String> paths) throws Exception {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        for (int i = 0; i < paths.size(); ++ i) {
            String tag = Utility.tagger(newsId, i);
            bitmaps.add(find(tag, paths.get(i)));
        }
        return bitmaps;
    }

    static Bitmap getCoverBitmap(String newsId, String path) throws Exception {
        return find(Utility.tagger(newsId, 0), path);
    }
}
