package com.chenggang.xinnews;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chenggang.xinnews.database.NewsEntry;
import com.google.android.material.navigation.NavigationView;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    static private final String LOG_TAG = "MainActivity";
    static final int REQUEST_CODE = 701;

    private RefreshLayout mRefreshLayout;
    private NewsListAdapter mNewsListAdapter;
    private Menu mNavigationMenu;
    private RecyclerView mRecyclerView;

    private String currentCategory;
    private String currentKeyword;
    private Handler UIHandler;
    private boolean[] openedCategory = new boolean[Utility.allCategoriesCount];
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationMenu = mNavigationView.getMenu();
        loadCategoryPreferences();

        mRecyclerView = findViewById(R.id.recyclerview);
        mNewsListAdapter = new NewsListAdapter(this, this);
        mRecyclerView.setAdapter(mNewsListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        UIHandler = new Handler();

        Bridge.setSystemCacheDir(getCacheDir());
        DbBridge.init(getApplication());
        refreshNewsList(Utility.homePage);

        mRefreshLayout = findViewById(R.id.refresh_layout);
        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setRefreshHeader(new MaterialHeader(this));
        mRefreshLayout.setOnRefreshListener(refreshLayout -> {
            isLoading = true;
            new loadNewsFromNetwork().execute();
        });
        mRefreshLayout.setOnLoadMoreListener(refreshLayout -> {
            isLoading = true;
            new loadMoreNewsFromNetwork().execute();
        });
        BehaviorTracer.setContent(this);
        BehaviorTracer.loadSharedPreferences();
    }

    /* Policy for loading news:
     *    Step 1: load from db
     *    Step 2.1: load from network
     *    Step 3.1: save downloaded news to db
     */
    private void refreshNewsList(String category) {
        currentCategory = category;
        new loadNewsFromDb().execute();
    }

    private void postErrorAndFinishRefreshAndLoadMore() {
        UIHandler.post(() -> {
            Toast.makeText(MainActivity.this, "网络或文件系统错误", Toast.LENGTH_SHORT).show();
            isLoading = false;
            mRefreshLayout.finishRefresh();
            mRefreshLayout.finishLoadMore();
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class loadNewsFromDb extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            List<NewsEntry> news = DbBridge.getNews(currentCategory);
            UIHandler.post(() -> {
                mNewsListAdapter.setNews(news);
                mRecyclerView.scrollToPosition(0);
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!currentCategory.equals(Utility.favorite))
                new loadNewsFromNetwork().execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class loadNewsFromNetwork extends AsyncTask<Void, Void, ArrayList<NewsEntry>> {

        @Override
        protected void onPreExecute() {
            if (!isLoading)
                UIHandler.post(() -> mRefreshLayout.autoRefresh());
        }

        @SuppressLint("Assert")
        @Override
        protected ArrayList<NewsEntry> doInBackground(Void... params) {
            try {
                String category = currentCategory;
                if (currentCategory.equals(Utility.homePage))
                    category = null;
                if (currentCategory.equals(Utility.recommend))
                    return Bridge.getRecommendNewsEntryArray(1);
                if (currentCategory.equals(Utility.search))
                    return Bridge.getNewsEntryArray(Utility.pageSize, null, currentKeyword, null, 1);
                assert !currentCategory.equals(Utility.favorite);
                return Bridge.getNewsEntryArray(Utility.pageSize, Utility.getCurrentDate(), null, category, 1);
            } catch (Exception exception) {
                Log.e(LOG_TAG, exception.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<NewsEntry> result) {
            if (result == null) {
                postErrorAndFinishRefreshAndLoadMore();
                return;
            }
            UIHandler.post(() -> {
                mNewsListAdapter.addNewsToFront(result);
                isLoading = false;
                mRefreshLayout.finishRefresh();
                mRecyclerView.scrollToPosition(0);
            });
            for (NewsEntry newsEntry: result)
                DbBridge.insert(newsEntry);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class loadMoreNewsFromNetwork extends AsyncTask<Void, Void, ArrayList<NewsEntry>> {

        @SuppressLint("Assert")
        @Override
        protected ArrayList<NewsEntry> doInBackground(Void... params) {
            try {
                String category = currentCategory;
                if (currentCategory.equals(Utility.homePage))
                    category = null;
                if (currentCategory.equals(Utility.recommend))
                    return Bridge.getRecommendNewsEntryArray(mNewsListAdapter.getNextPage());
                if (currentCategory.equals(Utility.search))
                    return Bridge.getNewsEntryArray(Utility.pageSize, null, currentKeyword, null, mNewsListAdapter.getNextPage());
                assert !currentCategory.equals(Utility.favorite);
                return Bridge.getNewsEntryArray(Utility.pageSize, Utility.getCurrentDate(), null, category, mNewsListAdapter.getNextPage());
            } catch (Exception exception) {
                Log.e(LOG_TAG, exception.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<NewsEntry> result) {
            if (result == null) {
                postErrorAndFinishRefreshAndLoadMore();
                return;
            }
            UIHandler.post(() -> {
                mNewsListAdapter.addNewsToEnd(result);
                isLoading = false;
                mRefreshLayout.finishLoadMore();
            });
            for (NewsEntry newsEntry: result)
                DbBridge.insert(newsEntry);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class loadSearchNewsFromNetwork extends AsyncTask<String, Void, ArrayList<NewsEntry>> {

        @Override
        protected ArrayList<NewsEntry> doInBackground(String... params) {
            try {
                String keyword = params[0];
                return Bridge.getNewsEntryArray(Utility.pageSize, null, keyword, null, 1);
            } catch (Exception exception) {
                Log.e(LOG_TAG, exception.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<NewsEntry> result) {
            if (result == null) {
                postErrorAndFinishRefreshAndLoadMore();
                return;
            }
            UIHandler.post(() -> {
                mNewsListAdapter.setNews(result);
                mRecyclerView.scrollToPosition(0);
            });
            for (NewsEntry newsEntry: result)
                DbBridge.insert(newsEntry);
        }
    }

    private void loadCategoryPreferences() {
        SharedPreferences categories = getSharedPreferences("categories", Context.MODE_PRIVATE);
        if (!categories.contains("CREATED")) {
            SharedPreferences.Editor editor = categories.edit();
            editor.putBoolean("CREATED", true);
            for (String category : Utility.categories)
                editor.putBoolean(category, true);
            editor.apply();
            for (int i = 0; i < Utility.allCategoriesCount; ++i)
                openedCategory[i] = true;
        } else {
            for (int i = 0; i < Utility.allCategoriesCount; ++ i)
                openedCategory[i] = categories.getBoolean(Utility.categories[i], true);
        }
        mNavigationMenu.getItem(0).setChecked(true);
        for (int i = 0; i < Utility.allCategoriesCount; ++ i)
            mNavigationMenu.getItem(i + Utility.navigationOffset).setVisible(openedCategory[i]);
    }

    private void saveCategoryPreferences() {
        SharedPreferences categories = getSharedPreferences("categories", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = categories.edit();
        editor.putBoolean("CREATED", true);
        for (int i = 0; i < Utility.allCategoriesCount; ++ i)
            editor.putBoolean(Utility.categories[i], openedCategory[i]);
        editor.apply();
        for (int i = 0; i < Utility.allCategoriesCount; ++ i)
            mNavigationMenu.getItem(i + Utility.navigationOffset).setVisible(openedCategory[i]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
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

    private void setRefreshSettings(String category) {
        mRefreshLayout.setEnableRefresh(!category.equals(Utility.favorite));
        mRefreshLayout.setEnableLoadMore(!category.equals(Utility.favorite));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String title = item.getTitle().toString();
        if (isLoading) return false;

        setRefreshSettings(title);
        if (title.equals(Utility.categorySettings)) {
            showCategorySectionDialog();
            return false;
        } else if (title.equals(Utility.search)) {
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
        final String[] items = Utility.categories;
        final boolean[] choices = openedCategory;
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("分类管理");
        dialog.setMultiChoiceItems(items, choices, (dialog1, which, isChecked) -> openedCategory[which] = isChecked);
        dialog.setPositiveButton("OK", (dialog12, which) -> saveCategoryPreferences());
        dialog.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void callSearchPage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.search_dialog, null);
        final Button confirmButton = view.findViewById(R.id.search_confirm_button);
        final Button cancelButton = view.findViewById(R.id.search_cancel_button);
        final ListView listView = view.findViewById(R.id.search_history_list);
        final EditText editText = view.findViewById(R.id.search_edit_text);

        final ArrayList<String> history = BehaviorTracer.getSearchHistory();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, history);
        listView.setAdapter(adapter);
        if (BehaviorTracer.haveHistory()) {
            listView.setOnItemClickListener((parent, view1, position, id) -> editText.setText(history.get(position)));
        }

        final Dialog searchDialog = builder.create();
        searchDialog.show();
        Objects.requireNonNull(searchDialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        searchDialog.getWindow().setContentView(view);
        confirmButton.setOnClickListener(v -> {
            String input = editText.getText().toString();
            if (input.length() > 0) {
                isLoading = true;
                mRefreshLayout.autoRefresh();
                String keyword = editText.getText().toString();
                currentCategory = Utility.search;
                currentKeyword = keyword;
                setRefreshSettings(currentCategory);
                new loadSearchNewsFromNetwork().execute(keyword);
                searchDialog.dismiss();
                mNavigationMenu.getItem(Utility.searchId).setChecked(true);
                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
                BehaviorTracer.addSearchHistory(keyword);
            }
        });
        cancelButton.setOnClickListener(v -> searchDialog.dismiss());
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
            if (changed) {
                if (data.hasExtra("FAVORITE")) {
                    if (currentCategory.equals(Utility.favorite))
                        refreshNewsList(currentCategory);
                    else
                        mNewsListAdapter.setFavorite(data.getBooleanExtra("FAVORITE", true));
                }
            }
        }
    }
}
