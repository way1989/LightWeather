package com.light.weather.adapter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.light.weather.R;
import com.light.weather.bean.HeWeather6;
import com.light.weather.util.WeatherUtil;
import com.light.weather.widget.CircleImageView;

import java.util.List;

/**
 * Created by liyu on 2017/4/1.
 */

public class SuggestionAdapter extends BaseQuickAdapter<HeWeather6.LifestyleBean, BaseViewHolder> {

    public SuggestionAdapter(int layoutResId, List<HeWeather6.LifestyleBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder holder, HeWeather6.LifestyleBean item) {
        CircleImageView circleImageView = holder.getView(R.id.civ_suggesstion);
        holder.setText(R.id.tvName, WeatherUtil.getTitle(item.getType()));
        holder.setText(R.id.tvMsg, item.getBrf());
        circleImageView.setFillColor(WeatherUtil.getBackground(item.getType()));
        circleImageView.setImageResource(WeatherUtil.getIcon(item.getType()));
    }
}
