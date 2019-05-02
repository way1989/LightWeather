package com.light.weather.widget.dynamic;

import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.core.view.animation.PathInterpolatorCompat;

import com.light.weather.R;
import com.light.weather.util.TimeUtils;


/**
 * Created by liyu on 2017/8/18.
 */

public class SunnyType extends WeatherType {

    private static final float BITMAP_SCALE = 0.2f; //图片的缩小倍数
    private static final int COLOR_DAY = 0xFF51C0F8;
    private static final int COLOR_NIGHT = 0xFF7F9EE9;
    private static final int COLOR_WAVE_START_NIGHT = 0x1A3A66CF;
    private static final int COLOR_WAVE_END_NIGHT_REAR = 0x803A66CF;
    private static final int COLOR_WAVE_END_NIGHT_FRONT = 0xE63A66CF;
    private static final int COLOR_WAVE_START_DAY = 0x1AFFFFFF;
    private static final int COLOR_WAVE_END_DAY_REAR = 0x80FFFFFF;
    private static final int COLOR_WAVE_END_DAY_FRONT = 0xE6FFFFFF;
    private Paint mPaint;               // 画笔
    private Path mPathFront;            //近处的波浪 path
    private Path mPathRear;             //远处的波浪 path
    private Path mSunPath;               // 太阳升起的半圆轨迹
    private PathMeasure mSunMeasure;
    private float mSpeed;                //振幅，用于初始时的动画效果
    private float[] mPos;                // 当前点的实际位置
    private float[] mTan;                // 当前点的切线写角度值,用于计算图片所需旋转的角度
    private Bitmap mBoatBitmap;             // 小船儿
    private Matrix mMatrix;             // 矩阵,用于对小船儿进行一些操作
    private PathMeasure mPathMeasure;
    private float φ;
    private Shader mShaderRear;
    private Shader mShaderFront;
    private float mBoardSpeed;
    private float mCloudShake;
    private float mCloudSpeed;
    private float mCurrentSunPosition;
    private float mCurrentMoonPosition;

    private float[] mSunPos;
    private float[] mSunTan;

    private float[] mMoonPos;
    private float[] mMoonTan;

    private boolean mIsCloud = false;
    private Bitmap mCloudBitmap;

