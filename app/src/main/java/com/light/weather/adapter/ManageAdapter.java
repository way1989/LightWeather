package com.light.weather.adapter;

import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.light.weather.R;
import com.light.weather.bean.City;
import com.light.weather.util.UiUtil;
import com.light.weather.util.WeatherUtil;

import java.util.List;

/**
 * Created by android on 18-1-30.
 */

public class ManageAdapter extends BaseItemDraggableAdapter<City, BaseViewHolder> {
    public ManageAdapter(@LayoutRes int layoutResId, List<City> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, City item) {
        helper.addOnClickListener(R.id.content).addOnClickListener(R.id.right);
        TextView tvName = helper.getView(android.R.id.text1);
        TextView tvWeather = helper.getView(android.R.id.text2);
        ImageView tvIcon = helper.getView(android.R.id.icon);
        CharSequence name = item.getCity();
        if (item.isLocation()) {
            name = UiUtil.getNameWithIcon(item.getCity(),
                    ContextCompat.getDrawable(tvName.getContext(), R.drawable.ic_location_on_black_18dp));
            helper.getView(R.id.drag_handle).setAlpha(0.2f);
        } else {
            helper.getView(R.id.drag_handle).setAlpha(1.0f);
        }
        // set text
        tvName.setText(name);
        tvWeather.setText(String.format("%s %s℃", item.getCodeTxt(), item.getTmp()));
        tvIcon.setImageResource(WeatherUtil.convertWeatherIcon(item.getCode()));
    }


}
