package com.chenggang.xinnews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.chenggang.xinnews.database.NewsEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.NewsViewHolder> {
    static private final int NORMAL_NEWS = 0;
    static private final int EMPTY_VIEW = 1;

    private Context mContext;
    private final LayoutInflater mInflater;
    private List<NewsEntry> mNews = new ArrayList<>();
    private MainActivity mParent;
    private Bitmap mLogo;
    private int lastCallPosition;

    NewsListAdapter(Context context, MainActivity parentActivity) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mParent = parentActivity;
        mLogo = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.logo);
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == NORMAL_NEWS) {
            itemView = mInflater.inflate(R.layout.news_card, parent, false);
        } else {
            itemView = mInflater.inflate(R.layout.no_data, parent, false);
        }
        return new NewsViewHolder(itemView);
    }

    // TODO: empty news
    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        if (mNews.size() == 0)
            return;
        final NewsEntry current = mNews.get(position);
        holder.setView(current);

        holder.getCardView().setOnClickListener(view -> {
            Intent intent = new Intent(mContext, NewsPage.class);
            intent.putExtra(NewsPage.EXTRA_NEWS_INFO, current);
            lastCallPosition = position;
            setViewed();
            mParent.callNewsPage(intent);
        });
    }

    @Override
    public int getItemViewType(int position) {
        return mNews.size() == 0 ? EMPTY_VIEW : NORMAL_NEWS;
    }

    void setNews(@NonNull List<NewsEntry> news) {
        mNews = news;
        notifyDataSetChanged();
    }

    void setViewed() {
        if (!mNews.get(lastCallPosition).getViewed()) {
            mNews.get(lastCallPosition).changeViewed();
            CommonActions.view(mNews.get(lastCallPosition));
            notifyDataSetChanged();
        }
    }

    void setFavorite(boolean favorite) {
        if (mNews.get(lastCallPosition).getFavorite() != favorite)
            mNews.get(lastCallPosition).changeFavorite();
    }

    void addNewsToEnd(@NonNull List<NewsEntry> news) {
        HashMap<String, Boolean> hashMap = new HashMap<>();
        for (NewsEntry newsEntry: mNews)
            hashMap.put(newsEntry.getNewsId(), true);
        boolean updated = false;
        for (NewsEntry newsEntry: news) if (!hashMap.containsKey(newsEntry.getNewsId())) {
            mNews.add(newsEntry);
            updated = true;
        }
        if (updated)
            notifyDataSetChanged();
    }

    void addNewsToFront(@NonNull List<NewsEntry> news) {
        HashMap<String, Boolean> hashMap = new HashMap<>();
        for (NewsEntry newsEntry: mNews)
            hashMap.put(newsEntry.getNewsId(), true);
        List<NewsEntry> newsToUpdate = new ArrayList<>();
        for (NewsEntry newsEntry: news) if (!hashMap.containsKey(newsEntry.getNewsId()))
            newsToUpdate.add(newsEntry);
        if (newsToUpdate.size() > 0) {
            newsToUpdate.addAll(mNews);
            mNews = newsToUpdate;
            notifyDataSetChanged();
        }
    }

    int getNextPage() {
        return mNews.size() / Utility.pageSize + 1;
    }

    @Override
    public int getItemCount() {
        return Math.max(mNews.size(), 1);
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {
        private TextView cardTitleView;
        private TextView cardCategoryView;
        private ImageView cardThumbnailView;
        private TextView cardPublisherView;
        private TextView cardTimeView;
        private TextView cardContentView;
        private CardView cardView;
        private ImageView cardShareButton;

        static private final String LOG_TAG = "NewsViewHolder";

        NewsViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_view);
            cardTitleView = itemView.findViewById(R.id.news_card_title);
            cardCategoryView = itemView.findViewById(R.id.news_card_category);
            cardThumbnailView = itemView.findViewById(R.id.news_card_thumbnail);
            cardPublisherView = itemView.findViewById(R.id.news_card_publisher);
            cardTimeView = itemView.findViewById(R.id.news_card_time);
            cardContentView = itemView.findViewById(R.id.news_card_content);
            cardShareButton = itemView.findViewById(R.id.news_card_share);
        }

        CardView getCardView() {
            return cardView;
        }

        @SuppressLint("StaticFieldLeak")
        private class DownloadTask extends AsyncTask<String, Void, Bitmap> {
            @Override
            protected Bitmap doInBackground(String... strings) {
                String newsId = strings[0], coverImagePath = strings[1];
                try {
                    return PicsCache.getCoverBitmap(newsId, coverImagePath);
                } catch (Exception exception) {
                    Log.e(LOG_TAG, exception.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null)
                    cardThumbnailView.setImageBitmap(bitmap);
            }
        }

        void setView(final NewsEntry news) {
            cardView.setCardBackgroundColor(mContext.getColor(news.getViewed() ? R.color.colorViewed : R.color.colorNotViewed));
            cardTitleView.setText(news.getTitle());
            cardCategoryView.setText(news.getCategory());
            cardTimeView.setText(news.getPublishTime());
            if (news.hasImage()) {
                cardThumbnailView.setVisibility(View.VISIBLE);
                try {
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(news.getNewsId(), news.getCoverImagePath());
                } catch (Exception exception) {
                    Log.e(LOG_TAG, exception.toString());
                    cardThumbnailView.setImageBitmap(mLogo);
                }
            } else {
                cardThumbnailView.setVisibility(View.GONE);
            }
            cardPublisherView.setText(news.getPublisher());
            cardContentView.setText(news.getContent().trim());
            cardShareButton.setOnClickListener(view -> CommonActions.share(news, mContext));
        }
    }
}
