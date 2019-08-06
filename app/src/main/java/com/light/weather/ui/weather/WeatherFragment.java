package com.light.weather.ui.weather;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.light.weather.BuildConfig;
import com.light.weather.R;
import com.light.weather.adapter.AqiAdapter;
import com.light.weather.adapter.SuggestionAdapter;
import com.light.weather.bean.AqiDetailBean;
import com.light.weather.bean.City;
import com.light.weather.bean.HeBasic;
import com.light.weather.bean.HeWeather6;
import com.light.weather.ui.base.BaseDagger2Fragment;
import com.light.weather.util.RxSchedulers;
import com.light.weather.util.ShareUtils;
import com.light.weather.util.UiUtil;
import com.light.weather.util.WeatherUtil;
import com.light.weather.viewmodel.WeatherViewModel;
import com.light.weather.widget.AqiView;
import com.light.weather.widget.AstroView;
import com.light.weather.widget.DailyForecastView;
import com.light.weather.widget.HourlyForecastView;
import com.light.weather.widget.dynamic.DefaultType;
import com.light.weather.widget.dynamic.ShortWeatherInfo;
import com.light.weather.widget.dynamic.WeatherType;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;

/**
 * Created by android on 16-11-10.
 */

public class WeatherFragment extends BaseDagger2Fragment<WeatherViewModel> implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "WeatherFragment";
    private static final String[] REQUEST_PERMISSIONS = BuildConfig.DEBUG
            ? new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE}
            : new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final long WEATHER_DIRTY_TIME = 30 * 60 * 1000L;
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
    @BindView(R.id.divider_now_and_forecast)
    View mDividerNowAndForecast;
    @BindView(R.id.divider_hourly_forecast)
    View mDividerHourlyForecast;
    @BindView(R.id.container_aqi)
    RelativeLayout mContainerAqi;
    @BindView(R.id.divider_aqi)
    View mDividerAqi;
    @BindView(R.id.divider_astro)
    View mDividerAstro;
    @BindView(R.id.divider_suggestion)
    View mDividerSuggestion;
    @BindView(R.id.bottom_title)
    TextView mBottomTitle;
    private OnDrawerTypeChangeListener mListener;
    private City mCity;
    private SuggestionAdapter mSuggestionAdapter;
    private AqiAdapter mAqiAdapter;
    private long mLastClickMenuTime;

    public static WeatherFragment makeInstance(@NonNull City city) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_KEY, city);
        fragment.setArguments(bundle);
        return fragment;
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
                if (System.currentTimeMillis() - mLastClickMenuTime > 2000L) {
                    Log.d(TAG, "onOptionsItemSelected: id = share...");
                    final Activity activity = getActivity();
                    mDisposable.add(Single.just(activity)
                            .map((Function<Context, String>) context -> {
                                Bitmap backgroundBitmap = mListener.getBackgroundBitmap();
                                String dir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
                                SimpleDateFormat format = new SimpleDateFormat("'Weather'_yyyyMMdd_HHmmss'.jpg'", Locale.US);
                                String title = format.format(new Date(System.currentTimeMillis()));
                                Bitmap bitmap = UiUtil.getBitmapByView(mWWeatherScrollView, backgroundBitmap);

                                UiUtil.saveToGallery(context, bitmap, dir, title, Bitmap.CompressFormat.JPEG, 100);
                                return dir + File.separator + title;
                            })
                            .compose(RxSchedulers.io_main_single())
                            .subscribe(path -> {
                                Log.d(TAG, "onSuccess: " + path);
                                final HeWeather6 weather = getWeather();
                                if (activity != null && weather != null) {
                                    final String shareInfo = WeatherUtil.getInstance().getShareMessage(weather);
                                    Uri contentUri = null;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        contentUri = FileProvider.getUriForFile(activity,
                                                BuildConfig.APPLICATION_ID + ".fileProvider",
                                                new File(path));
                                    } else {
                                        contentUri = Uri.parse("file://" + path);
                                    }
                                    ShareUtils.shareImage(activity,contentUri, shareInfo);
                                }
                            }, throwable -> Log.w(TAG, "onError: ", throwable))
                    );
                    mLastClickMenuTime = System.currentTimeMillis();
                }
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
        Log.i(TAG, "onResume: .........");
        /*if (getUserVisibleHint() && isDataDirty() && mCity != null) {
            Log.i(TAG, "onResume: city = " + mCity.getCity()
                    + ", isDirty = " + isDataDirty());
            getWeather(mCity, mCity.isLocation());
        }*/
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_weather;
    }

    public void onLocationChange(City city) {
        if (mListener != null) {
            mListener.updateLocationCity(city);
        }
    }

    @Override
    public void loadDataFirstTime() {
        Log.d(TAG, "loadDataFirstTime: .............");
        if (getActivity() == null || mCity == null) {
            toast("city is null");
            Log.e(TAG, "loadDataFirstTime: ", new Throwable("something is null..."));
            return;
        }
        requestWeather();
    }

    private HeWeather6 getWeather() {
        if (mRootView == null) {
            return null;
        }
        Object weather = mRootView.getTag();
        if (!(weather instanceof HeWeather6)) {
            return null;
        }
        return (HeWeather6) weather;
    }

    @Override
    public boolean isDataDirty() {
        HeWeather6 weather = getWeather();
        if (weather == null) {
            return false;
        }
        final boolean isDirty = mCity != null && !TextUtils.isEmpty(mCity.getAreaId())
                && (System.currentTimeMillis() - weather.getUpdateTime() > WEATHER_DIRTY_TIME);
        Log.v(TAG, "isDataDirty: " + isDirty + ", city = " + mCity);
        return isDirty;
    }

    private void getWeather(String areaId, final boolean isLocation) {
        Log.i(TAG, "getWeather... areaId = " + areaId);
        updateRefreshStatus(true);
        mDisposable.add(mViewModel.getWeather(areaId, isLocation)
                .compose(RxSchedulers.io_main())
                .subscribe(heWeather6 -> {
                            Log.d(TAG, "getWeather: onNext heWeatherS6 = " + heWeather6);
                            updateWeatherUI(heWeather6);
                            getAqi(areaId);
                            if (isLocation) {
                                getLocation();
                            }
                        },
                        throwable -> {
                            Log.e(TAG, "getWeather: onError", throwable);
                            updateWeatherUI(null);
                            toast(throwable.getMessage());
                        }));
    }

    private void getAqi(String areaId) {
        mDisposable.add(mViewModel.getAqi(areaId)
                .compose(RxSchedulers.io_main())
                .subscribe(this::updateAqi, throwable ->
                        Log.e(TAG, "getAqi: onError " + throwable.getMessage())));
    }

    private void updateAqi(HeWeather6.AqiBean aqiBean) {
        Log.d(TAG, "updateAqi: aqi = " + aqiBean);
        if (aqiBean != null) {
            HeWeather6 weather = getWeather();
            if (weather != null) {
                weather.setAqi(aqiBean);
                mRootView.setTag(weather);
            }
            mContainerAqi.setVisibility(View.VISIBLE);
            mDividerAqi.setVisibility(View.VISIBLE);
            mWAqiView.setAqi(weather.getAqi());
            setAqiDetail(weather.getAqi());
            final String qlty = weather.getAqi().getQlty();
            if (TextUtils.isEmpty(qlty)) {
                setTextViewString(R.id.w_now_cond_text, weather.getNow().getCond_txt());
            } else {
                setTextViewString(R.id.w_now_cond_text, weather.getNow().getCond_txt() + "\r\n" + qlty);
            }
        } else {
            mContainerAqi.setVisibility(View.GONE);
            mDividerAqi.setVisibility(View.GONE);
            //setTextViewString(R.id.w_now_cond_text, weather.getNow().getCond_txt());
        }
    }

    @Override
    protected void initView() {
        final Bundle bundle = getArguments();
        if (bundle != null) {
            mCity = (City) bundle.getSerializable(ARG_KEY);
        }

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
            mWWeatherScrollView.post(() -> mWWeatherScrollView.scrollTo(0, 0));
    }

    @Override
    public void onRefresh() {
        requestWeather();
    }

    private void requestWeather() {
        if (!TextUtils.isEmpty(mCity.getAreaId())) {
            getWeather(mCity.getAreaId(), mCity.isLocation());
        } else if (mCity.isLocation()) {
            getLocation();
        }
    }

    public void updateRefreshStatus(final boolean refresh) {
        mWPullRefreshLayout.post(() -> mWPullRefreshLayout.setRefreshing(refresh));
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser || mRootView == null) {
            return;
        }
        HeWeather6 weather = getWeather();
        changeDynamicWeather(weather);
    }

    private void updateWeatherUI(HeWeather6<HeBasic> weather) {
        updateRefreshStatus(false);
        mRootView.setTag(weather);
        if (weather == null) {
            hideViews();
            return;
        }
        weather.setUpdateTime(System.currentTimeMillis());
        try {
            changeDynamicWeather(weather);

            mWHourlyForecastView.setVisibility(View.VISIBLE);
            mDividerHourlyForecast.setVisibility(View.VISIBLE);
            mWAstroView.setVisibility(View.VISIBLE);
            mDividerAstro.setVisibility(View.VISIBLE);

            mWDailyForecastView.setData(weather.getDaily_forecast());
            mWHourlyForecastView.setData(weather.getHourly());
            mWAstroView.setData(weather);
            View layoutNow = mRootView.findViewById(R.id.layout_now);
            View layoutDetails = mRootView.findViewById(R.id.layout_details);

            setTextViewString(R.id.w_now_cond_text, weather.getNow().getCond_txt());
            setTextViewString(R.id.w_now_tmp, getString(R.string.weather_temp, weather.getNow().getTmp()));
            setTextViewString(R.id.tv_now_hum, weather.getNow().getHum() + "%");// 湿度
            setTextViewString(R.id.tv_now_vis, getString(R.string.weather_km, weather.getNow().getVis()));// 能见度
            setTextViewString(R.id.tv_now_pcpn, getString(R.string.weather_mm, weather.getNow().getPcpn())); // 降雨量

            layoutNow.setAlpha(0f);
            layoutDetails.setAlpha(0f);
            layoutNow.animate().alpha(1f).setDuration(1000);
            layoutDetails.setTranslationY(-100.0f);
            layoutDetails.animate().translationY(0).setDuration(1000);
            layoutDetails.animate().alpha(1f).setDuration(1000);

            TextView updateTextView = mRootView.findViewById(R.id.w_basic_update_loc);
            updateTextView.setAlpha(0f);
            updateTextView.animate().alpha(1f).setDuration(1000);
            if (WeatherUtil.isToday(weather.getUpdate().getLoc())) {
                setTextViewString(R.id.w_basic_update_loc,
                        getString(R.string.weather_update, weather.getUpdate().getLoc().substring(11)));
            } else {
                setTextViewString(R.id.w_basic_update_loc,
                        getString(R.string.weather_update, weather.getUpdate().getLoc().substring(5)));
            }

            //空气质量
            /*if (weather.getAqi() != null) {
                mContainerAqi.setVisibility(View.VISIBLE);
                mDividerAqi.setVisibility(View.VISIBLE);
                mWAqiView.setAqi(weather.getAqi());
                setAqiDetail(weather.getAqi());
                final String qlty = weather.getAqi().getQlty();
                if (TextUtils.isEmpty(qlty)) {
                    setTextViewString(R.id.w_now_cond_text, weather.getNow().getCond_txt());
                } else {
                    setTextViewString(R.id.w_now_cond_text, weather.getNow().getCond_txt() + "\r\n" + qlty);
                }
            } else {
                mContainerAqi.setVisibility(View.GONE);
                mDividerAqi.setVisibility(View.GONE);
                setTextViewString(R.id.w_now_cond_text, weather.getNow().getCond_txt());
            }*/
            //生活指数
            if (weather.getLifestyle() != null) {
                mDividerSuggestion.setVisibility(View.VISIBLE);
                mSuggestionRecyclerView.setVisibility(View.VISIBLE);
                setSuggestion(weather);
            } else {
                mDividerSuggestion.setVisibility(View.GONE);
                mSuggestionRecyclerView.setVisibility(View.GONE);
            }
            //底部标题
            mBottomTitle.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            toast(mCity.getCity() + " Error:" + e);
        }
    }

    private void hideViews() {
        mWHourlyForecastView.setVisibility(View.GONE);
        mDividerHourlyForecast.setVisibility(View.GONE);
        mContainerAqi.setVisibility(View.GONE);
        mDividerAqi.setVisibility(View.GONE);
        mWAstroView.setVisibility(View.GONE);
        mDividerAstro.setVisibility(View.GONE);
        mSuggestionRecyclerView.setVisibility(View.GONE);
        mDividerSuggestion.setVisibility(View.GONE);
        mBottomTitle.setVisibility(View.GONE);
    }

    private void setAqiDetail(HeWeather6.AqiBean weather) {
        List<AqiDetailBean> list = new ArrayList<>();
        AqiDetailBean pm25 = new AqiDetailBean("PM2.5", "细颗粒物", weather.getPm25());
        list.add(pm25);
        AqiDetailBean pm10 = new AqiDetailBean("PM10", "可吸入颗粒物", weather.getPm10());
        list.add(pm10);
        AqiDetailBean so2 = new AqiDetailBean("SO2", "二氧化硫", weather.getSo2());
        list.add(so2);
        AqiDetailBean no2 = new AqiDetailBean("NO2", "二氧化氮", weather.getNo2());
        list.add(no2);
        AqiDetailBean co = new AqiDetailBean("CO", "一氧化碳", weather.getCo());
        list.add(co);
        AqiDetailBean o3 = new AqiDetailBean("O3", "臭氧", weather.getO3());
        list.add(o3);
        mAqiAdapter.setNewData(list);
    }

    private void setSuggestion(HeWeather6<HeBasic> weather) {
        mSuggestionAdapter.setNewData(weather.getLifestyle());
    }

    private void changeDynamicWeather(HeWeather6<HeBasic> weather) {
        if (isAdded() && getUserVisibleHint()) {
            WeatherType type = null;
            if (weather != null) {
                type = WeatherUtil.getType(getResources(), getShortWeatherInfo(weather));
            }
            mListener.onDrawerTypeChange(type == null ? new DefaultType(getResources()) : type);
        }
    }

    @NonNull
    private ShortWeatherInfo getShortWeatherInfo(HeWeather6<HeBasic> weather) {
        ShortWeatherInfo info = new ShortWeatherInfo();
        info.setCode(weather.getNow().getCond_code());
        info.setWindSpeed(weather.getNow().getWind_spd());
        info.setSunrise(weather.getDaily_forecast().get(0).getSr());
        info.setSunset(weather.getDaily_forecast().get(0).getSs());
        info.setMoonrise(weather.getDaily_forecast().get(0).getMr());
        info.setMoonset(weather.getDaily_forecast().get(0).getMs());
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

    private void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void getLocation() {
        mDisposable.add(new RxPermissions(this)
                .requestEachCombined(REQUEST_PERMISSIONS)
                .observeOn(AndroidSchedulers.mainThread())
                .concatMap((Function<Permission, ObservableSource<City>>) permission -> {
                    if (!permission.granted) {
                        throw new Exception(getString(R.string.permission_message));
                    }
                    updateRefreshStatus(true);
                    return mViewModel.getLocation();
                })
                .compose(RxSchedulers.io_main())
                .subscribe(city -> {
                    Log.d(TAG, "requestLocation: onNext city = " + city.getCity() + ", mCity = " + mCity.getCity());
                    updateRefreshStatus(false);
                    if (!city.getCity().contains(mCity.getCity())) {
                        onLocationChange(city);
                    }
                }, throwable -> {
                    Log.e(TAG, "requestLocation: onError ", throwable);
                    updateRefreshStatus(false);
                    toast(throwable.toString());
                }));
    }

    public interface OnDrawerTypeChangeListener {
        void onDrawerTypeChange(WeatherType type);

        void updateLocationCity(City city);

        Bitmap getBackgroundBitmap();
    }

}
