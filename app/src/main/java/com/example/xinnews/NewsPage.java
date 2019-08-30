package com.example.xinnews;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.AsyncTask;
import android.provider.ContactsContract;
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
    public static final String EXTRA_NEWS_INFO = "com.example.xinnews.extra.NEWS_INFO";
    private static final String LOG_TAG = "NewsPage";

    private LinearLayout mLinearLayoutForImages;
    private boolean favoriteChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final NewsEntry newsEntry = intent.getParcelableExtra(EXTRA_NEWS_INFO);

        boolean hasImage = newsEntry.hasImage();
        if (hasImage) setContentView(R.layout.news_pages_with_image);
        else setContentView(R.layout.news_pages_without_image);

        TextView mTitleTextView = findViewById(R.id.news_title);
        TextView mSubtitleTextView = findViewById(R.id.news_subtitle);
        TextView mContentTextView = findViewById(R.id.news_content);
        Button mShareButton = findViewById(R.id.news_button_share);
        final Button mFavoriteButton = findViewById(R.id.news_button_favorite);
        if (newsEntry.getFavorite())
            mFavoriteButton.setText(R.string.button_cancel_favorite);

        mTitleTextView.setText(newsEntry.getTitle());
        mSubtitleTextView.setText(newsEntry.generateSubtitle());
        mContentTextView.setText(newsEntry.getContent());

        View.OnClickListener clickHandler = new View.OnClickListener() {
            @SuppressLint("Assert")
            @Override
            public void onClick(View view) {
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
                    default:
                        assert false;
                }
            }
        };
        mShareButton.setOnClickListener(clickHandler);
        mFavoriteButton.setOnClickListener(clickHandler);

        if (hasImage) {
            mLinearLayoutForImages = findViewById(R.id.linear_layout_for_images);
            try {
                LoadImageTask loadImageTask = new LoadImageTask();
                loadImageTask.execute(new ImageRequestWrapper(newsEntry.getNewsId(), newsEntry.getAllImagePaths()));
            } catch (Exception exception) {
                Log.e(LOG_TAG, exception.toString());
            }
        }
        RecommendEngine.pushViewedNews(newsEntry);
        RecommendEngine.printStatus();
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

    private class ImageRequestWrapper {
        String newsId;
        ArrayList<String> paths;

        ImageRequestWrapper(String newsId, ArrayList<String> paths) {
            this.newsId = newsId;
            this.paths = paths;
        }
    }

    private class LoadImageTask extends AsyncTask<ImageRequestWrapper, Void, ArrayList<Bitmap>> {

        @Override
        protected ArrayList<Bitmap> doInBackground(ImageRequestWrapper... imageRequestWrappers) {
            String newsId = imageRequestWrappers[0].newsId;
            ArrayList<String> paths = imageRequestWrappers[0].paths;
            return PicsCache.getBitmaps(newsId, paths);
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> results) {
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
