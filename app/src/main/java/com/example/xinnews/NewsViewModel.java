package com.example.xinnews;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.xinnews.database.NewsEntry;
import com.example.xinnews.database.NewsRepository;

import java.util.List;

public class NewsViewModel extends AndroidViewModel {
    private NewsRepository mRepository;
    private LiveData<List<NewsEntry>> mAllNews;

    public NewsViewModel(Application application) {
        super(application);
        mRepository = new NewsRepository(application);
        mAllNews = mRepository.getAllNews();
    }

    LiveData<List<NewsEntry>> getAllNews() {
        return mAllNews;
    }

    public void insert(NewsEntry news) {
        mRepository.insert(news);
    }
}
