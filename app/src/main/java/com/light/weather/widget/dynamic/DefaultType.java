package com.light.weather.widget.dynamic;

import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.view.animation.LinearInterpolator;

/**
 * 默认动态天气
 * Created by liyu on 2017/11/13.
 */

public class DefaultType extends BaseWeatherType {

    public DefaultType(Resources resources) {
        super(resources);
        setWeatherColor(0xFF51C0F8);
    }

    @Override
    public void onDrawElements(Canvas canvas) {
        clearCanvas(canvas);
        canvas.drawColor(mDynamicColor);
    }

    @Override
    public void generateElements() {

    }

    @Override
    public void startAnimation(int fromColor) {
        ValueAnimator backgroundColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, mWeatherColor);
        backgroundColorAnimator.setInterpolator(new LinearInterpolator());
        backgroundColorAnimator.setDuration(1000);
        backgroundColorAnimator.setRepeatCount(0);
        backgroundColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDynamicColor = (int) animation.getAnimatedValue();
            }
        });

        mStartAnimatorSet = new AnimatorSet();
        mStartAnimatorSet.play(backgroundColorAnimator);
        mStartAnimatorSet.start();
    }

    @Override
    public void endAnimation(AnimatorListenerAdapter listener) {
        if (listener != null) {
            listener.onAnimationEnd(null);
        }
    }
}
