package com.light.weather.widget.dynamic;

import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.IntDef;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.light.weather.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Created by liyu on 2017/8/18.
 */

public class SnowType extends WeatherType {

    public static final int SNOW_LEVEL_1 = 20;//小雪级别
    public static final int SNOW_LEVEL_2 = 40;//中雪级别
    public static final int SNOW_LEVEL_3 = 60;//大到暴雪级别
    private float mTransFactor;
    private Bitmap mBitmap;
    private Matrix mMatrix;
    private ArrayList<Snow> mSnows;
    private Paint mPaint;
    private int mSnowLevel = SNOW_LEVEL_1;

    public SnowType(Resources resources, @SnowLevel int snowLevel) {
        super(resources);
        setWeatherColor(0xFF62B1FF);
        mSnowLevel = snowLevel;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(5);
        mSnows = new ArrayList<>();
        mMatrix = new Matrix();
        mBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_snow_ground);
    }

    @Override
    public void onDrawElements(Canvas canvas) {
        clearCanvas(canvas);
        canvas.drawColor(mDynamicColor);
        mPaint.setAlpha(255);
        mMatrix.reset();
        mMatrix.postScale(0.25f, 0.25f);
        mMatrix.postTranslate(mTransFactor, getHeight() - mBitmap.getHeight() * 0.23f);
        canvas.drawBitmap(mBitmap, mMatrix, mPaint);
        Snow snow;
        for (int i = 0; i < mSnows.size(); i++) {
            snow = mSnows.get(i);
            mPaint.setAlpha((int) (255 * ((float) snow.y / (float) getHeight())));
            canvas.drawCircle(snow.x, snow.y, snow.size, mPaint);
        }
        for (int i = 0; i < mSnows.size(); i++) {
            snow = mSnows.get(i);
            snow.y += snow.speed;
            if (snow.y > getHeight() + snow.size * 2) {
                snow.y = 0 - snow.size * 2;
                snow.x = getRandom(0, getWidth());
            }
        }
    }

    @Override
    public void generateElements() {
        mSnows.clear();
        for (int i = 0; i < mSnowLevel; i++) {
            Snow snow = new Snow(
                    getRandom(0, getWidth()),
                    getRandom(0, getHeight()),
                    getRandom(dp2px(1), dp2px(6)),
                    getRandom(1, mSnowLevel / 12)
            );
            mSnows.add(snow);
        }
    }

    @Override
    public void startAnimation(int fromColor) {
        ValueAnimator backgroundColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(),
                fromColor, mWeatherColor);
        backgroundColorAnimator.setInterpolator(new LinearInterpolator());
        backgroundColorAnimator.setDuration(1000);
        backgroundColorAnimator.setRepeatCount(0);
        backgroundColorAnimator.addUpdateListener(animation ->
                mDynamicColor = (int) animation.getAnimatedValue());

        ValueAnimator animator = ValueAnimator.ofFloat(-mBitmap.getWidth() * 0.25f,
                getWidth() - mBitmap.getWidth() * 0.25f);
        animator.setDuration(1000);
        animator.setRepeatCount(0);
        animator.setInterpolator(new OvershootInterpolator());
        animator.addUpdateListener(animation ->
                mTransFactor = (float) animation.getAnimatedValue());

        mStartAnimatorSet = new AnimatorSet();
        mStartAnimatorSet.play(backgroundColorAnimator).with(animator);
        mStartAnimatorSet.start();
    }

    @Override
    public void endAnimation(AnimatorListenerAdapter listener) {
        ValueAnimator animator = ValueAnimator.ofFloat(getWidth() - mBitmap.getWidth() * 0.25f,
                getWidth());
        animator.setDuration(END_ANIM_DURATION);
        animator.setRepeatCount(0);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(animation ->
                mTransFactor = (float) animation.getAnimatedValue());

        mEndAnimatorSet = new AnimatorSet();
        mEndAnimatorSet.play(animator);
        if (listener != null) {
            mEndAnimatorSet.addListener(listener);
        }
        mEndAnimatorSet.start();
    }

    @IntDef({SNOW_LEVEL_1, SNOW_LEVEL_2, SNOW_LEVEL_3})
    @Retention(RetentionPolicy.SOURCE)
    @interface SnowLevel {
    }

    static class Snow {
        /**
         * 雪花 x 轴坐标
         */
        int x;
        /**
         * 雪花 y 轴坐标
         */
        int y;
        /**
         * 雪花大小
         */
        int size;
        /**
         * 雪花移动速度
         */
        int speed;

        Snow(int x, int y, int size, int speed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
        }

    }
}
