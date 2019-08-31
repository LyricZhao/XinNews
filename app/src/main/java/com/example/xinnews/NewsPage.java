package com.example.xinnews;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import com.example.xinnews.database.NewsEntry;

import java.util.ArrayList;

public class NewsPage extends AppCompatActivity {
    static final String EXTRA_NEWS_INFO = BuildConfig.APPLICATION_ID + ".extra.NEWS_INFO";
    static private final String LOG_TAG = "NewsPage";

    private LinearLayout mLinearLayoutForImages;
    private HorizontalScrollView mHorizontalScrollView;
    private boolean favoriteChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final NewsEntry newsEntry = intent.getParcelableExtra(EXTRA_NEWS_INFO);

        boolean hasImage = newsEntry.hasImage();
        setContentView(R.layout.news_page);

        TextView mTitleTextView = findViewById(R.id.news_title);
        TextView mSubtitleTextView = findViewById(R.id.news_subtitle);
        TextView mContentTextView = findViewById(R.id.news_content);
        Button mShareButton = findViewById(R.id.news_button_share);
        final Button mFavoriteButton = findViewById(R.id.news_button_favorite);
        if (newsEntry.getFavorite())
            mFavoriteButton.setText(R.string.button_cancel_favorite);

        mTitleTextView.setText(newsEntry.getTitle());
        mSubtitleTextView.setText(newsEntry.getSubtitle());
        mContentTextView.setText(newsEntry.getContent());

        View.OnClickListener clickHandler = view -> {
            switch (view.getId()) {
                case R.id.news_button_share:
                    CommonActions.share(newsEntry, getApplicationContext());
                    break;
                case R.id.news_button_favorite:
                    boolean favorite = CommonActions.favorite(newsEntry);
                    if (favorite)
                        mFavoriteButton.setText(R.string.button_cancel_favorite);
                    else
                        mFavoriteButton.setText(R.string.button_favorite);
                    if (!favoriteChanged) {
                        favoriteChanged = true;
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("CHANGED", true);
                        setResult(MainActivity.REQUEST_CODE, resultIntent);
                    }
                    break;
            }
        };
        mShareButton.setOnClickListener(clickHandler);
        mFavoriteButton.setOnClickListener(clickHandler);

        if (hasImage) {
            mHorizontalScrollView = findViewById(R.id.images_scroll_view);
            mLinearLayoutForImages = findViewById(R.id.linear_layout_for_images);
            try {
                LoadImageTask loadImageTask = new LoadImageTask();
                loadImageTask.execute(new ImageRequestWrapper(newsEntry.getNewsId(), newsEntry.getAllImagePaths()));
            } catch (Exception exception) {
                Log.e(LOG_TAG, exception.toString());
            }
        }
        BehaviorTracer.pushViewedNews(newsEntry);
//        BehaviorTracer.printReadingTread();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()== android.R.id.home) {
            Intent intent = NavUtils.getParentActivityIntent(this);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            NavUtils.navigateUpTo(this, intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static private class ImageRequestWrapper {
        String newsId;
        ArrayList<String> paths;

        ImageRequestWrapper(String newsId, ArrayList<String> paths) {
            this.newsId = newsId;
            this.paths = paths;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadImageTask extends AsyncTask<ImageRequestWrapper, Void, ArrayList<Bitmap>> {

        @Override
        protected ArrayList<Bitmap> doInBackground(ImageRequestWrapper... imageRequestWrappers) {
            String newsId = imageRequestWrappers[0].newsId;
            ArrayList<String> paths = imageRequestWrappers[0].paths;
            try {
                return PicsCache.getBitmaps(newsId, paths);
            } catch (Exception exception) {
                Log.e(LOG_TAG, exception.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> results) {
            if (results == null) {
                Toast.makeText(getApplicationContext(), "图片加载失败", Toast.LENGTH_SHORT).show();
                return;
            }
            mHorizontalScrollView.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            for (Bitmap bitmap: results) {
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setLayoutParams(layoutParams);
                imageView.setImageBitmap(bitmap);
                imageView.setPadding(8, 8, 8, 8);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                mLinearLayoutForImages.addView(imageView);
            }
        }
    }
}
