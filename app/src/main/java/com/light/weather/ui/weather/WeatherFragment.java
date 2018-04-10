package com.light.weather.ui.weather;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.light.weather.BuildConfig;
import com.light.weather.R;
import com.light.weather.adapter.AqiAdapter;
import com.light.weather.adapter.SuggestionAdapter;
import com.light.weather.bean.AqiDetailBean;
import com.light.weather.bean.City;
import com.light.weather.bean.HeWeather;
import com.light.weather.di.Injectable;
import com.light.weather.ui.base.BaseFragment;
import com.light.weather.ui.common.WeatherViewModel;
import com.light.weather.util.FormatUtil;
import com.light.weather.util.RxSchedulers;
import com.light.weather.util.ShareUtils;
import com.light.weather.util.WeatherUtil;
import com.light.weather.widget.AqiView;
import com.light.weather.widget.AstroView;
import com.light.weather.widget.DailyForecastView;
import com.light.weather.widget.HourlyForecastView;
import com.light.weather.widget.dynamic.BaseWeatherType;
import com.light.weather.widget.dynamic.ShortWeatherInfo;
import com.light.weather.widget.dynamic.TypeUtil;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by android on 16-11-10.
 */

public class WeatherFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, Injectable {
    private static final String TAG = "WeatherFragment";
    private static final String ARG_KEY = "city";
    @BindView(R.id.w_dailyForecastView)
    DailyForecastView mWDailyForecastView;
    @BindView(R.id.w_hourlyForecastView)
    HourlyForecastView mWHourlyForecastView;
    @BindView(R.id.w_aqi_view)
    AqiView mWAqiView;
    @BindView(R.id.w_astroView)
    AstroView mWAstroView;
    @BindView(R.id.w_WeatherScrollView)
    ScrollView mWWeatherScrollView;
    @BindView(R.id.w_PullRefreshLayout)
    SwipeRefreshLayout mWPullRefreshLayout;
    @BindView(R.id.suggestion_recyclerView)
    RecyclerView mSuggestionRecyclerView;

    @BindView(R.id.aqi_recyclerview)
    RecyclerView mAqiRecyclerView;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private OnDrawerTypeChangeListener mListener;
    private City mCity;
    private HeWeather mWeather;
    private SuggestionAdapter mSuggestionAdapter;
    private AqiAdapter mAqiAdapter;

