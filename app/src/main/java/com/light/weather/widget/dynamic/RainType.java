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
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.light.weather.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by liyu on 2017/8/17.
 */

public class RainType extends WeatherType {

    public static final int RAIN_LEVEL_1 = 10;//小雨级别
    public static final int RAIN_LEVEL_2 = 20;//中雨级别
    public static final int RAIN_LEVEL_3 = 30;//大到暴雨级别

    public static final int WIND_LEVEL_1 = 20;//小风
    public static final int WIND_LEVEL_2 = 30;//中风
    public static final int WIND_LEVEL_3 = 45;//大风
    private float mTransFactor;
    private Bitmap mBitmap;
    private Matrix mMatrix;
    private Runnable mFlashRunnable;
    private boolean mIsFlashing = false;
    private boolean mIsSnowing = false;
    private ArrayList<Rain> mRains;
    private Paint mPaint;
    private int mRainLevel = RAIN_LEVEL_1;
    private int mWindLevel = WIND_LEVEL_1;
    private float mAnimatorValue;
    private PathMeasure mFlashPathMeasure1;
    private float mFlashLength1;
    private Path mDstFlash1;
    private PathMeasure mFlashPathMeasure2;
    private float mFlashLength2;
    private Path mDstFlash2;
    private PathMeasure mFlashPathMeasure3;
    private float mFlashLength3;
    private Path mDstFlash3;
    private ArrayList<SnowType.Snow> mSnows;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public RainType(Resources resources, @RainLevel int rainLevel, @WindLevel int windLevel) {
        super(resources);
        setWeatherColor(0xFF6188DA);
        mRainLevel = rainLevel;
        mWindLevel = windLevel;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(5);
        mPaint.setStyle(Paint.Style.STROKE);
        mRains = new ArrayList<>();
        mSnows = new ArrayList<>();
        mMatrix = new Matrix();
        mBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_rain_ground);
        mDstFlash1 = new Path();
        mFlashPathMeasure1 = new PathMeasure();

        mDstFlash2 = new Path();
        mFlashPathMeasure2 = new PathMeasure();

