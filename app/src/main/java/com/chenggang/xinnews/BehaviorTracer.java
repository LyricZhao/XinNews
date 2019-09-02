package com.chenggang.xinnews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.chenggang.xinnews.database.NewsEntry;
import org.json.JSONArray;

import java.util.*;

class BehaviorTracer {

    private static class Keyword {
        String word;
        double score;

        Keyword(String word, double score) {
            this.word = word; this.score = score;
        }
    }

    static private final String LOG_TAG = "RecommendEngine";
    static private final int KEYWORD_LIMIT = 80;
    static private final int TOP_CUT = 2;
    static final int KEYWORD_TOTAL = TOP_CUT + 1;

    static private int newsViewedCount = 0;
    static private int newsViewedCountLastTime = 0;
    static private Random random = new Random();
    static private List<Keyword> keywordsHeap = new ArrayList<>();
    static private ArrayList<String> searchHistory = new ArrayList<>();
    static private ArrayList<String> topKeywordsLastTime = new ArrayList<>();
    @SuppressLint("StaticFieldLeak")
    static private Context mContent;

    static boolean hasViewedNews() {
        return newsViewedCount > 0;
    }

    static void pushViewedNews(NewsEntry newsEntry) {
        try {
            JSONArray keywords = new JSONArray(newsEntry.getKeywords());
            HashMap<String, Double> scores = new HashMap<>();
            for (Keyword keyword: keywordsHeap)
                scores.put(keyword.word, keyword.score);
            for (int i = 0; i < keywords.length(); ++ i) {
                String word = keywords.getJSONObject(i).getString("word");
                Double score = keywords.getJSONObject(i).getDouble("score");
                if (scores.containsKey(word))
                    scores.put(word, scores.get(word) + score);
                else
                    scores.put(word, score);
            }
            ArrayList<Keyword> newList = new ArrayList<>();
            for (Map.Entry<String, Double> entry: scores.entrySet())
                newList.add(new Keyword(entry.getKey(), entry.getValue()));
            newList.sort((o1, o2) -> {
                if (o1.score == o2.score) return 0;
                return o1.score < o2.score ? 1 : -1;
            });
            keywordsHeap = newList.subList(0, Math.min(KEYWORD_LIMIT, newList.size()));
            newsViewedCount ++;
            saveSharedPreferences();
        } catch (Exception exception) {
            Log.e(LOG_TAG, exception.toString());
        }
    }

    static void setContent(Context content) {
        mContent = content;
    }

    static void loadSharedPreferences() {
        SharedPreferences keywords = mContent.getSharedPreferences("keywords", Context.MODE_PRIVATE);
        for (Map.Entry<String, ?> entry: keywords.getAll().entrySet()) {
            Float v = (Float) entry.getValue();
            keywordsHeap.add(new Keyword(entry.getKey(), v.doubleValue()));
        }
        if (keywordsHeap.size() > 0)
            newsViewedCount = 1;
    }

    private static void saveSharedPreferences() {
        SharedPreferences keywords = mContent.getSharedPreferences("keywords", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = keywords.edit();
        editor.clear();
        for (Keyword keyword: keywordsHeap)
            editor.putFloat(keyword.word, (float) keyword.score);
        editor.apply();
    }

    static ArrayList<String> getTopKeywords() {
        ArrayList<String> topKeywords = new ArrayList<>();
        int topCut = Math.min(TOP_CUT, keywordsHeap.size());
        for (int i = 0; i < topCut; ++ i)
            topKeywords.add(keywordsHeap.get(i).word);
        if (keywordsHeap.size() > topCut) {
            int pos = random.nextInt(keywordsHeap.size() - topCut) + topCut;
            topKeywords.add(keywordsHeap.get(pos).word);
        }
        topKeywordsLastTime = topKeywords;
        newsViewedCountLastTime = newsViewedCount;
        return topKeywords;
    }

    static ArrayList<String> getTopKeywordsLastTime() {
        return topKeywordsLastTime;
    }

    static int getNewsViewedCountLastTime() {
        return newsViewedCountLastTime;
    }

    static void printReadingTread() {
        for (Keyword keyword: keywordsHeap)
            Log.d(LOG_TAG, keyword.word + ": " + keyword.score);
    }

    static void addSearchHistory(String keyword) {
        searchHistory.add(0, keyword);
    }

    static boolean haveHistory() {
        return searchHistory.size() > 0;
    }

    static ArrayList<String> getSearchHistory() {
        if (searchHistory.size() == 0) {
            ArrayList<String> history = new ArrayList<>();
            history.add("无历史记录");
            return history;
        }
        return searchHistory;
    }
}
