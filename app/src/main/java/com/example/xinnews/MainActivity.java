package com.example.xinnews;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.xinnews.database.NewsEntry;
import com.google.android.material.navigation.NavigationView;

import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NewsViewModel mNewsViewModel;
    private RecyclerView mRecyclerView;
    private final static String LOG_TAG = "MainActivity";

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

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        loadCategoryPreferences(navigationView.getMenu());

        mRecyclerView = findViewById(R.id.recyclerview);
        final NewsListAdapter adapter = new NewsListAdapter(this);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mNewsViewModel = ViewModelProviders.of(this).get(NewsViewModel.class);
        mNewsViewModel.getCurrentNews().observe(this, new Observer<List<NewsEntry>>() {
            @Override
            public void onChanged(List<NewsEntry> newsEntries) {
                adapter.setNews(newsEntries);
            }
        });
    }

    private void loadCategoryPreferences(Menu menu) {
        SharedPreferences categories = getSharedPreferences("categories", Context.MODE_PRIVATE);
        boolean[] opened = new boolean[Constants.allCategoriesCount];
        if (!categories.contains("CREATED")) {
            SharedPreferences.Editor editor = categories.edit();
            editor.putBoolean("CREATED", true);
            for (String category : Constants.categories)
                editor.putBoolean(category, true);
            editor.apply();
            for (int i = 0; i < Constants.allCategoriesCount; ++i)
                opened[i] = true;
        } else {
            for (int i = 0; i < Constants.allCategoriesCount; ++ i) {
                opened[i] = categories.getBoolean(Constants.categories[i], true);
            }
        }
        menu.getItem(0).setChecked(true);
        for (int i = 0; i < Constants.allCategoriesCount; ++ i) {
            menu.getItem(i + 1).setVisible(opened[i]);
        }
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
        mNewsViewModel.setCurrentCategory(title);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