    public SunnyType(Resources resources, ShortWeatherInfo info) {
        super(resources);
        mPathFront = new Path();
        mPathRear = new Path();
        mSunPath = new Path();
        mPaint = new Paint();
        mPos = new float[2];
        mTan = new float[2];
        mMatrix = new Matrix();
        mPathMeasure = new PathMeasure();
        mSunMeasure = new PathMeasure();
        mCurrentSunPosition = TimeUtils.getTimeDiffPercent(info.getSunrise(), info.getSunset());
        mCurrentMoonPosition = TimeUtils.getTimeDiffPercent(info.getMoonrise(), info.getMoonset());
        if (mCurrentSunPosition >= 0 && mCurrentSunPosition <= 1) {
            setWeatherColor(COLOR_DAY);
            mBoatBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_boat_day);
        } else {
            setWeatherColor(COLOR_NIGHT);
            mBoatBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_boat_night);
        }

        mCloudBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_cloud);

    }

    public boolean isCloud() {
        return mIsCloud;
    }

    public void setCloud(boolean isCloud) {
        mIsCloud = isCloud;
    }

    @Override
    public void onDrawElements(Canvas canvas) {
        mPaint.reset();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        clearCanvas(canvas);
        canvas.drawColor(mDynamicColor);

        if (mCurrentSunPosition >= 0 && mCurrentSunPosition <= 1) {
            mPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.SOLID));
            canvas.drawCircle(mSunPos[0], mSunPos[1], 40, mPaint);
            mPaint.setMaskFilter(null);
            if (mShaderRear == null) {
                mShaderRear = new LinearGradient(0, getHeight(), getWidth(), getHeight(),
                        COLOR_WAVE_START_DAY, COLOR_WAVE_END_DAY_REAR, Shader.TileMode.CLAMP);
            }
            if (mShaderFront == null) {
                mShaderFront = new LinearGradient(0, getHeight(), getWidth(), getHeight(),
                        COLOR_WAVE_START_DAY, COLOR_WAVE_END_DAY_FRONT, Shader.TileMode.CLAMP);
            }
        } else if (mCurrentMoonPosition >= 0 && mCurrentMoonPosition <= 1) {
            mPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.SOLID));
            canvas.drawCircle(mMoonPos[0], mMoonPos[1], 40, mPaint);
            mPaint.setColor(mDynamicColor);
            canvas.drawCircle(mMoonPos[0] + 20, mMoonPos[1] - 20, 40, mPaint);
            mPaint.setColor(Color.WHITE);
            mPaint.setMaskFilter(null);
            if (mShaderRear == null) {
                mShaderRear = new LinearGradient(0, getHeight(), getWidth(), getHeight(),
                        COLOR_WAVE_START_NIGHT, COLOR_WAVE_END_NIGHT_REAR, Shader.TileMode.CLAMP);
            }
            if (mShaderFront == null) {
                mShaderFront = new LinearGradient(0, getHeight(), getWidth(), getHeight(),
                        COLOR_WAVE_START_NIGHT, COLOR_WAVE_END_NIGHT_FRONT, Shader.TileMode.CLAMP);
            }
        } else {
            mPaint.setMaskFilter(null);
            if (mShaderRear == null) {
                mShaderRear = new LinearGradient(0, getHeight(), getWidth(), getHeight(),
                        COLOR_WAVE_START_NIGHT, COLOR_WAVE_END_NIGHT_REAR, Shader.TileMode.CLAMP);
            }
            if (mShaderFront == null) {
                mShaderFront = new LinearGradient(0, getHeight(), getWidth(), getHeight(),
                        COLOR_WAVE_START_NIGHT, COLOR_WAVE_END_NIGHT_FRONT, Shader.TileMode.CLAMP);
            }
        }

        φ -= 0.05f;

        float y, y2;

        double ω = 2 * Math.PI / getWidth();

        mPathRear.reset();
        mPathFront.reset();
        mPathFront.moveTo(-mBoatBitmap.getWidth() * BITMAP_SCALE, getHeight());
        mPathRear.moveTo(-mBoatBitmap.getWidth() * BITMAP_SCALE, getHeight());
        for (float x = -mBoatBitmap.getWidth() * BITMAP_SCALE;
             x <= getWidth() + mBoatBitmap.getWidth() * BITMAP_SCALE; x += 20) {
            /**
             *  y=Asin(ωx+φ)+k
             *  A—振幅越大，波形在y轴上最大与最小值的差值越大
             *  ω—角速度， 控制正弦周期(单位角度内震动的次数)
             *  φ—初相，反映在坐标系上则为图像的左右移动，通过不断改变φ,达到波浪移动效果
             *  k—偏距，反映在坐标系上则为图像的上移或下移。
             */
            y = (float) (mSpeed * Math.cos(ω * x + φ) + getHeight() * 6 / 7);
            y2 = (float) (mSpeed * Math.sin(ω * x + φ) + getHeight() * 6 / 7 - 8);
            mPathFront.lineTo(x, y);
            mPathRear.lineTo(x, y2);
        }

        mPathFront.lineTo(getWidth() + mBoatBitmap.getWidth() * BITMAP_SCALE, getHeight());
        mPathRear.lineTo(getWidth() + mBoatBitmap.getWidth() * BITMAP_SCALE, getHeight());

        mPaint.setShader(mShaderRear);
        canvas.drawPath(mPathRear, mPaint);

        mPathMeasure.setPath(mPathFront, false);
        mPathMeasure.getPosTan(mPathMeasure.getLength() * 0.618f * mBoardSpeed, mPos, mTan);
        mMatrix.reset();
        float degrees = (float) (Math.atan2(mTan[1], mTan[0]) * 180.0 / Math.PI);
        mMatrix.postScale(BITMAP_SCALE, BITMAP_SCALE);
        mMatrix.postRotate(degrees, mBoatBitmap.getWidth() * BITMAP_SCALE / 2,
                mBoatBitmap.getHeight() * BITMAP_SCALE / 2);
        mMatrix.postTranslate(mPos[0] - mBoatBitmap.getWidth() / 2f * BITMAP_SCALE,
                mPos[1] - mBoatBitmap.getHeight() * BITMAP_SCALE + 4);
        mPaint.setAlpha(255);
        canvas.drawBitmap(mBoatBitmap, mMatrix, mPaint);

        mPaint.setShader(mShaderFront);
        canvas.drawPath(mPathFront, mPaint);

        if (!mIsCloud)
            return;

        if (mCurrentSunPosition >= 0 && mCurrentSunPosition <= 1) {
            mPaint.setAlpha(200);
        } else {
            mPaint.setAlpha(80);
        }
        mMatrix.reset();
        mMatrix.postScale(BITMAP_SCALE, BITMAP_SCALE);
        mMatrix.postTranslate(getWidth() / 2f * mCloudSpeed,
                getHeight() / 2f + mCloudShake);
        canvas.drawBitmap(mCloudBitmap, mMatrix, mPaint);

    }

    @Override
    public void generateElements() {
        mSunPath.reset();
        RectF rectF = new RectF(100f, getHeight() * (8.5f / 10f) - getWidth() / 2f,
                getWidth() - 100, getHeight() * (8.5f / 10f) + getWidth() / 2f);
        mSunPath.arcTo(rectF, 180, 180);
        mSunMeasure.setPath(mSunPath, false);
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

        mSunPos = new float[2];
        mSunTan = new float[2];
        mMoonPos = new float[2];
        mMoonTan = new float[2];
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(2000);
        animator.setRepeatCount(0);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation ->
                mSpeed = (float) animation.getAnimatedValue() * 32);

        ValueAnimator animator2 = ValueAnimator.ofFloat(1.5f, 1);
        animator2.setDuration(3000);
        animator2.setRepeatCount(0);
        animator2.setInterpolator(new DecelerateInterpolator());
        animator2.addUpdateListener(animation ->
                mBoardSpeed = (float) animation.getAnimatedValue());

        ValueAnimator animator3 = ValueAnimator.ofFloat(-0.5f, 1);
        animator3.setDuration(3000);
        animator3.setRepeatCount(0);
        animator3.setInterpolator(new DecelerateInterpolator());
        animator3.addUpdateListener(animation ->
                mCloudSpeed = (float) animation.getAnimatedValue());

        ValueAnimator sunAnimator = ValueAnimator.ofFloat(0, 1);
        sunAnimator.setDuration(3000);
        sunAnimator.setRepeatCount(0);
        sunAnimator.setInterpolator(PathInterpolatorCompat.create(0.5f, 1));
        sunAnimator.addUpdateListener(animation -> {
            mSunMeasure.getPosTan(mSunMeasure.getLength()
                            * (float) animation.getAnimatedValue() * mCurrentSunPosition,
                    mSunPos, mSunTan);
            mSunMeasure.getPosTan(mSunMeasure.getLength()
                            * (float) animation.getAnimatedValue() * mCurrentMoonPosition,
                    mMoonPos, mMoonTan);
        });

        ValueAnimator animatorCloud = ValueAnimator.ofFloat(-10, 10);
        animatorCloud.setDuration(2000);
        animatorCloud.setRepeatCount(-1);
        animatorCloud.setRepeatMode(ValueAnimator.REVERSE);
        animatorCloud.setInterpolator(new DecelerateInterpolator());
        animatorCloud.addUpdateListener(animation ->
                mCloudShake = (float) animation.getAnimatedValue());

        mStartAnimatorSet = new AnimatorSet();
        mStartAnimatorSet.play(backgroundColorAnimator).with(animator).with(animator2)
                .with(animator3).with(sunAnimator).with(animatorCloud);
        mStartAnimatorSet.start();
    }

    @Override
    public void endAnimation(AnimatorListenerAdapter listener) {
        ValueAnimator animator1 = ValueAnimator.ofFloat(mCurrentSunPosition, 1);
        animator1.setInterpolator(PathInterpolatorCompat.create(0.5f, 1));
        animator1.addUpdateListener(animation ->
                mSunMeasure.getPosTan(mSunMeasure.getLength()
                        * (float) animation.getAnimatedValue(), mSunPos, mSunTan));

        ValueAnimator animator2 = ValueAnimator.ofFloat(1, 0);
        animator2.setInterpolator(new AccelerateInterpolator());
        animator2.addUpdateListener(animation ->
                mBoardSpeed = (float) animation.getAnimatedValue());

        ValueAnimator animator3 = ValueAnimator.ofFloat(mCurrentMoonPosition, 1);
        animator3.setInterpolator(PathInterpolatorCompat.create(0.5f, 1));
        animator3.addUpdateListener(animation ->
                mSunMeasure.getPosTan(mSunMeasure.getLength()
                        * (float) animation.getAnimatedValue(), mMoonPos, mMoonTan));

        ValueAnimator animator4 = ValueAnimator.ofFloat(1, 2f);
        animator4.setInterpolator(new AccelerateInterpolator());
        animator4.addUpdateListener(animation ->
                mCloudSpeed = (float) animation.getAnimatedValue());

        mEndAnimatorSet = new AnimatorSet();
        mEndAnimatorSet.play(animator1).with(animator2).with(animator3).with(animator4);
        mEndAnimatorSet.setDuration(END_ANIM_DURATION);
        if (listener != null) {
            mEndAnimatorSet.addListener(listener);
        }
        mEndAnimatorSet.start();
    }
}
