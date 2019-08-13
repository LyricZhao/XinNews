package com.example.xinnews.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    void insert(CategoryEntry category);

    @Query("DELETE FROM categories_table")
    void deleteAll();

    @Query("SELECT * from categories_table")
    LiveData<List<CategoryEntry>> getAllCategory();
}
