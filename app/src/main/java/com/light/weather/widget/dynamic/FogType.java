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
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.light.weather.R;


/**
 * Created by liyu on 2017/10/13.
 */

public class FogType extends WeatherType {

    private Paint mPaint;               // 画笔

    private float mFogFactor1;           // 雾变化因子1，动态改变圆半径

    private float mFogFactor2;           // 雾变化因子2，动态改变圆半径

    private float mTransFactor;          // 背景图位移变化因子

    private Bitmap mBitmap;

    private Matrix mMatrix;

    private Shader mShader;

    public FogType(Resources resources) {
        super(resources);
        setWeatherColor(0xFF8CADD3);
        mPaint = new Paint();
        mMatrix = new Matrix();
        mBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_fog_ground);
    }

    @Override
    public void onDrawElements(Canvas canvas) {
        mPaint.reset();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        clearCanvas(canvas);
        canvas.drawColor(mDynamicColor);

        mMatrix.reset();
        mMatrix.postScale(0.25f, 0.25f);
        mMatrix.postTranslate(mTransFactor, getHeight() - mBitmap.getHeight() * 0.25f);
        canvas.drawBitmap(mBitmap, mMatrix, mPaint);

        mPaint.setShader(mShader);

        mPaint.setAlpha((int) (255 * (1 - mFogFactor1)));

        canvas.drawCircle(getWidth() / 2f + getWidth() / 6f, getHeight(),
                getWidth() / 2f * mFogFactor1, mPaint);

        mPaint.setAlpha((int) (255 * (1 - mFogFactor2)));

        canvas.drawCircle(getWidth() / 2f + getWidth() / 6f, getHeight(),
                getWidth() / 2f * mFogFactor2, mPaint);
    }

    @Override
    public void generateElements() {
        mShader = new RadialGradient(getWidth() / 2f + getWidth() / 6f, getHeight(),
                getWidth() / 2, 0x00ffffff, 0xffffffff,
                Shader.TileMode.CLAMP);
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
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(6000);
        animator.setRepeatCount(-1);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation ->
                mFogFactor1 = (float) animation.getAnimatedValue());
        animator.setStartDelay(3000);

        ValueAnimator animator2 = ValueAnimator.ofFloat(0, 1);
        animator2.setDuration(6000);
        animator2.setRepeatCount(-1);
        animator2.setInterpolator(new LinearInterpolator());
        animator2.addUpdateListener(animation ->
                mFogFactor2 = (float) animation.getAnimatedValue());

        ValueAnimator animator3 = ValueAnimator.ofFloat(-mBitmap.getWidth() * 0.25f,
                getWidth() - mBitmap.getWidth() * 0.25f);
        animator3.setDuration(1000);
        animator3.setRepeatCount(0);
        animator3.setInterpolator(new OvershootInterpolator());
        animator3.addUpdateListener(animation ->
                mTransFactor = (float) animation.getAnimatedValue());

        mStartAnimatorSet = new AnimatorSet();
        mStartAnimatorSet.play(backgroundColorAnimator).with(animator)
                .with(animator2).with(animator3);
        mStartAnimatorSet.start();
    }

    @Override
    public void endAnimation(AnimatorListenerAdapter listener) {
        ValueAnimator animator = ValueAnimator.ofFloat(mFogFactor1, 0);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation ->
                mFogFactor1 = (float) animation.getAnimatedValue());

        ValueAnimator animator2 = ValueAnimator.ofFloat(mFogFactor2, 0);
        animator2.setInterpolator(new LinearInterpolator());
        animator2.addUpdateListener(animation ->
                mFogFactor2 = (float) animation.getAnimatedValue());

        ValueAnimator animator3 = ValueAnimator.ofFloat(getWidth() - mBitmap.getWidth() * 0.25f,
                getWidth());
        animator3.setInterpolator(new AccelerateInterpolator());
        animator3.addUpdateListener(animation ->
                mTransFactor = (float) animation.getAnimatedValue());

        mEndAnimatorSet = new AnimatorSet();
        mEndAnimatorSet.play(animator).with(animator2).with(animator3);
        mEndAnimatorSet.setDuration(END_ANIM_DURATION);
        if (listener != null) {
            mEndAnimatorSet.addListener(listener);
        }
        mEndAnimatorSet.start();
    }
}