        mDstFlash3 = new Path();
        mFlashPathMeasure3 = new PathMeasure();
    }

    @Override
    public void onDrawElements(Canvas canvas) {
        clearCanvas(canvas);
        canvas.drawColor(mDynamicColor);
        mPaint.setAlpha(255);
        mPaint.setStyle(Paint.Style.STROKE);

        if (mIsFlashing && mAnimatorValue < 1) {
            float stop = mFlashLength1 * mAnimatorValue;
            mDstFlash1.reset();
            mFlashPathMeasure1.getSegment(0, stop, mDstFlash1, true);
            mPaint.setStrokeWidth(14 * (1 - mAnimatorValue));
            canvas.drawPath(mDstFlash1, mPaint);

            float stop2 = mFlashLength2 * mAnimatorValue;
            mDstFlash2.reset();
            mFlashPathMeasure2.getSegment(0, stop2, mDstFlash2, true);
            mPaint.setStrokeWidth(14 * (1 - mAnimatorValue));
            canvas.drawPath(mDstFlash2, mPaint);

            float stop3 = mFlashLength3 * mAnimatorValue;
            mDstFlash3.reset();
            mFlashPathMeasure3.getSegment(0, stop3, mDstFlash3, true);
            mPaint.setStrokeWidth(14 * (1 - mAnimatorValue));
            canvas.drawPath(mDstFlash3, mPaint);
        }

        mPaint.setStrokeWidth(5);
        mMatrix.reset();
        mMatrix.postScale(0.2f, 0.2f);
        mMatrix.postTranslate(mTransFactor, getHeight() - mBitmap.getHeight() * 0.2f + 2f);
        canvas.drawBitmap(mBitmap, mMatrix, mPaint);
        Rain rain;
        for (int i = 0; i < mRains.size(); i++) {
            rain = mRains.get(i);
            mPaint.setAlpha(rain.alpha);
            canvas.drawLine(rain.x, rain.y,
                    (float) (rain.x + rain.length * Math.sin(Math.PI * mWindLevel / 180f)),
                    (float) (rain.y + rain.length * Math.cos(Math.PI * mWindLevel / 180)), mPaint);
        }
        for (int i = 0; i < mRains.size(); i++) {
            rain = mRains.get(i);
            rain.x += (rain.length + rain.speed) * Math.sin(Math.PI * mWindLevel / 180);
            rain.y += (rain.length + rain.speed) * Math.cos(Math.PI * mWindLevel / 180);
            if (rain.x > getWidth() || rain.y > getHeight()) {
                rain.x = getRandom(getWidth() / 3, getWidth() - getWidth() / 5);
                rain.y = getRandom(getHeight() / 2, getHeight() - getHeight() / 5);
            }
        }
        if (mIsSnowing) {
            mPaint.setStyle(Paint.Style.FILL);
            SnowType.Snow snow;
            for (int i = 0; i < mSnows.size(); i++) {
                snow = mSnows.get(i);
                mPaint.setAlpha((int) (255 * (((float) snow.y - getHeight() / 2)
                        / (float) getHeight())));
                canvas.drawCircle(snow.x, snow.y, snow.size, mPaint);
            }
            for (int i = 0; i < mSnows.size(); i++) {
                snow = mSnows.get(i);
                snow.x += 1;
                snow.y += snow.speed;
                if (snow.y > getHeight() + snow.size * 2) {
                    snow.x = getRandom(getWidth() / 4, getWidth());
                    snow.y = getHeight() / 2;
                }
            }
        }
    }

    private void createFlash(int x1, int y1) {
        Path flash1 = new Path();
        Path flash2 = new Path();
        Path flash3 = new Path();

        flash1.moveTo(x1, y1);
        flash2.moveTo(x1, y1);
        flash3.moveTo(x1, y1);

        int[] yArray = randomArray(0, 400, 50);
        Arrays.sort(yArray);
        int[] xArray = new int[yArray.length];
        for (int i = 0; i < yArray.length; i++) {
            xArray[i] = (int) (yArray[i] + (16 - (Math.random() * 32)));
            flash1.lineTo(xArray[i] + x1, yArray[i] + y1);
        }

        mFlashPathMeasure1.setPath(flash1, false);
        mFlashLength1 = mFlashPathMeasure1.getLength();

        int[] yArray2Temp = randomArray(yArray[20], yArray[40], 20);
        Arrays.sort(yArray2Temp);
        int[] xArray2Temp = new int[yArray2Temp.length];

        for (int i = 0; i < yArray2Temp.length; i++) {
            xArray2Temp[i] = (int) (yArray2Temp[i] * 0.5f + (16 - (Math.random() * 32)));
        }

        int[] xArray2 = new int[xArray2Temp.length + 11];
        System.arraycopy(xArray, 0, xArray2, 0, 11);
        System.arraycopy(xArray2Temp, 0, xArray2, 11, xArray2Temp.length);

        int[] yArray2 = new int[yArray2Temp.length + 11];

        System.arraycopy(yArray, 0, yArray2, 0, 11);
        System.arraycopy(yArray2Temp, 0, yArray2, 11, yArray2Temp.length);

        int[] yArray3Temp = randomArray(yArray[25], yArray[45], 20);
        Arrays.sort(yArray3Temp);
        int[] xArray3Temp = new int[yArray3Temp.length];

        for (int i = 0; i < yArray3Temp.length; i++) {
            xArray3Temp[i] = (int) (yArray3Temp[i] * 1.5f + (16 - (Math.random() * 32)));
        }

        int[] xArray3 = new int[xArray3Temp.length + 26];
        System.arraycopy(xArray, 0, xArray3, 0, 26);
        System.arraycopy(xArray3Temp, 0, xArray3, 26, xArray3Temp.length);

        int[] yArray3 = new int[yArray3Temp.length + 26];

        System.arraycopy(yArray, 0, yArray3, 0, 26);
        System.arraycopy(yArray3Temp, 0, yArray3, 26, yArray3Temp.length);

        for (int m = 0; m < xArray2.length; m++) {
            flash2.lineTo(xArray2[m] + x1, yArray2[m] + y1);
        }

        for (int n = 0; n < xArray3.length; n++) {
            flash3.lineTo(xArray3[n] + x1, yArray3[n] + y1);
        }

        mFlashPathMeasure2.setPath(flash2, false);
        mFlashLength2 = mFlashPathMeasure2.getLength();

        mFlashPathMeasure3.setPath(flash3, false);
        mFlashLength3 = mFlashPathMeasure3.getLength();
    }

    private int[] randomArray(int min, int max, int n) {
        if (n > (max - min + 1) || max < min) {
            return null;
        }
        int[] result = new int[n];
        int count = 0;
        while (count < n) {
            int num = (int) (Math.random() * (max - min)) + min;
            boolean flag = true;
            for (int j = 0; j < n; j++) {
                if (num == result[j]) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                result[count] = num;
                count++;
            }
        }
        return result;
    }

    @Override
    public void generateElements() {
        mRains.clear();
        for (int i = 0; i < mRainLevel; i++) {
            Rain rain = new Rain(
                    getRandom(getWidth() / 4, getWidth() - getWidth() / 5),
                    getRandom(getHeight() / 2, getHeight() - getHeight() / 5),
                    getRandom(dp2px(10), dp2px(20)),
                    getRandom(1, 3),
                    getRandom(20, 100)
            );
            mRains.add(rain);
        }
        createFlash(getWidth() / 3, getHeight() / 2);

        mSnows.clear();
        for (int i = 0; i < mRainLevel; i++) {
            SnowType.Snow snow = new SnowType.Snow(
                    getRandom(getWidth() / 4, getWidth() - getWidth() / 5),
                    getRandom(getHeight() / 2, getHeight() - getHeight() / 5),
                    getRandom(dp2px(1), dp2px(6)),
                    getRandom(1, 5)
            );
            mSnows.add(snow);
        }
    }

    public boolean isFlashing() {
        return mIsFlashing;
    }

    public void setFlashing(boolean flashing) {
        mIsFlashing = flashing;
        setWeatherColor(0xFF7187DB);
    }

    public boolean isSnowing() {
        return mIsSnowing;
    }

    public void setSnowing(boolean snowing) {
        mIsSnowing = snowing;
        setWeatherColor(0xFF5697D8);
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

        ValueAnimator animator = ValueAnimator.ofFloat(-mBitmap.getWidth() * 0.2f,
                getWidth() - mBitmap.getWidth() * 0.2f);
        animator.setDuration(1000);
        animator.setRepeatCount(0);
        animator.setInterpolator(new OvershootInterpolator());
        animator.addUpdateListener(animation ->
                mTransFactor = (float) animation.getAnimatedValue());

        mStartAnimatorSet = new AnimatorSet();
        mStartAnimatorSet.play(backgroundColorAnimator).with(animator);
        mStartAnimatorSet.start();

        mFlashRunnable = () -> {
            ValueAnimator animator2 = ValueAnimator.ofFloat(0, 1);
            animator2.setDuration(500);
            animator2.setRepeatCount(0);
            animator2.setInterpolator(new DecelerateInterpolator());
            animator2.addUpdateListener(animation ->
                    mAnimatorValue = (float) animation.getAnimatedValue());
            animator2.start();
            mHandler.postDelayed(mFlashRunnable, 5000L);
        };
        mHandler.post(mFlashRunnable);
    }

    @Override
    public void endAnimation(AnimatorListenerAdapter listener) {
        mHandler.removeCallbacks(mFlashRunnable);
        ValueAnimator animator = ValueAnimator.ofFloat(getWidth() - mBitmap.getWidth() * 0.2f,
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

    @IntDef({RAIN_LEVEL_1, RAIN_LEVEL_2, RAIN_LEVEL_3})
    @Retention(RetentionPolicy.SOURCE)
    @interface RainLevel {
    }

    @IntDef({WIND_LEVEL_1, WIND_LEVEL_2, WIND_LEVEL_3})
    @Retention(RetentionPolicy.SOURCE)
    @interface WindLevel {
    }

    private class Rain {
        /**
         * 雨点 x 轴坐标
         */
        int x;
        /**
         * 雨点 y 轴坐标
         */
        int y;
        /**
         * 雨点长度
         */
        int length;
        /**
         * 雨点移动速度
         */
        int speed;
        /**
         * 雨点透明度
         */
        int alpha;

        public Rain(int x, int y, int length, int speed, int alpha) {
            this.x = x;
            this.y = y;
            this.length = length;
            this.speed = speed;
            this.alpha = alpha;
        }
    }
}
