package com.example.xinnews.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NewsDao {

    @Insert
    void insert(News news);

    @Query("DELETE FROM news_table")
    void deleteAll();

    @Query("SELECT * from news_table")
    LiveData<List<NewsEntry>> getAllNews();
}
