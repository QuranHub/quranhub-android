package app.quranhub.util;

import android.content.Context;
import android.content.Intent;

import app.quranhub.R;

public class IntentUtils {

    public static Intent getShareIntent(String ayaText, Context context) {
        String shareBody = ayaText + "\n\n" + context.getString(R.string.app_name) + ".";
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getString(R.string.aya));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        return Intent.createChooser(sharingIntent, context.getResources().getString(R.string.share_using));
    }
}
