package com.light.weather.adapter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.light.weather.R;
import com.light.weather.bean.Suggestion;
import com.light.weather.widget.CircleImageView;

import java.util.List;

/**
 * Created by liyu on 2017/4/1.
 */

public class SuggestionAdapter extends BaseQuickAdapter<Suggestion, BaseViewHolder> {

    public SuggestionAdapter(int layoutResId, List<Suggestion> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder holder, Suggestion item) {
        CircleImageView circleImageView = holder.getView(R.id.civ_suggesstion);
        holder.setText(R.id.tvName, item.getTitle());
        holder.setText(R.id.tvMsg, item.getMsg());
        circleImageView.setFillColor(item.getIconBackgroudColor());
        circleImageView.setImageResource(item.getIcon());
    }
}
