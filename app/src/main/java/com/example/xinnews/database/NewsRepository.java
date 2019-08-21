package com.example.xinnews.database;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;

import java.util.List;

public class NewsRepository {
    private NewsDao mNewsDao;
    private LiveData<List<NewsEntry>> mAllNews;

    public NewsRepository(Application application) {
        NewsRoomDatabase db = NewsRoomDatabase.getDatabase(application);
        mNewsDao = db.newsDao();
        mAllNews = mNewsDao.getAllNews();
    }

    public LiveData<List<NewsEntry>> getAllNews() {
        return mAllNews;
    }

    public void insert(NewsEntry news) {
        new insertAsyncTask(mNewsDao).execute(news);
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
