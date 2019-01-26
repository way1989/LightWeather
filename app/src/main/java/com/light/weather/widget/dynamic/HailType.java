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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.light.weather.R;

import java.util.ArrayList;

/**
 * Created by liyu on 2017/8/18.
 */

public class HailType extends WeatherType {

    private float mTransFactor;
    private Bitmap mBitmap;
    private Matrix mMatrix;
    private ArrayList<Hail> mHailArrayList;
    private Paint mPaint;

    public HailType(Resources resources) {
        super(resources);
        setWeatherColor(0xFF0CB399);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(5);
        mHailArrayList = new ArrayList<>();
        mMatrix = new Matrix();
        mBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_hail_ground);
    }

    @Override
    public void onDrawElements(Canvas canvas) {
        clearCanvas(canvas);
        canvas.drawColor(mDynamicColor);
        mPaint.setAlpha(255);
        mMatrix.reset();
        mMatrix.postScale(0.25f, 0.25f);
        mMatrix.postTranslate(mTransFactor, getHeight() - mBitmap.getHeight() * 0.25f);
        canvas.drawBitmap(mBitmap, mMatrix, mPaint);

        Hail hail;
        for (int i = 0; i < mHailArrayList.size(); i++) {
            hail = mHailArrayList.get(i);
            mPaint.setAlpha((int) (255 * ((float) hail.y / (float) getHeight())));
            canvas.save();
            canvas.rotate(45, hail.x + hail.size / 2f, hail.y + hail.size / 2f);
            canvas.drawRect(hail.x, hail.y, hail.x + hail.size,
                    hail.y + hail.size, mPaint);
            canvas.restore();
        }
        for (int i = 0; i < mHailArrayList.size(); i++) {
            hail = mHailArrayList.get(i);
            hail.y += hail.speed;
            if (hail.y > getHeight() + hail.size * 2) {
                hail.y = 0 - hail.size * 2;
                hail.x = getRandom(0, getWidth());
            }
        }
    }

    @Override
    public void generateElements() {
        mHailArrayList.clear();
        for (int i = 0; i < 15; i++) {
            Hail hail = new Hail(
                    getRandom(0, getWidth()),
                    getRandom(0, getHeight()),
                    getRandom(dp2px(3), dp2px(8)),
                    getRandom(8, 18)
            );
            mHailArrayList.add(hail);
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
        animator.setRepeatCount(0);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(animation ->
                mTransFactor = (float) animation.getAnimatedValue());
        mEndAnimatorSet = new AnimatorSet();
        mEndAnimatorSet.play(animator);
        mEndAnimatorSet.setDuration(END_ANIM_DURATION);
        if (listener != null) {
            mEndAnimatorSet.addListener(listener);
        }
        mEndAnimatorSet.start();
    }

    private static class Hail {
        int x;
        int y;
        int size;
        int speed;

        Hail(int x, int y, int size, int speed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
        }

    }

}
