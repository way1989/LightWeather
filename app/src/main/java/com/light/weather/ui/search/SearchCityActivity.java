package com.light.weather.ui.search;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;
import com.light.weather.BuildConfig;
import com.light.weather.R;
import com.light.weather.adapter.SearchAdapter;
import com.light.weather.bean.City;
import com.light.weather.bean.SearchItem;
import com.light.weather.ui.base.BaseDagger2NonFragmentInjectorActivity;
import com.light.weather.viewmodel.WeatherViewModel;
import com.light.weather.util.AppConstant;
import com.light.weather.util.RxSchedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class SearchCityActivity extends BaseDagger2NonFragmentInjectorActivity<WeatherViewModel> implements MenuItem.OnActionExpandListener, View.OnTouchListener {
    private static final String TAG = "SearchCityActivity";
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    private SearchAdapter mSearchAdapter;
    private SearchView mSearchView;
    private List<SearchItem> mDefaultItems;

    protected View mLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        LayoutInflater inflater = getLayoutInflater();
        mLoadingView = inflater.inflate(R.layout._loading_layout_loading, null);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setOnTouchListener(this);
        mSearchAdapter = new SearchAdapter(R.layout.item_search_city, R.layout.item_search_head, null);
        //mSearchAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        mSearchAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                final City city = (City) view.getTag();
                SearchCityActivity.this.onItemClick(city);
            }
        });
        mRecyclerView.setAdapter(mSearchAdapter);
        //mRecyclerView.addItemDecoration(new GridDividerItemDecoration(1, ContextCompat.getColor(this, R.color.divider_color)));

        loadDefaultCities();
    }

    private void loadDefaultCities() {
        mSearchAdapter.setNewData(null);
        mSearchAdapter.setEmptyView(mLoadingView);
        mDisposable.add(mViewModel.getDefaultCities()
                .compose(RxSchedulers.<List<SearchItem>>io_main())
                .subscribe(new Consumer<List<SearchItem>>() {
                    @Override
                    public void accept(List<SearchItem> searchItems) throws Exception {
                        mDefaultItems = searchItems;
                        mSearchAdapter.setNewData(mDefaultItems);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "loadDefaultCities: ", throwable);
                        Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private List<SearchItem> getSearchItems(List<City> cities) {
        List<SearchItem> searchItems = new ArrayList<>();
        SearchItem searchItem = new SearchItem(true, getString(R.string.search_city_title));
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
            mSearchAdapter.setNewData(null);
            mSearchAdapter.setEmptyView(mLoadingView);
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
        } else {
            onSearchResult(null);
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
            mSearchAdapter.setNewData(mDefaultItems);
        } else {
            mSearchAdapter.setNewData(getSearchItems(cities));
        }
    }

    public void onSearchError(Throwable e) {
        Log.d(TAG, "onSearchError... e = " + e.getMessage());
        mSearchAdapter.setNewData(mDefaultItems);
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
            return;
        }
        if (city.getIsLocation() == 1) {
            mSearchAdapter.setNewData(null);
            mSearchAdapter.setEmptyView(mLoadingView);
            mDisposable.add(mViewModel.getLocation()
                    .concatMap(new Function<City, ObservableSource<List<SearchItem>>>() {
                        @Override
                        public ObservableSource<List<SearchItem>> apply(City city) throws Exception {
                            return mViewModel.getDefaultCities();
                        }
                    })
                    .compose(RxSchedulers.<List<SearchItem>>io_main())
                    .subscribe(new Consumer<List<SearchItem>>() {
                        @Override
                        public void accept(List<SearchItem> searchItems) throws Exception {
                            mDefaultItems = searchItems;
                            mSearchAdapter.setNewData(mDefaultItems);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }));
        } else {
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
                            Toast.makeText(getApplicationContext(), R.string.city_exist, Toast.LENGTH_SHORT).show();
                        }
                    }));
        }
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideInputManager();
        return false;
    }
}
