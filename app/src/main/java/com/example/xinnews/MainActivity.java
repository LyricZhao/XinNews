package com.example.xinnews;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.xinnews.database.NewsEntry;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView mRecyclerView;
    private final static String LOG_TAG = "MainActivity";
    private String currentCategory = null;
    private NewsListAdapter mNewsListAdapter;
    public static final int REQUEST_CODE = 701;
    private Handler UIHandler;

    NavigationView navigationView;
    Menu navigationMenu;
    boolean[] openedCategory = new boolean[Constants.allCategoriesCount];
    private List<NewsEntry> tempNewsList;

    Runnable setNewsRunnable = new Runnable() {
        @Override
        public void run() {
            mNewsListAdapter.setNews(tempNewsList);
        }
    };

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationMenu = navigationView.getMenu();
        loadCategoryPreferences();

        mRecyclerView = findViewById(R.id.recyclerview);
        mNewsListAdapter = new NewsListAdapter(this, this);
        mRecyclerView.setAdapter(mNewsListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        UIHandler = new Handler();

        Bridge.setSystemCacheDir(getApplicationContext().getCacheDir());
        DbBridge.init(getApplication());
        refreshNewsList(Constants.homePage);
    }

    /* Policy for loading news:
     *    async_task_1: load from db
     *    async_task_2: load from network
     *    async_task_2: save downloaded news to db
     */
    private void refreshNewsList(String category) {
        currentCategory = category;
        new loadNewsFromDb().execute();
    }

    private class loadNewsFromDb extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            List<NewsEntry> news = DbBridge.getNews(currentCategory);
            tempNewsList = news;
            UIHandler.post(setNewsRunnable);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            new loadNewsFromNetwork().execute();
        }
    }

    private class loadNewsFromNetwork extends AsyncTask<Void, Void, ArrayList<NewsEntry>> {

        @Override
        protected ArrayList<NewsEntry> doInBackground(Void... params) {
            try {
                String category = currentCategory;
                if (currentCategory.equals(Constants.homePage))
                    category = null;
                if (currentCategory.equals(Constants.favorite))
                    return null;
                if (currentCategory.equals(Constants.recommend))
                    return Bridge.getRecommendNewsEntryArray(15);
                return Bridge.getNewsEntryArray(15, null, null, null, category);
            } catch (Exception exception) {
                Log.e(LOG_TAG, exception.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<NewsEntry> result) {
            if (result == null)
                return;
            mNewsListAdapter.addNewsToFront(result);
            for (NewsEntry newsEntry: result)
                DbBridge.insert(newsEntry);
        }
    }

    private void loadCategoryPreferences() {
        SharedPreferences categories = getSharedPreferences("categories", Context.MODE_PRIVATE);
        if (!categories.contains("CREATED")) {
            SharedPreferences.Editor editor = categories.edit();
            editor.putBoolean("CREATED", true);
            for (String category : Constants.categories)
                editor.putBoolean(category, true);
            editor.apply();
            for (int i = 0; i < Constants.allCategoriesCount; ++i)
                openedCategory[i] = true;
        } else {
            for (int i = 0; i < Constants.allCategoriesCount; ++ i)
                openedCategory[i] = categories.getBoolean(Constants.categories[i], true);
        }
        navigationMenu.getItem(0).setChecked(true);
        for (int i = 0; i < Constants.allCategoriesCount; ++ i)
            navigationMenu.getItem(i + Constants.navigationOffset).setVisible(openedCategory[i]);
    }

    private void saveCategoryPreferences() {
        SharedPreferences categories = getSharedPreferences("categories", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = categories.edit();
        editor.putBoolean("CREATED", true);
        for (int i = 0; i < Constants.allCategoriesCount; ++ i)
            editor.putBoolean(Constants.categories[i], openedCategory[i]);
        editor.apply();
        for (int i = 0; i < Constants.allCategoriesCount; ++ i)
            navigationMenu.getItem(i + Constants.navigationOffset).setVisible(openedCategory[i]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.main_search_button) {
            callSearchPage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String title = item.getTitle().toString();
        if (title.equals(Constants.categorySettings)) {
            showCategorySectionDialog();
            return false;
        } else if (title.equals(Constants.search)) {
            // TODO: decide whether return true
            callSearchPage();
            return true;
        } else {
            refreshNewsList(title);
            DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void showCategorySectionDialog() {
        final String[] items = Constants.categories;
        final boolean choices[] = openedCategory;
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("分类管理");
        dialog.setMultiChoiceItems(items, choices, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                openedCategory[which] = isChecked;
            }
        });
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveCategoryPreferences();
            }
        });
        dialog.show();
    }

    void callSearchPage() {

    }

    void callNewsPage(Intent intent) {
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == REQUEST_CODE) {
            boolean changed = false;
            if (data != null)
                changed = data.getBooleanExtra("CHANGED", false);
            if (changed)
                refreshNewsList(currentCategory);
        }
    }
}
