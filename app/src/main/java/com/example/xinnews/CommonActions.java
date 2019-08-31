package com.example.xinnews;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.example.xinnews.database.NewsEntry;

import java.util.ArrayList;

public class CommonActions {

    static private final String WECHAT_EXTRA_TEXT = "Kdescription";
    static private final String SMS_EXTRA_TEXT = "sms_body";

    // TODO: fix the warning of EXTRA_TEXT
    static void share(NewsEntry newsEntry, Context context) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, newsEntry.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TITLE, newsEntry.getTitle());
        String shareContent = newsEntry.getShareContent();
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
        shareIntent.putExtra(WECHAT_EXTRA_TEXT, shareContent);
        shareIntent.putExtra(SMS_EXTRA_TEXT, shareContent);

        if (newsEntry.hasImage()) {
            ArrayList<Uri> imageUris = Bridge.generateAllImagesUri(newsEntry.getNewsId(), context, newsEntry.getImageCount());
            shareIntent.setType("text/plain, image/*");
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        } else {
            shareIntent.setType("text/plain");
        }
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(shareIntent);
    }

    public static boolean favorite(NewsEntry newsEntry) {
        boolean isFavorite = newsEntry.changeFavorite();
        DbBridge.update(newsEntry);
        return isFavorite;
    }
}
