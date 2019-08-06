package com.light.weather.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class UiUtil {
    private static final String TAG = "UiUtil";

    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static float px2dip(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (px / scale);
    }

    /**
     * sp转px
     *
     * @param context 上下文
     * @param spValue sp值
     * @return px值
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static Drawable zoomDrawable(Drawable drawable, int w, int h) {
        if (drawable == null || w < 0 || h < 0)
            return null;
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        // drawable转换成bitmap
        Bitmap oldbmp = drawableToBitmap(drawable);
        // 创建操作图片用的Matrix对象
        Matrix matrix = new Matrix();
        // 计算缩放比例
        float sx = ((float) w / width);
        float sy = ((float) h / height);
        // 设置缩放比例
        matrix.postScale(sx, sy);
        // 建立新的bitmap，其内容是对原bitmap的缩放后的图
        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);
        return new BitmapDrawable(newbmp);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }

    public static int getStatusBarHeight() {
        final Resources res = Resources.getSystem();
        int id = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android");
        if (id > 0) {
            return res.getDimensionPixelSize(id);
        }
        return 0;
    }

    public static int getActionBarHeight() {
        final Resources res = Resources.getSystem();
        int id = Resources.getSystem().getIdentifier("action_bar_default_height", "dimen", "android");
        if (id > 0) {
            return res.getDimensionPixelSize(id);
        }
        return 0;
    }

    public static void setMIUIStatusBarDarkMode(boolean darkmode, Activity activity) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void invalidateViewInViewGroup(View rootView) {
        if (rootView == null) {
            return;
        }
        if (rootView instanceof ViewGroup) {
            ViewGroup rootGroup = (ViewGroup) rootView;
            for (int i = 0; i < rootGroup.getChildCount(); i++) {
                invalidateViewInViewGroup(rootGroup.getChildAt(i));
            }
        } else {
            rootView.invalidate();
        }
    }

    public static void copyString(Context context, String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(
                Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(null, text));
//        Toast.makeText(context, R.string.text_copied_toast, Toast.LENGTH_SHORT).show();
    }

    public static float getTextPaintOffset(Paint paint) {
        FontMetrics fontMetrics = paint.getFontMetrics();
        return -(fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.top;
    }

    public static void toastDebug(Context context, String msg) {
        if (context == null) {
            return;
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void logDebug(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static SpannableString getNameWithIcon(String name, final Drawable d) {
        if (TextUtils.isEmpty(name)) {
            return new SpannableString("");
        }
        DynamicDrawableSpan drawableSpan =
                new DynamicDrawableSpan(DynamicDrawableSpan.ALIGN_BASELINE) {//基于文本基线,默认是文本底部
                    @Override
                    public Drawable getDrawable() {
                        //Drawable d = getResources().getDrawable(R.drawable.ic_location_on_white_18dp);
                        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                        return d;
                    }
                };
        //ImageSpan imgSpan = new ImageSpan(getApplicationContext(), R.drawable.ic_location_on_white_18dp);

        SpannableString spannableString = new SpannableString(name + " ");
        spannableString.setSpan(drawableSpan, spannableString.length() - 1,
                spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    /**
     * 截取scrollview的屏幕
     *
     * @param scrollView
     * @param backgroundBitmap
     * @return
     */
    public static Bitmap getBitmapByView(ScrollView scrollView, Bitmap backgroundBitmap) {
        if (scrollView == null) {
            return null;
        }
        int h = 0;
        Bitmap bitmap;
        // 获取scrollview实际高度
        for (int i = 0, size = scrollView.getChildCount(); i < size; i++) {
            final View childView = scrollView.getChildAt(i);
            h += childView.getHeight();
            //childView.setBackgroundColor(Color.WHITE);
        }
        // 创建对应大小的bitmap
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), h, Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(backgroundBitmap, new Matrix(), null);
        scrollView.draw(canvas);
        return bitmap;
    }

    /**
     * Returns the bitmap that represents the chart.
     *
     * @return bitmap
     */
    public static Bitmap getBitmapByView(View view) {
        if (view == null) {
            return null;
        }
        // Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
        // Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        // Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            // does not have background drawable, then draw white background on
            // the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        // return the bitmap
        return returnedBitmap;
    }

    public static boolean saveToGallery(Context context, Bitmap bitmap, String fileDir, String fileName,
                                        Bitmap.CompressFormat format, int quality) {
        // restrain quality
        if (quality < 1 || quality > 100) {
            quality = 80;
        }
        File file = new File(fileDir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return false;
            }
        }

        String mimeType;
        switch (format) {
            case PNG:
                mimeType = "image/png";
                if (!fileName.endsWith(".png")) {
                    fileName += ".png";
                }
                break;
            case WEBP:
                mimeType = "image/webp";
                if (!fileName.endsWith(".webp")) {
                    fileName += ".webp";
                }
                break;
            case JPEG:
            default:
                mimeType = "image/jpeg";
                if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))) {
                    fileName += ".jpg";
                }
                break;
        }

        String filePath = fileDir + File.separator + fileName;
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            bitmap.compress(format, quality, out);
            out.flush();
        } catch (IOException e) {
            Log.e(TAG, "saveToGallery: ", e);
            return false;
        }
        long size = new File(filePath).length();

        ContentValues values = new ContentValues(7);

        // store the details
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
        values.put(MediaStore.Images.Media.ORIENTATION, 0);
        values.put(MediaStore.Images.Media.DATA, filePath);
        values.put(MediaStore.Images.Media.SIZE, size);

        return context.getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) != null;
    }

    }
