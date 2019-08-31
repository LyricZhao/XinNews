package com.example.xinnews.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.xinnews.Bridge;

import java.util.ArrayList;

@Database(entities = {NewsEntry.class}, version = 1, exportSchema = false)
public abstract class NewsRoomDatabase extends RoomDatabase {
    public abstract NewsDao newsDao();
    private static NewsRoomDatabase Instance;

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new PopulateDbAsync(Instance).execute();
        }
    };

    // TODO: determine the migration option
    static public NewsRoomDatabase getDatabase(final Context context) {
        if (Instance == null) {
            synchronized (NewsRoomDatabase.class) {
                if (Instance == null) {
                    Instance = Room.databaseBuilder(context.getApplicationContext(),
                            NewsRoomDatabase.class, "news_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return Instance;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private static final String LOG_TAG = "PopulateDbAsync";
        private final NewsDao mDao;

        PopulateDbAsync(NewsRoomDatabase db) {
            mDao = db.newsDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            // TODO: refresh the news list when start the app
            mDao.deleteAll();
            return null;
        }
    }
}
