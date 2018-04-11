package com.light.weather.ui.manage;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback;
import com.chad.library.adapter.base.listener.OnItemDragListener;
import com.jakewharton.rxbinding2.view.RxView;
import com.light.weather.R;
import com.light.weather.adapter.ManageAdapter;
import com.light.weather.bean.City;
import com.light.weather.bean.HeWeather;
import com.light.weather.ui.base.BaseActivity;
import com.light.weather.ui.common.WeatherViewModel;
import com.light.weather.ui.search.SearchCityActivity;
import com.light.weather.util.AppConstant;
import com.light.weather.util.RxSchedulers;
import com.light.weather.widget.SimpleListDividerDecorator;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.bakumon.statuslayoutmanager.library.StatusLayoutManager;

public class ManageActivity extends BaseActivity {
    public static final String EXTRA_DATA_CHANGED = "extra_data_changed";
    public static final String EXTRA_SELECTED_ITEM = "extra_selected_item";
    private static final String TAG = "ManageActivity";
    private static final int REQUEST_CODE_CITY = 0;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private ManageAdapter mAdapter;
    private boolean mDataChanged;
    private int mSelectedItem;
    private StatusLayoutManager mStatusLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(WeatherViewModel.class);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mDisposable.add(RxView.clicks(mFab)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        startActivityForResult(new Intent(ManageActivity.this,
                                SearchCityActivity.class), REQUEST_CODE_CITY);
                    }
                }));
        mAdapter = new ManageAdapter(R.layout.item_manage_city, null);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat
                .getDrawable(getApplicationContext(), R.drawable.list_divider_h), false));
        mRecyclerView.setAdapter(mAdapter);

        ItemDragAndSwipeCallback itemDragAndSwipeCallback = new ItemDragAndSwipeCallback(mAdapter) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
                if (source.getAdapterPosition() == 0 || target.getAdapterPosition() == 0)
                    return false;
                else
                    return super.onMove(recyclerView, source, target);
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (viewHolder.getAdapterPosition() == 0)
                    return makeMovementFlags(0, 0);
                else
                    return super.getMovementFlags(recyclerView, viewHolder);
            }


        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemDragAndSwipeCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        itemDragAndSwipeCallback.setSwipeMoveFlags(ItemTouchHelper.START | ItemTouchHelper.END);

        // 开启拖拽
        mAdapter.enableDragItem(itemTouchHelper, R.id.drag_handle, false);
        mAdapter.setOnItemDragListener(new OnItemDragListener() {
            private int dragStartPosition = -1;

            @Override
            public void onItemDragStart(RecyclerView.ViewHolder viewHolder, int pos) {
                dragStartPosition = pos;
            }

            @Override
            public void onItemDragMoving(RecyclerView.ViewHolder source, int from, RecyclerView.ViewHolder target, int to) {

            }

            @Override
            public void onItemDragEnd(RecyclerView.ViewHolder viewHolder, int pos) {
                if (dragStartPosition != pos) {
                    mDataChanged = true;
                    onItemSwap();
                }
            }
        });

        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                City city = mAdapter.getItem(position);
                Log.d(TAG, "onItemClick: city = " + city);
                switch (view.getId()) {
                    case R.id.content:
                        mSelectedItem = position;
                        onBackPressed();
                        break;
                    case R.id.right:
                        mDataChanged = true;
                        mAdapter.remove(position);
                        onItemRemoved(city);
                        break;
                }
            }
        });

        mStatusLayoutManager = new StatusLayoutManager.Builder(mRecyclerView)
                .setDefaultEmptyClickViewVisible(false)
                .setDefaultErrorClickViewVisible(false)
                .build();
        getCities();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putExtra(EXTRA_SELECTED_ITEM, mSelectedItem);
        i.putExtra(EXTRA_DATA_CHANGED, mDataChanged);
        setResult(RESULT_OK, i);
        mSelectedItem = -1;
        mDataChanged = false;
        super.onBackPressed();
    }

    private void onItemSwap() {
        final List<City> data = mAdapter.getData();
        mDisposable.add(mViewModel.swapCity(data)
                .compose(RxSchedulers.<Boolean>io_main())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean result) throws Exception {
                        Log.d(TAG, "accept: onItemSwap result = " + result);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "accept: onItemSwap...", throwable);
                    }
                }));
    }

    public void onItemRemoved(final City city) {
        mDisposable.add(mViewModel.deleteCity(city)
                .compose(RxSchedulers.<Boolean>io_main())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean result) throws Exception {
                        Snackbar.make(mRecyclerView, city.getCity() + " 删除成功!",
                                Snackbar.LENGTH_LONG).show();
                        Log.d(TAG, "accept: onItemRemoved city = " + city.getCity()
                                + " result = " + result);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "accept: onItemRemoved...", throwable);
                    }
                }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CITY && resultCode == RESULT_OK) {
            City city = (City) data.getSerializableExtra(AppConstant.ARG_CITY_KEY);
            if (city != null) {
                mDataChanged = true;
                mAdapter.addData(city);
                getWeather(city);
            }
        }
    }

    private void getWeather(final City city) {
        Log.i(TAG, "getWeather... city = " + city);
        mDisposable.add(mViewModel.getWeather(city, true)
                .map(new Function<HeWeather, City>() {
                    @Override
                    public City apply(HeWeather heWeather) throws Exception {
                        Log.d(TAG, "getWeather: map weather isOK = " + heWeather.isOK());
                        if (heWeather.isOK()) {
                            String code = heWeather.getWeather().getNow().getCond().getCode();
                            String codeTxt = heWeather.getWeather().getNow().getCond().getTxt();
                            String tmp = heWeather.getWeather().getNow().getTmp();
                            Log.d(TAG, "getWeather: codeTxt = " + codeTxt + ", code = " + code + ", tmp = " + tmp);
                            city.setCode(code);
                            city.setCodeTxt(codeTxt);
                            city.setTmp(tmp);
                            //mViewModel.updateSync(city);
                        }
                        return city;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<City>() {
                    @Override
                    public void accept(City city1) throws Exception {
                        Log.d(TAG, "getWeather: onNext weather city1 = " + city1);
                        mAdapter.remove(mAdapter.getItemCount() - 1);
                        mAdapter.addData(city1);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "getWeather onError: ", throwable);
                    }
                }));

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_manage_city;
    }

    public void onCityChange(List<City> cities) {
        if (cities != null && cities.size() > 0) {
            mStatusLayoutManager.showSuccessLayout();
        } else {
            mStatusLayoutManager.showEmptyLayout();
        }
        mAdapter.setNewData(cities);
    }

    private void getCities() {
        mStatusLayoutManager.showLoadingLayout();
        mDisposable.add(mViewModel.getCities()
                .compose(RxSchedulers.<List<City>>io_main())
                .subscribe(new Consumer<List<City>>() {
                    @Override
                    public void accept(List<City> cities) throws Exception {
                        onCityChange(cities);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "accept: getCities ", throwable);
                    }
                }));
    }

}
