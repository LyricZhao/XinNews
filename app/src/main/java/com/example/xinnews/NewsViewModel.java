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
    private LiveData<List<NewsEntry>> mCurrentNews;
    private String currentCategory;

    public NewsViewModel(Application application) {
        super(application);
        mRepository = new NewsRepository(application);
        mAllNews = mRepository.getAllNews();
        currentCategory = "科技";
        mCurrentNews = mRepository.getCurrentNews(currentCategory);
    }

    LiveData<List<NewsEntry>> getAllNews() {
        return mAllNews;
    }

    LiveData<List<NewsEntry>> getCurrentNews() {
        return mCurrentNews;
    }

    public NewsEntry getNews(int position) {
        return mAllNews.getValue().get(position);
    }

    public void setCurrentCategory(String category) {
        currentCategory = category;
        mRepository.setCurrentCategory(currentCategory);
    }

    public void insert(NewsEntry news) {
        mRepository.insert(news);
    }
}
