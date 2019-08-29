package com.example.xinnews.database;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;
import androidx.lifecycle.LiveData;

import java.util.List;

public class NewsRepository {
    static private final String LOG_TAG = "NewsRepository";
    private NewsDao mNewsDao;
    private LiveData<List<NewsEntry>> mAllNews;
    private LiveData<List<NewsEntry>> mCurrentNews;
    private String currentCategory;

    public NewsRepository(Application application) {
        NewsRoomDatabase db = NewsRoomDatabase.getDatabase(application);
        currentCategory = "科技";
        mNewsDao = db.newsDao();
        mAllNews = mNewsDao.getAllNews();
        mCurrentNews = mNewsDao.getCurrentNews(currentCategory);
    }

    public LiveData<List<NewsEntry>> getAllNews() {
        return mAllNews;
    }

    public LiveData<List<NewsEntry>> getCurrentNews(String category) {
        return mCurrentNews;
    }

    public void insert(NewsEntry news) {
        new insertAsyncTask(mNewsDao).execute(news);
    }

    public void setCurrentCategory(String category) {
        currentCategory = category;
        mCurrentNews = mNewsDao.getCurrentNews(currentCategory);
    }

    private static class insertAsyncTask extends AsyncTask<NewsEntry, Void, Void> {
        private NewsDao mAsyncTaskDao;

        insertAsyncTask(NewsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final NewsEntry... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
