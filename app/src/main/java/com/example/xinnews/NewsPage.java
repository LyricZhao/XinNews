package com.example.xinnews;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.xinnews.database.NewsEntry;

import java.util.ArrayList;

public class NewsPage extends AppCompatActivity {
    public static final String EXTRA_NEWS_INFO = "com.example.xinnews.extra.NEWS_INFO";
    private static final String LOG_TAG = "NewsPage";

    private TextView mTitleTextView;
    private TextView mSubtitleTextView;
    private TextView mContentTextView;
    private LinearLayout mLinearLayoutForImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        NewsEntry newsEntry = intent.getParcelableExtra(EXTRA_NEWS_INFO);

        boolean hasImage = newsEntry.hasImage();
        if (hasImage) setContentView(R.layout.news_pages_with_image);
        else setContentView(R.layout.news_pages_without_image);

        mTitleTextView = findViewById(R.id.news_title);
        mSubtitleTextView = findViewById(R.id.news_subtitle);
        mContentTextView = findViewById(R.id.news_content);

        mTitleTextView.setText(newsEntry.getTitle());
        mSubtitleTextView.setText(newsEntry.generateSubtitle());
        mContentTextView.setText(newsEntry.getContent());

        if (hasImage) {
            mLinearLayoutForImages = findViewById(R.id.linear_layout_for_images);
            try {
                LoadImageTask loadImageTask = new LoadImageTask();
                loadImageTask.execute(new ImageRequestWrapper(newsEntry.getNewsId(), newsEntry.getAllImagePaths()));
            } catch (Exception exception) {
                Log.e(LOG_TAG, exception.toString());
            }
        }
    }

    private class ImageRequestWrapper {
        public String newsId;
        public ArrayList<String> paths;

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
            for (Bitmap bitmap: results) {
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setImageBitmap(bitmap);
                imageView.setPadding(16, 16, 16, 16);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                mLinearLayoutForImages.addView(imageView);
            }
        }
    }
}
