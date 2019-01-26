package com.light.weather.widget.dynamic;

import android.graphics.Canvas;

/**
 * Created by liyu on 2017/8/16.
 */

public interface IWeatherType {
    void onDrawElements(Canvas canvas);

    void onSizeChanged(int width, int height);
}
