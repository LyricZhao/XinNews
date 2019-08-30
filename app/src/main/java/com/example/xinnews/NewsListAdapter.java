package com.example.xinnews;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.xinnews.database.NewsEntry;
import org.json.JSONException;

import java.util.List;

import static com.example.xinnews.NewsListAdapter.NewsItemType.NewsWithImage;
import static com.example.xinnews.NewsListAdapter.NewsItemType.NewsWithoutImage;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.NewsViewHolder> {
    public static enum NewsItemType {
        NewsWithImage,
        NewsWithoutImage
    }

    private Context mContext;
    private final LayoutInflater mInflater;
    private List<NewsEntry> mNews;

    NewsListAdapter(Context context) {
        mContext = context;
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

    // TODO: empty news
    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        final NewsEntry current = mNews.get(position);
        holder.setView(current);

        holder.getCardView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, NewsPage.class);
                intent.putExtra(NewsPage.EXTRA_NEWS_INFO, current);
                mContext.startActivity(intent);
            }
        });
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

    void addNewsToEnd(List<NewsEntry> news) {
        mNews.addAll(mNews);
        notifyDataSetChanged();
    }

    void addNewsToFront(List<NewsEntry> news) {
        news.addAll(mNews);
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

        private class DownloadTask extends AsyncTask<String, Void, Bitmap> {
            @Override
            protected Bitmap doInBackground(String... strings) {
                String newsId = strings[0], coverImagePath = strings[1];
                Bitmap bitmap = PicsCache.getCoverBitmap(newsId, coverImagePath);
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                cardThumbnailView.setImageBitmap(bitmap);
            }
        }

        void setView(final NewsEntry news) {
            cardTitleView.setText(news.getTitle());
            cardCategoryView.setText(news.getCategory());
            cardTimeView.setText(news.getPublishTime());
            Log.e(LOG_TAG, news.getTitle());
            Log.e(LOG_TAG, news.toString());
            Log.e(LOG_TAG, "Setting view, bitmap size");
            if (news.hasImage()) {
                try {
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(news.getNewsId(), news.getCoverImagePath());
                } catch (Exception exception) {
                    Log.e(LOG_TAG, exception.toString());
                }
            }
            cardPublisherView.setText(news.getPublisher());
            cardContentView.setText(news.getContent().trim());
            cardShareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CommonActions.share(news, mContext);
                }
            });
        }
    }
}
