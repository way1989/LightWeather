package com.light.weather.widget.dynamic;

import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;


/**
 * Created by liyu on 2017/8/19.
 */

public class OvercastType extends WeatherType {

    private static final int HILL_COLOR = 0xFF59789D; //山坡的颜色
    private PathMeasure mPathMeasure;
    private Paint mPaint;
    private Path mPathFront;                         // 近处的山坡
    private Path mPathRear;                          // 远处的山坡
    private Path mFanPath = new Path();               // 旋转的风扇的扇叶
    private Path mFanPillarPath = new Path();         // 旋转的风扇的柱子
    private float mFanPillarHeight;
    private float mCurRotate;                         // 旋转的风扇的角度
    private float[] mPos;                             // 当前点的实际位置
    private float[] mTan;                             // 当前点的tangent值,用于计算图片所需旋转的角度
    private String mWindSpeed;
    private Shader mCloudShader;
    private Path mCloudPath;
    private float mCloudTransFactor;
    private float mHillTransFactor;
    private float mCloudOffset;

    public OvercastType(Resources resources, ShortWeatherInfo info) {
        super(resources);
        setWeatherColor(0xFF6D8DB1);
        mPathFront = new Path();
        mPathRear = new Path();
        mPaint = new Paint();
        mPos = new float[2];
        mTan = new float[2];
        mPathMeasure = new PathMeasure();
        mWindSpeed = info.getWindSpeed();
        mCloudPath = new Path();
        mCloudOffset = dp2px(32);
    }

    @Override
    public void onDrawElements(Canvas canvas) {

        mPaint.reset();
        mPaint.setColor(HILL_COLOR);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        clearCanvas(canvas);
        canvas.drawColor(mDynamicColor);

        mPaint.setAlpha(100);

        mPathRear.reset();
        mPathRear.moveTo(getWidth(), getHeight() - getHeight() / 7f * mHillTransFactor);
        mPathRear.rQuadTo(-getWidth() / 2f, -20 * mHillTransFactor, -getWidth(),
                getHeight() / 10f * mHillTransFactor);
        mPathRear.lineTo(0, getHeight());
        mPathRear.lineTo(getWidth(), getHeight());
        mPathRear.close();
        canvas.drawPath(mPathRear, mPaint);

        mPaint.setAlpha(255);

        mPathFront.reset();
        mPathFront.moveTo(0, getHeight() - getHeight() / 7f * mHillTransFactor);
        mPathFront.rQuadTo(getWidth() / 2f, -20 * mHillTransFactor, getWidth(),
                getHeight() / 10f * mHillTransFactor);
        mPathFront.lineTo(getWidth(), getHeight());
        mPathFront.lineTo(0, getHeight());
        mPathFront.close();

        drawFan(canvas, mPathFront, 0.618f * mHillTransFactor, 1f);

        drawFan(canvas, mPathRear, 0.15f * mHillTransFactor, 0.4f);

        mPaint.reset();
        mPaint.setColor(HILL_COLOR);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawPath(mPathFront, mPaint);

        mPaint.setShader(mCloudShader);
        mCloudPath.reset();
        mCloudPath.addCircle(getWidth() * mCloudTransFactor,
                getHeight() * 0.618f - mCloudOffset, getHeight() / 22f,
                Path.Direction.CW);
        mCloudPath.addCircle(getWidth() * mCloudTransFactor, getHeight() * 0.618f,
                getHeight() / 22f, Path.Direction.CW);
        mCloudPath.addCircle(getWidth() * mCloudTransFactor - getHeight() / 19f,
                getHeight() * 0.618f - mCloudOffset * 2 / 3, getHeight() / 26f,
                Path.Direction.CW);
        mCloudPath.addCircle(getWidth() * mCloudTransFactor + getHeight() / 20f,
                getHeight() * 0.618f - mCloudOffset * 2 / 3, getHeight() / 30f,
                Path.Direction.CW);
        canvas.drawPath(mCloudPath, mPaint);

    }

