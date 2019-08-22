package com.example.xinnews;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.xinnews.database.NewsEntry;

import java.util.List;

import static com.example.xinnews.NewsListAdapter.NewsItemType.NewsWithImage;
import static com.example.xinnews.NewsListAdapter.NewsItemType.NewsWithoutImage;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.NewsViewHolder> {
    public static enum NewsItemType {
        NewsWithImage,
        NewsWithoutImage
    }

    private final LayoutInflater mInflater;
    private List<NewsEntry> mNews;

    NewsListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if (NewsWithImage.ordinal() == viewType) {
            itemView = mInflater.inflate(R.layout.news_card, parent, false);
        } else if (NewsWithoutImage.ordinal() == viewType) {
            itemView = mInflater.inflate(R.layout.news_card_without_image, parent, false);
        }

        return new NewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        if (mNews != null) {
            NewsEntry current = mNews.get(position);
            holder.setView(current);
        } else {
            // TODO: no news here
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mNews.get(position).hasImage())
            return NewsWithImage.ordinal();
        return NewsWithoutImage.ordinal();
    }

    void setNews(List<NewsEntry> news) {
        mNews = news;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mNews != null)
            return mNews.size();
        return 0;
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {
        private TextView cardTitleView;
        private TextView cardCategoryView;
        private ImageView cardThumbnailView;
        private TextView cardPublisherView;
        private TextView cardTimeView;
        private TextView cardContentView;

        static private final String LOG_TAG = "NewsViewHolder";

        NewsViewHolder(View itemView) {
            super(itemView);

            cardTitleView = itemView.findViewById(R.id.news_card_title);
            cardCategoryView = itemView.findViewById(R.id.news_card_category);
            cardThumbnailView = itemView.findViewById(R.id.news_card_thumbnail);
            cardPublisherView = itemView.findViewById(R.id.news_card_publisher);
            cardTimeView = itemView.findViewById(R.id.news_card_time);
            cardContentView = itemView.findViewById(R.id.news_card_content);
        }

        void setView(NewsEntry news) {
            cardTitleView.setText(news.getTitle());
            cardCategoryView.setText(news.getCategory());
            cardTimeView.setText(news.getPublishTime());
            Log.e(LOG_TAG, news.getTitle());
            Log.e(LOG_TAG, news.toString());
            Log.e(LOG_TAG, "Setting view, bitmap size");
            if (news.hasImage()) {
                Bitmap bitmap = PicsCache.getCoverBitmap(news.getNewsId());
                cardThumbnailView.setImageBitmap(bitmap);
            }
            cardPublisherView.setText(news.getPublisher());
            cardContentView.setText(news.getContent().trim());
        }
    }
}
