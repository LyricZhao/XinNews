package com.example.xinnews;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.AndroidViewModel;
import com.example.xinnews.database.NewsDao;
import com.example.xinnews.database.NewsEntry;
import com.example.xinnews.database.NewsRoomDatabase;

import java.util.List;

public class NewsViewModel extends AndroidViewModel {
    private NewsDao newsDao;

    public NewsViewModel(Application application) {
        super(application);
        newsDao = NewsRoomDatabase.getDatabase(application).newsDao();
    }

    public void insert(NewsEntry news) {
        new insertSingleAsyncTask(newsDao).execute(news);
    }

    public List<NewsEntry> getNewsForCategory(String category) {
        return newsDao.getNewsForCategory(category);
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
