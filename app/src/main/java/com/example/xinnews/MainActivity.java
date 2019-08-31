package com.example.xinnews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.xinnews.database.NewsEntry;
import com.google.android.material.navigation.NavigationView;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView mRecyclerView;
    private final static String LOG_TAG = "MainActivity";
    private String currentCategory = null;
    private NewsListAdapter mNewsListAdapter;
    public static final int REQUEST_CODE = 701;
    private Handler UIHandler;
    RefreshLayout mRefreshLayout;

    NavigationView navigationView;
    Menu navigationMenu;
    boolean[] openedCategory = new boolean[Constants.allCategoriesCount];

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

        mRefreshLayout = findViewById(R.id.refresh_layout);
        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setRefreshHeader(new MaterialHeader(this));
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                new loadNewsFromNetwork().execute();
            }
        });
        mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                new loadMoreNewsFromNetwork().execute();
            }
        });
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
            UIHandler.post(() -> mNewsListAdapter.setNews(news));
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
                    return Bridge.getRecommendNewsEntryArray(Constants.pageSize, 1);
                return Bridge.getNewsEntryArray(Constants.pageSize, null, Constants.getCurrentDate(), null, category, 1);
            } catch (Exception exception) {
                Log.e(LOG_TAG, exception.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<NewsEntry> result) {
            UIHandler.post(() -> {
                mNewsListAdapter.addNewsToFront(result);
                mRefreshLayout.finishRefresh();
            });
            if (result == null)
                return;
            for (NewsEntry newsEntry: result)
                DbBridge.insert(newsEntry);
        }
    }

    private class loadMoreNewsFromNetwork extends AsyncTask<Void, Void, ArrayList<NewsEntry>> {

        @Override
        protected ArrayList<NewsEntry> doInBackground(Void... params) {
            try {
                String category = currentCategory;
                if (currentCategory.equals(Constants.homePage))
                    category = null;
                if (currentCategory.equals(Constants.favorite))
                    return null;
                if (currentCategory.equals(Constants.recommend))
                    return Bridge.getRecommendNewsEntryArray(Constants.pageSize, mNewsListAdapter.getNextPage());
                return Bridge.getNewsEntryArray(Constants.pageSize, null, Constants.getCurrentDate(), null, category, mNewsListAdapter.getNextPage());
            } catch (Exception exception) {
                Log.e(LOG_TAG, exception.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<NewsEntry> result) {
            UIHandler.post(() -> {
                mNewsListAdapter.addNewsToEnd(result);
                mRefreshLayout.finishLoadMore();
            });
            if (result == null)
                return;
            for (NewsEntry newsEntry: result)
                DbBridge.insert(newsEntry);
        }
    }

    private class loadSearchNewsFromNetwork extends AsyncTask<String, Void, ArrayList<NewsEntry>> {

        @Override
        protected ArrayList<NewsEntry> doInBackground(String... params) {
            try {
                String keyword = params[0];
                return Bridge.getNewsEntryArray(30, null, null, keyword, null, 1);
            } catch (Exception exception) {
                Log.e(LOG_TAG, exception.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<NewsEntry> result) {
            if (result == null)
                return;
            UIHandler.post(() -> mNewsListAdapter.setNews(result));
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
            callSearchPage();
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

    void callSearchPage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View view = inflater.inflate(R.layout.search_dialog, null);
        final Button confirmButton = view.findViewById(R.id.search_confirm_button);
        final Button cancelButton = view.findViewById(R.id.search_cancel_button);
        final ListView listView = view.findViewById(R.id.search_history_list);
        final EditText editText = view.findViewById(R.id.search_edit_text);

        ArrayList<String> history = BehaviorTracer.getSearchHistory();
        if (history.size() == 0) {
            history = new ArrayList<>();
            history.add("无历史记录");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, history);
        listView.setAdapter(adapter);

        final Dialog searchDialog = builder.create();
        searchDialog.show();
        searchDialog.getWindow().setContentView(view);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = editText.getText().toString();
                if (input.length() > 0) {
                    String keyword = editText.getText().toString();
                    new loadSearchNewsFromNetwork().execute(keyword);
                    searchDialog.dismiss();
                    navigationMenu.getItem(Constants.searchId).setChecked(true);
                    DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    BehaviorTracer.addSearchHistory(keyword);
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDialog.dismiss();
            }
        });
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