    public static WeatherFragment makeInstance(@NonNull City city) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_KEY, city);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(WeatherViewModel.class);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0, R.id.action_share, 0, R.string.action_share)
                .setIcon(R.drawable.ic_menu_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: item = " + item);
        switch (item.getItemId()) {
            case R.id.action_share:
                Log.d(TAG, "onOptionsItemSelected: id = share... isVisible = " + getUserVisibleHint());
                onShareItemClick();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDrawerTypeChangeListener) {
            mListener = (OnDrawerTypeChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDrawerTypeChangeListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (mWeather == null || !DeviceUtil.hasInternet(getContext()))
//            return;
//        mPresenter.getWeather(mCity.getAreaId(), true);
    }

    private City getArgCity() {
        return (City) getArguments().getSerializable(ARG_KEY);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_weather;
    }

    public void onShareItemClick() {
        if (getUserVisibleHint() && mWeather != null && mWeather.isOK()) {
            final String shareInfo = WeatherUtil.getInstance().getShareMessage(mWeather);
            ShareUtils.shareText(getActivity(), shareInfo, "分享到");
            mWWeatherScrollView.scrollTo(0, 0);
//            RxImage.saveText2ImageObservable(mWWeatherScrollView)
//                    .compose(RxSchedulers.<File>io_main())
//                    .subscribe(new Consumer<File>() {
//                        @Override
//                        public void accept(File file) throws Exception {
//                            Log.d(TAG, "onShareItemClick onNext: file = " + file);
//                            ShareUtils.shareImage(getActivity(), file, "分享到");
//                        }
//                    }, new Consumer<Throwable>() {
//                        @Override
//                        public void accept(Throwable throwable) throws Exception {
//                            Log.e(TAG, "onShareItemClick onError: ", throwable);
//                            showErrorTip("share error:" + throwable.getMessage());
//                        }
//                    });
        }
    }

    public void onWeatherChange(HeWeather weather) {
        mWeather = weather;
        updateWeatherUI();
    }

    public void onLocationChange(City city) {
        if (!TextUtils.equals(mCity.getAreaId(), city.getAreaId())) {
            mListener.onLocationChange(city);
        }
        if (getUserVisibleHint()) {
            mCity = city;
            getWeather(city, true);
        }
    }

    public void showErrorTip(String msg) {
        if (BuildConfig.LOG_DEBUG)
            toast(msg);
    }

    @Override
    public void loadDataFirstTime() {
        if (getActivity() == null || mCity == null) {
            showErrorTip("city is null");
            Log.e(TAG, "loadDataFirstTime: ", new Throwable("something is null..."));
            return;
        }
        if (!TextUtils.isEmpty(mCity.getAreaId())) {
            getWeather(mCity, false);
        }
        if (mCity.getIsLocation() == 1) {
            getLocation();
        }
    }

    private void getWeather(City city, boolean force) {
        Log.i(TAG, "getWeather... city = " + city + ", areaId = " + city.getAreaId());
        mDisposable.add(mViewModel.getWeather(city, force)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        updateRefreshStatus(true);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<HeWeather>() {
                    @Override
                    public void accept(HeWeather heWeather) throws Exception {
                        Log.d(TAG, "getWeather: onNext weather isOK = " + heWeather.isOK());
                        onWeatherChange(heWeather);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "getWeather onError: ", throwable);
                        updateRefreshStatus(false);
                        showErrorTip(throwable.getMessage());
                    }
                }));

    }

    @Override
    protected void initView() {
        mCity = getArgCity();

        mAqiRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mAqiAdapter = new AqiAdapter(R.layout.item_weather_aqi, null);
        mAqiAdapter.setDuration(1000);
        mAqiAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        mAqiRecyclerView.setAdapter(mAqiAdapter);

        mSuggestionRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mSuggestionAdapter = new SuggestionAdapter(R.layout.item_suggestion, null);
        mSuggestionAdapter.setDuration(1000);
        mSuggestionAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        mSuggestionRecyclerView.setAdapter(mSuggestionAdapter);
        mWPullRefreshLayout.setOnRefreshListener(this);

        if (mWWeatherScrollView != null)
            mWWeatherScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mWWeatherScrollView.scrollTo(0, 0);
                }
            });
    }

    @Override
    public void onRefresh() {
        if (!TextUtils.isEmpty(mCity.getAreaId())) {
            getWeather(mCity, true);
        }
        if (mCity.getIsLocation() == 1) {
            getLocation();
        }
    }

    public void updateRefreshStatus(final boolean refresh) {
        mWPullRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mWPullRefreshLayout.setRefreshing(refresh);
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        changeDynamicWeather(mWeather);
    }

    private void updateWeatherUI() {
        final HeWeather weather = mWeather;
        updateRefreshStatus(false);
        if (weather == null || !weather.isOK()) {
            return;
        }
        mWeather.setUpdateTime(System.currentTimeMillis());
        try {
            changeDynamicWeather(weather);

            HeWeather.HeWeather5Bean w = weather.getWeather();
            mWDailyForecastView.setData(weather);
            mWHourlyForecastView.setData(weather);
            mWAstroView.setData(weather);
            View layoutNow = mRootView.findViewById(R.id.layout_now);
            View layoutDetails = mRootView.findViewById(R.id.layout_details);

            setTextViewString(R.id.w_now_tmp, getString(R.string.weather_temp, w.getNow().getTmp()));
            setTextViewString(R.id.tv_now_hum, w.getNow().getHum() + "%");// 湿度
            setTextViewString(R.id.tv_now_vis, getString(R.string.weather_km, w.getNow().getVis()));// 能见度
            setTextViewString(R.id.tv_now_pcpn, getString(R.string.weather_mm, w.getNow().getPcpn())); // 降雨量

            layoutNow.setAlpha(0f);
            layoutDetails.setAlpha(0f);
            layoutNow.animate().alpha(1f).setDuration(1000);
            layoutDetails.setTranslationY(-100.0f);
            layoutDetails.animate().translationY(0).setDuration(1000);
            layoutDetails.animate().alpha(1f).setDuration(1000);

            TextView updateTextView = mRootView.findViewById(R.id.w_basic_update_loc);
            updateTextView.setAlpha(0f);
            updateTextView.animate().alpha(1f).setDuration(1000);
            if (FormatUtil.isToday(w.getBasic().getUpdate().getLoc())) {
                setTextViewString(R.id.w_basic_update_loc,
                        getString(R.string.weather_update, w.getBasic().getUpdate().getLoc().substring(11)));
            } else {
                setTextViewString(R.id.w_basic_update_loc,
                        getString(R.string.weather_update, w.getBasic().getUpdate().getLoc().substring(5)));
            }

//            setTextViewString(R.id.w_todaydetail_bottomline, w.getNow().getCond().getTxt() + "  " + weather.getTodayTempDescription());
//            setTextViewString(R.id.w_todaydetail_temp, getString(R.string.weather_temp, w.getNow().getTmp()));
//
//            setTextViewString(R.id.w_now_fl, getString(R.string.weather_temp, w.getNow().getFl()));
//            setTextViewString(R.id.w_now_hum, w.getNow().getHum() + "%");// 湿度
//            setTextViewString(R.id.w_now_vis, getString(R.string.weather_km, w.getNow().getVis()));// 能见度
//            setTextViewString(R.id.w_now_pcpn, getString(R.string.weather_mm, w.getNow().getPcpn())); // 降雨量

            if (weather.hasAqi()) {
                mWAqiView.setAqi(weather);
                setAqiDetail(weather);
                final String qlty = w.getAqi().getCity().getQlty();
                if (TextUtils.isEmpty(qlty)) {
                    setTextViewString(R.id.w_now_cond_text, w.getNow().getCond().getTxt());
                } else {
                    setTextViewString(R.id.w_now_cond_text, w.getNow().getCond().getTxt() + "\r\n" + qlty);
                }
//                setTextViewString(R.id.w_aqi_detail_text, qlty);
//                final String pm25 = w.getAqi().getCity().getPm25();
//                setTextViewString(R.id.w_aqi_pm25, TextUtils.isEmpty(pm25) ? getString(R.string.nodata)
//                        : getString(R.string.weather_ug_m3, pm25));
//                final String pm10 = w.getAqi().getCity().getPm10();
//                setTextViewString(R.id.w_aqi_pm10, TextUtils.isEmpty(pm10) ? getString(R.string.nodata)
//                        : getString(R.string.weather_ug_m3, pm10));
//                final String so2 = w.getAqi().getCity().getSo2();
//                setTextViewString(R.id.w_aqi_so2, TextUtils.isEmpty(so2) ? getString(R.string.nodata)
//                        : getString(R.string.weather_ug_m3, so2));
//                final String no2 = w.getAqi().getCity().getNo2();
//                setTextViewString(R.id.w_aqi_no2, TextUtils.isEmpty(no2) ? getString(R.string.nodata)
//                        : getString(R.string.weather_ug_m3, no2));
            } else {
                setTextViewString(R.id.w_now_cond_text, w.getNow().getCond().getTxt());
//                mRootView.findViewById(R.id.air_quality_item).setVisibility(View.GONE);
            }
            setSuggesstion(weather);
//            if (w.getSuggestion() != null) {
//                setTextViewString(R.id.w_suggestion_comf, w.getSuggestion().getComf().getTxt());
//                setTextViewString(R.id.w_suggestion_cw, w.getSuggestion().getCw().getTxt());
//                setTextViewString(R.id.w_suggestion_drsg, w.getSuggestion().getDrsg().getTxt());
//                setTextViewString(R.id.w_suggestion_flu, w.getSuggestion().getFlu().getTxt());
//                setTextViewString(R.id.w_suggestion_sport, w.getSuggestion().getSport().getTxt());
//                setTextViewString(R.id.w_suggestion_tarv, w.getSuggestion().getTrav().getTxt());
//                setTextViewString(R.id.w_suggestion_uv, w.getSuggestion().getUv().getTxt());
//                setTextViewString(R.id.w_suggestion_comf_brf, w.getSuggestion().getComf().getBrf());
//                setTextViewString(R.id.w_suggestion_cw_brf, w.getSuggestion().getCw().getBrf());
//                setTextViewString(R.id.w_suggestion_drsg_brf, w.getSuggestion().getDrsg().getBrf());
//                setTextViewString(R.id.w_suggestion_flu_brf, w.getSuggestion().getFlu().getBrf());
//                setTextViewString(R.id.w_suggestion_sport_brf, w.getSuggestion().getSport().getBrf());
//                setTextViewString(R.id.w_suggestion_tarv_brf, w.getSuggestion().getTrav().getBrf());
//                setTextViewString(R.id.w_suggestion_uv_brf, w.getSuggestion().getUv().getBrf());
//            } else {
//                mRootView.findViewById(R.id.index_item).setVisibility(View.GONE);
//            }
        } catch (Exception e) {
            e.printStackTrace();
            toast(mCity.getCity() + " Error\n" + e.toString());
        }
    }

    private void setAqiDetail(HeWeather weather) {
        List<AqiDetailBean> list = new ArrayList<>();
        AqiDetailBean pm25 = new AqiDetailBean("PM2.5", "细颗粒物", weather.getWeather().getAqi().getCity().getPm25());
        list.add(pm25);
        AqiDetailBean pm10 = new AqiDetailBean("PM10", "可吸入颗粒物", weather.getWeather().getAqi().getCity().getPm10());
        list.add(pm10);
        AqiDetailBean so2 = new AqiDetailBean("SO2", "二氧化硫", weather.getWeather().getAqi().getCity().getSo2());
        list.add(so2);
        AqiDetailBean no2 = new AqiDetailBean("NO2", "二氧化氮", weather.getWeather().getAqi().getCity().getNo2());
        list.add(no2);
        AqiDetailBean co = new AqiDetailBean("CO", "一氧化碳", weather.getWeather().getAqi().getCity().getCo());
        list.add(co);
        AqiDetailBean o3 = new AqiDetailBean("O3", "臭氧", weather.getWeather().getAqi().getCity().getO3());
        list.add(o3);
        mAqiAdapter.setNewData(list);
    }

    private void setSuggesstion(HeWeather weather) {
        mSuggestionAdapter.setNewData(WeatherUtil.getSuggestion(weather));
    }

    private void changeDynamicWeather(HeWeather weather) {
        if (getUserVisibleHint() && mWeather != null && mWeather.isOK()) {
            BaseWeatherType type = TypeUtil.getType(getResources(), getShortWeatherInfo(weather));
            mListener.onDrawerTypeChange(type);
            //RxBus.getInstance().post(type);
        }
    }

    @NonNull
    private ShortWeatherInfo getShortWeatherInfo(HeWeather weather) {
        ShortWeatherInfo info = new ShortWeatherInfo();
        info.setCode(weather.getWeather().getNow().getCond().getCode());
        info.setWindSpeed(weather.getWeather().getNow().getWind().getSpd());
        info.setSunrise(weather.getWeather().getDaily_forecast().get(0).getAstro().getSr());
        info.setSunset(weather.getWeather().getDaily_forecast().get(0).getAstro().getSs());
        info.setMoonrise(weather.getWeather().getDaily_forecast().get(0).getAstro().getMr());
        info.setMoonset(weather.getWeather().getDaily_forecast().get(0).getAstro().getMs());
        return info;
    }

    private void setTextViewString(int textViewId, String str) {

        TextView tv = mRootView.findViewById(textViewId);
        if (tv != null) {
            tv.setText(str);
        } else {
            toast("Error NOT found textView id->" + Integer.toHexString(textViewId));
        }
    }

    protected void toast(String msg) {
        //Snackbar.make(mWPullRefreshLayout, msg, Snackbar.LENGTH_SHORT).show();
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void getLocation() {
        Log.d(TAG, "getLocation: start...");
        mDisposable.add(new RxPermissions(getActivity())
                .requestEachCombined(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .observeOn(AndroidSchedulers.mainThread())
                .concatMap(new Function<Permission, ObservableSource<City>>() {
                    @Override
                    public ObservableSource<City> apply(Permission permission) throws Exception {
                        if (!permission.granted) {
                            throw new Exception(getString(R.string.permission_message));
                        }
                        return mViewModel.getLocation();
                    }
                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) {
                        updateRefreshStatus(true);
                    }
                })
                .compose(RxSchedulers.<City>io_main())
                .subscribe(new Consumer<City>() {
                    @Override
                    public void accept(City city) {
                        Log.d(TAG, "requestLocation: onNext city = " + city);
                        if (!TextUtils.equals(city.getAreaId(), mCity.getAreaId()))
                            onLocationChange(city);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Log.e(TAG, "requestLocation: onError ", throwable);
                        updateRefreshStatus(false);
                        showErrorTip(throwable.getMessage());
                    }
                }));
    }

    public interface OnDrawerTypeChangeListener {
        void onDrawerTypeChange(BaseWeatherType type);

        void onLocationChange(City city);
    }

}
