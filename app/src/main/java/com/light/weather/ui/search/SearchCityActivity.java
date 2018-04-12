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
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;
import com.light.weather.BuildConfig;
import com.light.weather.R;
import com.light.weather.adapter.SearchAdapter;
import com.light.weather.bean.City;
import com.light.weather.bean.SearchItem;
import com.light.weather.ui.base.BaseActivity;
import com.light.weather.ui.common.WeatherViewModel;
import com.light.weather.util.AppConstant;
import com.light.weather.util.RxSchedulers;
import com.light.weather.widget.SimpleListDividerDecorator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import me.bakumon.statuslayoutmanager.library.StatusLayoutManager;

public class SearchCityActivity extends BaseActivity implements MenuItem.OnActionExpandListener, View.OnTouchListener {
    private static final String TAG = "SearchCityActivity";
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private SearchAdapter mSearchAdapter;
    private SearchView mSearchView;
    private List<SearchItem> mDefaultItems;
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
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(
                getDrawable(R.drawable.list_divider_h), getDrawable(R.drawable.list_divider_v), false));

        loadDefaultCities();
    }

    private void loadDefaultCities() {
        mStatusLayoutManager.showLoadingLayout();
        mDisposable.add(mViewModel.getDefaultCities()
                .compose(RxSchedulers.<List<SearchItem>>io_main())
                .subscribe(new Consumer<List<SearchItem>>() {
                    @Override
                    public void accept(List<SearchItem> searchItems) throws Exception {
                        mDefaultItems = searchItems;
                        mSearchAdapter.setNewData(mDefaultItems);
                        mStatusLayoutManager.showSuccessLayout();
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
        mStatusLayoutManager.showSuccessLayout();
        if (cities == null || cities.isEmpty()) {
            mSearchAdapter.setNewData(mDefaultItems);
        } else {
            mSearchAdapter.setNewData(getSearchItems(cities));
        }
    }

    public void onSearchError(Throwable e) {
        Log.d(TAG, "onSearchError... e = " + e.getMessage());
        mStatusLayoutManager.showSuccessLayout();
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
            mStatusLayoutManager.showLoadingLayout();
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
                            mStatusLayoutManager.showSuccessLayout();
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
