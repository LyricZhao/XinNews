package com.example.xinnews;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
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

    private LinearLayout mLinearLayoutForImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        final NewsEntry newsEntry = intent.getParcelableExtra(EXTRA_NEWS_INFO);

        boolean hasImage = newsEntry.hasImage();
        if (hasImage) setContentView(R.layout.news_pages_with_image);
        else setContentView(R.layout.news_pages_without_image);

        TextView mTitleTextView = findViewById(R.id.news_title);
        TextView mSubtitleTextView = findViewById(R.id.news_subtitle);
        TextView mContentTextView = findViewById(R.id.news_content);
        ImageView mShareImageView = findViewById(R.id.news_share_icon);
        TextView mShareTextView = findViewById(R.id.news_share_text);
        ImageView mFavoriteImageView = findViewById(R.id.news_favorite_icon);
        TextView mFavoriteTextView = findViewById(R.id.news_favorite_text);

        mTitleTextView.setText(newsEntry.getTitle());
        mSubtitleTextView.setText(newsEntry.generateSubtitle());
        mContentTextView.setText(newsEntry.getContent());

        View.OnClickListener clickHandler = new View.OnClickListener() {
            @SuppressLint("Assert")
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.news_share_icon:
                    case R.id.news_share_text:
                        CommonActions.share(newsEntry);
                        break;
                    case R.id.news_favorite_icon:
                    case R.id.news_favorite_text:
                        CommonActions.favorite(newsEntry);
                        break;
                    default:
                        assert false;
                }
            }
        };
        mShareTextView.setOnClickListener(clickHandler);
        mShareImageView.setOnClickListener(clickHandler);
        mFavoriteTextView.setOnClickListener(clickHandler);
        mFavoriteImageView.setOnClickListener(clickHandler);

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
