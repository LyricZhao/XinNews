package com.example.xinnews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.xinnews.database.NewsEntry;

import java.util.List;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.NewsViewHolder> {
    private final LayoutInflater mInflater;
    private List<NewsEntry> mNews;

    NewsListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.news_card, parent, false);
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
            // TODO: set thumbnail
            cardPublisherView.setText(news.getPublisher());
            cardContentView.setText(news.getContent());
        }
    }
}
