package com.example.xinnews.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories_table")
public class CategoryEntry {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "keyword")
    private String keyword;

    CategoryEntry(String name, String category, String keyword) {
        this.name = name;
        this.category = category;
        this.keyword = keyword;
    }
}
