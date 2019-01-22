package com.light.weather.bean;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BaseResponse<T> {

    @SerializedName("HeWeather6")
    private List<HeWeather6<T>> heWeather6List;

    public boolean isOK() {
        if (heWeather6List != null && !heWeather6List.isEmpty()) {
            final HeWeather6 weather6Bean = heWeather6List.get(0);
            return TextUtils.equals(weather6Bean.getStatus(), "ok");
        }
        return false;
    }

    public HeWeather6<T> getWeather() {
        if (isOK()) {
            return heWeather6List.get(0);
        }
        return null;
    }
}
