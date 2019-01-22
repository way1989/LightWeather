package com.light.weather.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar;
import com.light.weather.R;
import com.light.weather.bean.City;
import com.light.weather.ui.base.BaseDagger2Activity;
import com.light.weather.ui.manage.ManageActivity;
import com.light.weather.ui.weather.WeatherFragment;
import com.light.weather.util.RxSchedulers;
import com.light.weather.util.UiUtil;
import com.light.weather.viewmodel.WeatherViewModel;
import com.light.weather.widget.SimplePagerIndicator;
import com.light.weather.widget.dynamic.BaseWeatherType;
import com.light.weather.widget.dynamic.DynamicWeatherView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;


public class MainActivity extends BaseDagger2Activity<WeatherViewModel>
        implements WeatherFragment.OnDrawerTypeChangeListener {
    public static final String UNKNOWN_CITY = "unknown";
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_CITY = 0;
    @BindView(R.id.dynamic_weather_view)
    DynamicWeatherView mDynamicWeatherView;
    @BindView(R.id.main_view_pager)
    ViewPager mMainViewPager;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.app_bar_layout)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.indicator_spring)
    SimplePagerIndicator mIndicator;
    private MainFragmentPagerAdapter<City> mAdapter;
    private List<City> mCities;
    private int mSelectItem = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);

        mMainViewPager.setPadding(0, UiUtil.getStatusBarHeight() + UiUtil.getActionBarHeight(), 0, 0);
        mAppBarLayout.setPadding(0, UiUtil.getStatusBarHeight(), 0, 0);
        ((ViewGroup) mIndicator.getParent()).setPadding(0, UiUtil.getStatusBarHeight(), 0, 0);

        setSupportActionBar(mToolbar);
        setupToolBar();
        setupViewPager();

        getCities();//加载城市列表
    }

    private void setupViewPager() {
        mAdapter = new MainFragmentPagerAdapter<>(getSupportFragmentManager(), null);
        mMainViewPager.setAdapter(mAdapter);
        mMainViewPager.setOffscreenPageLimit(8);//最多缓存9个
        mIndicator.setViewPager(mMainViewPager);
    }

    private void setupToolBar() {
        setTitle("");
        mToolbar.setNavigationIcon(R.drawable.ic_location_city);
        mDisposable.add(RxToolbar.navigationClicks(mToolbar)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> startActivityForResult(new Intent(MainActivity.this, ManageActivity.class), REQUEST_CODE_CITY)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CITY && resultCode == RESULT_OK) {
            final boolean changed = data.getBooleanExtra(ManageActivity.EXTRA_DATA_CHANGED, false);
            mSelectItem = data.getIntExtra(ManageActivity.EXTRA_SELECTED_ITEM, -1);
            if (changed) {
                getCities();
            } else if (mSelectItem >= 0 && mSelectItem < mAdapter.getCount()) {
                mMainViewPager.setCurrentItem(mSelectItem);
                mSelectItem = -1;
            }
        }
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        mDynamicWeatherView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
        mDynamicWeatherView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        mDynamicWeatherView.onDestroy();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    public void updateAdapter(List<City> cities) {
        Log.d(TAG, "updateAdapter: cities = " + cities);
        mCities = cities;
        if (mCities != null && !mCities.isEmpty()) {
            mAdapter.setNewData(mCities);
            if (mSelectItem >= 0 && mSelectItem < mAdapter.getCount()) {
                mMainViewPager.setCurrentItem(mSelectItem);
                mSelectItem = -1;
            }
            mIndicator.notifyDataSetChanged();
        }
    }

    public void getCities() {
        Log.d(TAG, "getCities: start.... mViewModel = " + mViewModel);
        mDisposable.add(mViewModel.getCities(false)
                .compose(RxSchedulers.io_main())
                .subscribe(cities -> updateAdapter(cities),
                        throwable -> {
                            Toast.makeText(getApplicationContext(), "getCities onError: " + throwable, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "getCities onError: ", throwable);
                        }));
    }

    @Override
    public void onDrawerTypeChange(BaseWeatherType type) {
        mDynamicWeatherView.setType(type);
    }

    @Override
    public void updateLocationCity(City city) {
        for (int i = 0; i < mCities.size(); i++) {
            if (mCities.get(i).isLocation()) {
                Log.d(TAG, "updateLocationCity: city = " + city + ", location city index = " + i);
                mCities.set(i, city);
                mAdapter.setNewData(mCities);
                mIndicator.notifyDataSetChanged();
                break;
            }
        }
    }

    public static class MainFragmentPagerAdapter<T extends City> extends FragmentStatePagerAdapter {
        private boolean isUpdateData;
        private List<T> mData;

        MainFragmentPagerAdapter(FragmentManager fragmentManager, List<T> data) {
            super(fragmentManager);
            this.mData = data == null ? new ArrayList<T>() : data;
            isUpdateData = false;
        }

        void setNewData(List<T> data) {
            this.mData = data == null ? new ArrayList<T>() : data;
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = WeatherFragment.makeInstance(mData.get(position));
            fragment.setRetainInstance(true);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            final CharSequence charSequence = mData.get(position).getCity();
            return TextUtils.isEmpty(charSequence) ? UNKNOWN_CITY : charSequence;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(((Fragment) object).getView());
            super.destroyItem(container, position, object);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public void notifyDataSetChanged() {
            isUpdateData = true;
            super.notifyDataSetChanged();
            isUpdateData = false;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            if (isUpdateData) return POSITION_NONE;
            return super.getItemPosition(object);
        }

    }
}
