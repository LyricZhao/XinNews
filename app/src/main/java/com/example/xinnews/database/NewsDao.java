package com.example.xinnews.database;

import androidx.room.*;

import java.util.List;

@Dao
public interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(NewsEntry news);

    @Query("DELETE FROM news_table")
    void deleteAll();

    @Query("SELECT * FROM news_table")
    List<NewsEntry> getAllNews();

    @Query("SELECT * FROM news_table WHERE category LIKE :category")
    List<NewsEntry> getNewsForCategory(String category);

    @Query("SELECT * FROM news_table WHERE favorite = 1")
    List<NewsEntry> getFavoriteNews();

    @Update
    void update(NewsEntry... newsEntries);
}
