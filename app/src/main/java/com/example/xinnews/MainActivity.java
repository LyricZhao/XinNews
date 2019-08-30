package com.example.xinnews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.xinnews.database.NewsEntry;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NewsViewModel mNewsViewModel;
    private RecyclerView mRecyclerView;
    private final static String LOG_TAG = "MainActivity";
    private String currentCategory = null;
    private NewsListAdapter mNewsListAdapter;

    NavigationView navigationView;
    Menu navigationMenu;
    boolean[] openedCategory = new boolean[Constants.allCategoriesCount];

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bridge.setSystemCacheDir(getApplicationContext().getCacheDir());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationMenu = navigationView.getMenu();
        loadCategoryPreferences();

        mRecyclerView = findViewById(R.id.recyclerview);
        mNewsListAdapter = new NewsListAdapter(this);
        mRecyclerView.setAdapter(mNewsListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mNewsViewModel = ViewModelProviders.of(this).get(NewsViewModel.class);

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
            List<NewsEntry> news = mNewsViewModel.getNewsForCategory(currentCategory);
            mNewsListAdapter.setNews(news);
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
                return Bridge.getNewsEntryArray(15, null, null, null, category);
            } catch (Exception exception) {
                Log.e(LOG_TAG, exception.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<NewsEntry> result) {
            mNewsListAdapter.addNewsToFront(result);
            for (NewsEntry newsEntry: result)
                mNewsViewModel.insert(newsEntry);
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
}