    private void drawFan(Canvas canvas, Path path, float location, float scale) {
        int saveCount = canvas.save();
        mPathMeasure.setPath(path, false);
        mPathMeasure.getPosTan(getWidth() * location, mPos, mTan);
        canvas.translate(mPos[0], mPos[1] - mFanPillarHeight * scale);
        canvas.scale(scale, scale);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawPath(mFanPillarPath, mPaint);
        canvas.rotate(mCurRotate * 360f);
        float speed = 0f;
        try {
            speed = Float.valueOf(mWindSpeed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        speed = Math.max(speed, 0.75f);
        mCurRotate += 0.0002f * speed;
        if (mCurRotate > 1f) {
            mCurRotate = 0f;
        }
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mFanPath, mPaint);
        canvas.rotate(120f);
        canvas.drawPath(mFanPath, mPaint);
        canvas.rotate(120f);
        canvas.drawPath(mFanPath, mPaint);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public void generateElements() {
        mCloudShader = new LinearGradient(getWidth() / 2f, 0,
                getWidth() / 2f, getHeight() * 0.618f, 0xFFFFFFFF,
                0x00FFFFFF, Shader.TileMode.CLAMP);
        final float textSize = getHeight() / 32f;
        mFanPath.reset();
        final float fanSize = textSize * 0.2f;// 风扇底部半圆的半径
        final float fanHeight = textSize * 2f;
        final float fanCenterOffsetY = fanSize * 1.6f;
        mFanPath.addArc(new RectF(-fanSize, -fanSize - fanCenterOffsetY, fanSize,
                        fanSize - fanCenterOffsetY), 0, 180);
        mFanPath.quadTo(-fanSize * 1f, -fanHeight * 0.5f - fanCenterOffsetY,
                0, -fanHeight - fanCenterOffsetY);
        mFanPath.quadTo(fanSize * 1f, -fanHeight * 0.5f - fanCenterOffsetY,
                fanSize, -fanCenterOffsetY);
        mFanPath.close();

        mFanPillarPath.reset();
        final float fanPillarSize = textSize * 0.20f;// 柱子的宽度
        mFanPillarPath.moveTo(0, 0);
        mFanPillarHeight = textSize * 4f;// 柱子的高度
        mFanPillarPath.lineTo(2, 0);
        mFanPillarPath.lineTo(fanPillarSize, mFanPillarHeight);
        mFanPillarPath.lineTo(-fanPillarSize, mFanPillarHeight);
        mFanPillarPath.lineTo(-2, 0);
        mFanPillarPath.close();
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

        ValueAnimator animator = ValueAnimator.ofFloat(-0.2f, 1.2f);
        animator.setDuration(30000);
        animator.setRepeatCount(-1);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation ->
                mCloudTransFactor = (float) animation.getAnimatedValue());

        ValueAnimator animator2 = ValueAnimator.ofFloat(0, 1);
        animator2.setDuration(1000);
        animator2.setRepeatCount(0);
        animator2.setInterpolator(new OvershootInterpolator());
        animator2.addUpdateListener(animation ->
                mHillTransFactor = (float) animation.getAnimatedValue());

        mStartAnimatorSet = new AnimatorSet();
        mStartAnimatorSet.play(backgroundColorAnimator).with(animator).with(animator2);
        mStartAnimatorSet.start();
    }

    @Override
    public void endAnimation(AnimatorListenerAdapter listener) {
        ValueAnimator animator = ValueAnimator.ofFloat(1, -1);
        animator.setDuration(END_ANIM_DURATION);
        animator.setRepeatCount(0);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(animation ->
                mHillTransFactor = (float) animation.getAnimatedValue());

        mEndAnimatorSet = new AnimatorSet();
        mEndAnimatorSet.play(animator);
        if (listener != null) {
            mEndAnimatorSet.addListener(listener);
        }
        mEndAnimatorSet.start();
    }
}
