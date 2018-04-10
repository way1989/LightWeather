package com.light.weather.ui.search;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.GsonBuilder;
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;
import com.light.weather.BuildConfig;
import com.light.weather.R;
import com.light.weather.adapter.SearchAdapter;
import com.light.weather.bean.City;
import com.light.weather.bean.HotCity;
import com.light.weather.bean.SearchItem;
import com.light.weather.ui.base.BaseActivity;
import com.light.weather.ui.common.WeatherViewModel;
import com.light.weather.util.AppConstant;
import com.light.weather.util.RxSchedulers;
import com.light.weather.widget.SimpleListDividerDecorator;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import me.bakumon.statuslayoutmanager.library.StatusLayoutManager;

public class SearchCityActivity extends BaseActivity implements MenuItem.OnActionExpandListener, View.OnTouchListener {
    private static final String TAG = "SearchCityActivity";
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private SearchAdapter mSearchAdapter;
    private SearchView mSearchView;
    private InputMethodManager mInputMethodManager;
    private List<City> mHotCities;
    private List<SearchItem> mSearchItems;
    private StatusLayoutManager mStatusLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(WeatherViewModel.class);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mStatusLayoutManager = new StatusLayoutManager.Builder(mRecyclerView)
                .setDefaultEmptyClickViewVisible(false)
                .setDefaultErrorClickViewVisible(false)
                .build();

        mSearchItems = new ArrayList<>();

        String json = getString(R.string.hot_city_json);
        HotCity hotCity = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.PROTECTED)//忽略protected字段
                .create().fromJson(json, HotCity.class);
        mHotCities = hotCity.getCities();
        Log.d(TAG, "onCreate: getHotCity = " + mHotCities);
        List<SearchItem> searchItems = getSearchItems(mHotCities, true);
        mSearchItems.addAll(searchItems);

        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setOnTouchListener(this);
        mSearchAdapter = new SearchAdapter(R.layout.item_search_city, R.layout.item_search_head, mSearchItems);
        //mSearchAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        mSearchAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                final City city = (City) view.getTag();
                SearchCityActivity.this.onItemClick(city);
            }
        });
        mRecyclerView.setAdapter(mSearchAdapter);
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(
                getDrawable(R.drawable.list_divider_h), getDrawable(R.drawable.list_divider_v), false));

    }

    private List<SearchItem> getSearchItems(List<City> cities, boolean isHotCity) {
        List<SearchItem> searchItems = new ArrayList<>();
        SearchItem searchItem = new SearchItem(true, isHotCity ? "热门城市:" : "搜索结果:");
        searchItems.add(searchItem);
        for (City city : cities)
            searchItems.add(new SearchItem(city));
        return searchItems;
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
        //mSearchView.setIconifiedByDefault(false);
        //mSearchView.setIconified(false);
        searchMenuItem.expandActionView();

        searchMenuItem.setOnActionExpandListener(this);

        mDisposable.add(RxSearchView.queryTextChanges(mSearchView)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<CharSequence>() {
                    @Override
                    public void accept(CharSequence charSequence) throws Exception {
                        search(charSequence.toString());
                    }
                }));
        return super.onCreateOptionsMenu(menu);
    }


    private void search(String query) {
        if (!TextUtils.isEmpty(query) || !query.trim().equals("")) {
            mStatusLayoutManager.showLoadingLayout();
            mDisposable.add(mViewModel.search(query)
                    .compose(RxSchedulers.<List<City>>io_main())
                    .subscribe(new Consumer<List<City>>() {
                        @Override
                        public void accept(List<City> cities) throws Exception {
                            onSearchResult(cities);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e(TAG, "accept: search city ", throwable);
                            onSearchError(throwable);
                        }
                    }));
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideInputManager();
        return false;
    }

    public void hideInputManager() {
        if (mSearchView != null) {
            if (mInputMethodManager != null) {
                mInputMethodManager.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            }
            mSearchView.clearFocus();
        }
    }

    public void onSearchResult(List<City> cities) {
        Log.d(TAG, "onSearchResult... city size = " + cities.size());
        mStatusLayoutManager.showSuccessLayout();
        if (cities.isEmpty()) {
            mSearchItems.clear();
            List<SearchItem> hotSearchItems = getSearchItems(mHotCities, true);
            mSearchItems.addAll(hotSearchItems);
            mSearchAdapter.setNewData(mSearchItems);
        } else {
            mSearchItems.clear();
            List<SearchItem> hotSearchItems = getSearchItems(mHotCities, true);
            List<SearchItem> searchItems = getSearchItems(cities, false);
            mSearchItems.addAll(searchItems);
            mSearchItems.addAll(hotSearchItems);
            mSearchAdapter.setNewData(mSearchItems);
        }
    }

    public void onSearchError(Throwable e) {
        Log.d(TAG, "onSearchError... e = " + e.getMessage());
        mStatusLayoutManager.showSuccessLayout();
        mSearchItems.clear();
        List<SearchItem> hotSearchItems = getSearchItems(mHotCities, true);
        mSearchItems.addAll(hotSearchItems);
        mSearchAdapter.setNewData(mSearchItems);
        if (BuildConfig.LOG_DEBUG)
            Snackbar.make(mRecyclerView, e.getMessage(), Snackbar.LENGTH_LONG).show();
    }

    public void onSaveCitySucceed(City city) {
        Log.d(TAG, "onSaveCitySucceed city = " + city);
        if (city == null) {
            Snackbar.make(mRecyclerView, R.string.city_exist, Snackbar.LENGTH_LONG).show();
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
            Toast.makeText(this, "city is null...", Toast.LENGTH_SHORT).show();
        }
        mDisposable.add(mViewModel.addCity(city)
                .compose(RxSchedulers.<City>io_main())
                .subscribe(new Consumer<City>() {
                    @Override
                    public void accept(City result) throws Exception {
                        onSaveCitySucceed(result);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "accept: addCity city = " + city, throwable);
                    }
                }));
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        finish();
        return true;
    }

}
