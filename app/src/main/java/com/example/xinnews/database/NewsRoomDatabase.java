package com.example.xinnews.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {NewsEntry.class}, version = 1, exportSchema = false)
public abstract class NewsRoomDatabase extends RoomDatabase {
    public abstract NewsDao newsDao();
    private static NewsRoomDatabase Instance;

    static NewsRoomDatabase getDatabase(final Context context) {
        if (Instance == null) {
            synchronized (NewsRoomDatabase.class) {
                if (Instance == null) {
                    Instance = Room.databaseBuilder(context.getApplicationContext(),
                            NewsRoomDatabase.class, "news_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return Instance;
    }
}
