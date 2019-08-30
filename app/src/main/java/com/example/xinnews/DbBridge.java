package com.example.xinnews;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;
import com.example.xinnews.database.NewsDao;
import com.example.xinnews.database.NewsEntry;
import com.example.xinnews.database.NewsRoomDatabase;

import java.util.List;

public class DbBridge {
    private static NewsDao newsDao;
    private static final String LOG_TAG = "NewsViewModel";

    public static void init(Application application) {
        newsDao = NewsRoomDatabase.getDatabase(application).newsDao();
    }

    public static void insert(NewsEntry news) {
        new insertSingleAsyncTask(newsDao).execute(news);
    }

    public static void update(NewsEntry news) {
        new updateSingleAsyncTask(newsDao).execute(news);
    }

    public static List<NewsEntry> getNews(String category) {
        if (category.equals(Constants.homePage))
            return newsDao.getAllNews();
        if (category.equals(Constants.favorite))
            return newsDao.getFavoriteNews();
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
