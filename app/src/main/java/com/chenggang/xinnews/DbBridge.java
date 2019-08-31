package com.chenggang.xinnews;

import android.app.Application;
import android.os.AsyncTask;
import com.chenggang.xinnews.database.NewsDao;
import com.chenggang.xinnews.database.NewsEntry;
import com.chenggang.xinnews.database.NewsRoomDatabase;

import java.util.ArrayList;
import java.util.List;

class DbBridge {
    static private NewsDao newsDao;
    static private final String LOG_TAG = "DbBridge";

    static void init(Application application) {
        newsDao = NewsRoomDatabase.getDatabase(application).newsDao();
    }

    static void insert(NewsEntry news) {
        new insertSingleAsyncTask(newsDao).execute(news);
    }

    static void update(NewsEntry news) {
        new updateSingleAsyncTask(newsDao).execute(news);
    }

    static List<NewsEntry> getNews(String category) {
        if (category.equals(Utility.homePage))
            return newsDao.getAllNews();
        if (category.equals(Utility.favorite))
            return newsDao.getFavoriteNews();
        if (category.equals(Utility.recommend))
            return new ArrayList<>();
        if (category.equals(Utility.search))
            return new ArrayList<>();
        return newsDao.getNewsForCategory(category);
    }

    private static class updateSingleAsyncTask extends AsyncTask<NewsEntry, Void, Void> {
        private NewsDao mAsyncTaskDao;

        updateSingleAsyncTask(NewsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final NewsEntry... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class insertSingleAsyncTask extends AsyncTask<NewsEntry, Void, Void> {
        private NewsDao mAsyncTaskDao;

        insertSingleAsyncTask(NewsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final NewsEntry... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
