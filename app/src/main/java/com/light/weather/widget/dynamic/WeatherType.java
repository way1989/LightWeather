package com.light.weather.widget.dynamic;

import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;

import java.util.Random;

/**
 * Created by liyu on 2017/8/16.
 */

public abstract class WeatherType implements IWeatherType {
    protected static final long START_ANIM_DURATION = 3000L;
    protected static final long END_ANIM_DURATION = 500L;
    protected int mWeatherColor;
    protected int mDynamicColor;
    protected AnimatorSet mStartAnimatorSet;
    protected AnimatorSet mEndAnimatorSet;
    private Resources mResources;
    private int mWidth;
    private int mHeight;

    public WeatherType(Resources resources) {
        mResources = resources;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public int getWeatherColor() {
        return mWeatherColor;
    }

    public void setWeatherColor(int weatherColor) {
        this.mWeatherColor = weatherColor;
    }

    public void end() {
        if (mStartAnimatorSet != null) {
            if (mStartAnimatorSet.isRunning()) {
                mStartAnimatorSet.end();
            }
            mStartAnimatorSet.removeAllListeners();
        }

        if (mEndAnimatorSet != null) {
            if (mEndAnimatorSet.isRunning()) {
                mEndAnimatorSet.end();
            }
            mEndAnimatorSet.removeAllListeners();
        }
    }

    public abstract void generateElements();

    public abstract void startAnimation(int fromColor);

    public abstract void endAnimation(AnimatorListenerAdapter listener);

    @Override
    public void onSizeChanged(int w, int h) {
        mWidth = w;
        mHeight = h;
        generateElements();
    }

    protected void clearCanvas(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    protected int getRandom(int min, int max) {
        if (max < min) {
            return 1;
        }
        return min + new Random().nextInt(max - min);
    }

    public int dp2px(float dpValue) {
        final float scale = mResources.getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
