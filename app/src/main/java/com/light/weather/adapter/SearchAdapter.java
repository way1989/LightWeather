package com.light.weather.adapter;

import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.light.weather.R;
import com.light.weather.bean.City;

import java.util.List;

/**
 * Created by android on 18-1-30.
 */

public class SearchAdapter extends BaseQuickAdapter<City, BaseViewHolder> {

    public SearchAdapter(int layoutResId, List<City> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, City city) {
        TextView cityName = helper.getView(R.id.city_name_tv);
        TextView cityProv = helper.getView(R.id.city_prov_tv);
        if (city == null) {
            cityName.setText("unknown");
            cityProv.setText("");
            return;
        }
        helper.itemView.setTag(city);
        cityName.setText(city.getCity());
        if (city.isLocation()) {
            cityProv.setText("");
        } else {
            cityProv.setText(city.getProv());
        }
    }
}
