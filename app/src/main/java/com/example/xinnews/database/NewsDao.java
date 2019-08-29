package com.example.xinnews.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

// TODO: finish all kinds of queries

@Dao
public interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(NewsEntry news);

    @Query("DELETE FROM news_table")
    void deleteAll();

    @Query("SELECT * FROM news_table")
    LiveData<List<NewsEntry>> getAllNews();

    @Query("SELECT * FROM news_table WHERE category LIKE :category")
    LiveData<List<NewsEntry>> getCurrentNews(String category);
}
