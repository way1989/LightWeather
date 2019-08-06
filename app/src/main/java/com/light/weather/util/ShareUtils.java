package com.light.weather.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * Created by liyu on 2016/11/17.
 */

public class ShareUtils {

    public static void shareText(Context context, String extraText, String title) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, extraText);
        context.startActivity(Intent.createChooser(intent, title));
    }

    public static void shareImage(Context context, Uri uri, String title) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(Intent.createChooser(shareIntent, "share to"));
    }

    public static void shareImage(Context context, File file, String title) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        shareIntent.setType("image/jpeg");
        context.startActivity(Intent.createChooser(shareIntent, title));
    }
}
