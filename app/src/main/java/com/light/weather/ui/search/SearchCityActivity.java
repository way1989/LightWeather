package com.light.weather.ui.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;
import com.light.weather.BuildConfig;
import com.light.weather.R;
import com.light.weather.adapter.SearchAdapter;
import com.light.weather.bean.City;
import com.light.weather.ui.base.BaseDagger2NonFragmentInjectorActivity;
import com.light.weather.util.AppConstant;
import com.light.weather.util.RxSchedulers;
import com.light.weather.viewmodel.WeatherViewModel;
import com.light.weather.widget.SimpleListDividerDecorator;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SearchCityActivity extends BaseDagger2NonFragmentInjectorActivity<WeatherViewModel>
        implements MenuItem.OnActionExpandListener {
    private static final String TAG = "SearchCityActivity";
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    private SearchAdapter mSearchAdapter;
    private SearchView mSearchView;

    private View mLoadingView;
    private View mEmptyView;
    private TextView mLocationView;
    private TagFlowLayout mFlowLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        LayoutInflater inflater = getLayoutInflater();
        mLoadingView = inflater.inflate(R.layout._loading_layout_loading, null);
        mEmptyView = inflater.inflate(R.layout.search_layout_empty, null);
        mLocationView = mEmptyView.findViewById(R.id.search_location_item);
        mFlowLayout = mEmptyView.findViewById(R.id.hot_city_flow_layout);

        mDisposable.add(mViewModel.getLocationCity()
                .compose(RxSchedulers.all_io_single())
                .subscribe(this::setLocationCity, this::snack));
        mLocationView.setOnClickListener(v -> onItemClick((City) v.getTag()));

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setOnTouchListener((v, event) -> {
            hideInputManager();
            return false;
        });
        mSearchAdapter = new SearchAdapter(R.layout.item_search_city, null);
        mSearchAdapter.setOnItemClickListener((adapter, view, position) -> {
            final City city = (City) view.getTag();
            SearchCityActivity.this.onItemClick(city);
        });
        mRecyclerView.setAdapter(mSearchAdapter);
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat
                .getDrawable(this, R.drawable.list_divider_h), false));

        loadDefaultCities();
    }

    private void setLocationCity(City city) {
        mSearchAdapter.setEmptyView(mEmptyView);
        mLocationView.setTag(city);
        mLocationView.setText(city.getCity());
    }

    private void loadDefaultCities() {
        mSearchAdapter.setNewData(null);
        mSearchAdapter.setEmptyView(mLoadingView);
        mDisposable.add(mViewModel.getDefaultCities()
                .compose(RxSchedulers.io_main_single())
                .subscribe(this::showDefaultData, this::snack));
    }


    public void showDefaultData(final List<City> cities) {
        mFlowLayout.setAdapter(new TagAdapter<City>(cities) {
            @Override
            public View getView(FlowLayout parent, int position, City city) {
                TextView tv = (TextView) LayoutInflater.from(SearchCityActivity.this)
                        .inflate(R.layout.flow_layout_tv, parent, false);
                tv.setTag(city);
                tv.setText(city.getCity());
                return tv;
            }
        });
        mFlowLayout.setOnTagClickListener((view, position, parent) -> {
            City city = cities.get(position);
            onItemClick(city);
            return false;
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search_city;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) searchMenuItem.getActionView();
        mSearchView.setQueryHint(getString(R.string.search_city));
        searchMenuItem.expandActionView();
        searchMenuItem.setOnActionExpandListener(this);

        mDisposable.add(RxSearchView.queryTextChanges(mSearchView)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::search));
        return super.onCreateOptionsMenu(menu);
    }


    private void search(CharSequence query) {
        if (TextUtils.isEmpty(query)) {
            onSearchResult(null);
        } else {
            mSearchAdapter.setNewData(null);
            mSearchAdapter.setEmptyView(mLoadingView);
            mDisposable.add(mViewModel.search(query)
                    .compose(RxSchedulers.io_main())
                    .subscribe(this::onSearchResult, this::onSearchError));
        }
    }

    public void hideInputManager() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
        }
        if (mSearchView != null) {
            mSearchView.clearFocus();
        }
    }

    public void onSearchResult(List<City> cities) {
        Log.d(TAG, "onSearchResult... cities = " + cities);
        if (cities == null || cities.isEmpty()) {
            mSearchAdapter.setNewData(null);
            mSearchAdapter.setEmptyView(mEmptyView);
        } else {
            mSearchAdapter.setNewData(cities);
        }
    }

    public void onSearchError(Throwable e) {
        Log.d(TAG, "onSearchError... e = " + e.getMessage());
        mSearchAdapter.setNewData(null);
        mSearchAdapter.setEmptyView(mEmptyView);
        if (BuildConfig.LOG_DEBUG) {
            snack(e.toString());
        }
    }

    private void snack(Throwable throwable) {
        Snackbar.make(mRecyclerView, throwable.toString(), Snackbar.LENGTH_SHORT).show();
    }

    private void snack(String msg) {
        Snackbar.make(mRecyclerView, msg, Snackbar.LENGTH_SHORT).show();
    }

    private void snack(@StringRes int msg) {
        Snackbar.make(mRecyclerView, msg, Snackbar.LENGTH_SHORT).show();
    }

    public void onSaveCitySucceed(City city) {
        Log.d(TAG, "onSaveCitySucceed city = " + city);
        if (city == null) {
            snack(R.string.city_exist);
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(AppConstant.ARG_CITY_KEY, city);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onItemClick(final City city) {
        Log.d(TAG, "onItemClick city = " + city);
        hideInputManager();
        if (city == null) {
            snack("city is null...");
            return;
        }
        if (city.isLocation()) {
            mSearchAdapter.setNewData(null);
            mSearchAdapter.setEmptyView(mLoadingView);
            mDisposable.add(mViewModel.getLocation()
                    .compose(RxSchedulers.io_main())
                    .subscribe(this::setLocationCity, this::snack));
        } else {
            mDisposable.add(mViewModel.addCity(city)
                    .compose(RxSchedulers.io_main())
                    .subscribe(this::onSaveCitySucceed,
                            throwable -> snack(R.string.city_exist)));
        }
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        if (TextUtils.isEmpty(mSearchView.getQuery())) {
            finish();
        } else {
            mSearchView.setQuery("", true);
        }
        return false;
    }
}
