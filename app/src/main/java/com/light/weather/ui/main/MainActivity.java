package com.light.weather.ui.main;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar;
import com.light.weather.R;
import com.light.weather.bean.City;
import com.light.weather.ui.base.BaseActivity;
import com.light.weather.ui.common.WeatherViewModel;
import com.light.weather.ui.manage.ManageActivity;
import com.light.weather.ui.weather.WeatherFragment;
import com.light.weather.util.RxSchedulers;
import com.light.weather.util.UiUtil;
import com.light.weather.widget.SimplePagerIndicator;
import com.light.weather.widget.dynamic.BaseWeatherType;
import com.light.weather.widget.dynamic.DynamicWeatherView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import io.reactivex.functions.Consumer;


public class MainActivity extends BaseActivity
        implements WeatherFragment.OnDrawerTypeChangeListener, HasSupportFragmentInjector {
    public static final String UNKNOWN_CITY = "unknown";
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_CITY = 0;
    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;
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
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private MainFragmentPagerAdapter mAdapter;
    private List<City> mCities = new ArrayList<>();
    private int mSelectItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.AppTheme_NoActionBar);
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(WeatherViewModel.class);

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mMainViewPager.setPadding(0, UiUtil.getStatusBarHeight() + UiUtil.getActionBarHeight(), 0, 0);
        mAppBarLayout.setPadding(0, UiUtil.getStatusBarHeight(), 0, 0);
        ((ViewGroup) mIndicator.getParent()).setPadding(0, UiUtil.getStatusBarHeight(), 0, 0);

        setSupportActionBar(mToolbar);
        setupToolBar();
        setupViewPager();

        getCities();//加载城市列表
    }

    private void setupViewPager() {
        mAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager());
        mMainViewPager.setAdapter(mAdapter);
        mIndicator.setViewPager(mMainViewPager);
    }

    private void setupToolBar() {
        setTitle("");
        mToolbar.setNavigationIcon(R.drawable.ic_location_city);
        mDisposable.add(RxToolbar.navigationClicks(mToolbar)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        startActivityForResult(new Intent(MainActivity.this, ManageActivity.class), REQUEST_CODE_CITY);
                    }
                }));
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
        mDynamicWeatherView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDynamicWeatherView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDynamicWeatherView.onDestroy();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    public void onCityChange(List<City> cities) {
        Log.d(TAG, "onCityChange: cities = " + cities);
        mCities = cities;
        if (mCities != null && !mCities.isEmpty()) {
            mAdapter.setNewData(mCities);
            //mMainViewPager.setAdapter(mAdapter);
            //mMainViewPager.setOffscreenPageLimit(mAdapter.getCount() - 1);
            if (mSelectItem >= 0 && mSelectItem < mAdapter.getCount()) {
                mMainViewPager.setCurrentItem(mSelectItem);
                mIndicator.notifyDataSetChanged();
                mSelectItem = -1;
            }
        }
    }

    public void getCities() {
        Log.d(TAG, "getCities: start.... mViewModel = " + mViewModel);
        mDisposable.add(mViewModel.getCities()
                .compose(RxSchedulers.<List<City>>io_main())
                .subscribe(new Consumer<List<City>>() {
                    @Override
                    public void accept(List<City> cities) {
                        Log.d(TAG, "getCities onNext: cities = " + cities.size());
                        onCityChange(cities);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getApplicationContext(), "getCities onError: " + throwable, Toast.LENGTH_LONG).show();
                        Log.d(TAG, "getCities onError: ", throwable);
                    }
                }));
    }

    @Override
    public void onDrawerTypeChange(BaseWeatherType type) {
        mDynamicWeatherView.setType(type);
    }

    @Override
    public void onLocationChange(City city) {
        int index = -1;
        for (int i = 0; i < mCities.size(); i++) {
            if (mCities.get(i).getIsLocation() == 1) {
                index = i;
                break;
            }
        }
        Log.d(TAG, "onLocationChange: city = " + city + ", location city index = " + index);
        if (index >= 0 && index < mCities.size()) {
            mAdapter.replace(index, city.getCity());
            mIndicator.notifyDataSetChanged();
        }
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

    public static class MainFragmentPagerAdapter extends FragmentPagerAdapter {
        private FragmentManager mFragmentManager;
        private List<Fragment> mFragments;
        private List<String> mTitles;

        MainFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            mFragmentManager = fragmentManager;
            this.mFragments = new ArrayList<>();
            this.mTitles = new ArrayList<>();
        }

        void setNewData(List<City> cities) {
            if (!mFragments.isEmpty()) {
                FragmentTransaction ft = mFragmentManager.beginTransaction();
                for (Fragment f : mFragments) {
                    ft.remove(f);
                }
                ft.commitAllowingStateLoss();
                mFragmentManager.executePendingTransactions();
            }
            mFragments.clear();
            mTitles.clear();
            for (City city : cities) {
                Log.i(TAG, "setNewData: city = " + city.getCity());
                mFragments.add(WeatherFragment.makeInstance(city));
                mTitles.add(TextUtils.isEmpty(city.getCity()) ? UNKNOWN_CITY : city.getCity());
            }
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = mFragments.get(position);
            fragment.setRetainInstance(true);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            final CharSequence charSequence = mTitles.get(position);
            return TextUtils.isEmpty(charSequence) ? UNKNOWN_CITY : charSequence;
        }

        @Override
        public int getCount() {
            return mFragments.size();
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
        public int getItemPosition(@NonNull Object object) {
            return super.getItemPosition(object);
//            return POSITION_NONE;
        }

        void replace(int index, String city) {
            mTitles.remove(index);
            mTitles.add(index, city);
        }
    }
}
