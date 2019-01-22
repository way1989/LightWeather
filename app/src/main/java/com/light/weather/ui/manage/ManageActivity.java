package com.light.weather.ui.manage;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback;
import com.chad.library.adapter.base.listener.OnItemDragListener;
import com.light.weather.R;
import com.light.weather.adapter.ManageAdapter;
import com.light.weather.bean.City;
import com.light.weather.ui.base.BaseDagger2NonFragmentInjectorActivity;
import com.light.weather.ui.search.SearchCityActivity;
import com.light.weather.util.AppConstant;
import com.light.weather.util.RxSchedulers;
import com.light.weather.viewmodel.WeatherViewModel;
import com.light.weather.widget.SimpleListDividerDecorator;

import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ManageActivity extends BaseDagger2NonFragmentInjectorActivity<WeatherViewModel> {
    public static final String EXTRA_DATA_CHANGED = "extra_data_changed";
    public static final String EXTRA_SELECTED_ITEM = "extra_selected_item";
    private static final String TAG = "ManageActivity";
    private static final int REQUEST_CODE_CITY = 0;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private ManageAdapter mAdapter;
    private boolean mDataChanged;
    private int mSelectedItem = -1;
    private long mLastClickMenuTime;

    protected View mLoadingView;
    protected View mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        LayoutInflater inflater = getLayoutInflater();
        mLoadingView = inflater.inflate(R.layout._loading_layout_loading, null);
        mEmptyView = inflater.inflate(R.layout._loading_layout_empty, null);

        mAdapter = new ManageAdapter(R.layout.item_manage_city, null);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat
                .getDrawable(this, R.drawable.list_divider_h), false));
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

        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
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
        });

        getCities();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manager, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                if (System.currentTimeMillis() - mLastClickMenuTime > 2000L) {
                    startActivityForResult(new Intent(ManageActivity.this,
                            SearchCityActivity.class), REQUEST_CODE_CITY);
                    mLastClickMenuTime = System.currentTimeMillis();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
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
                .compose(RxSchedulers.io_main())
                .subscribe(result -> Log.d(TAG, "accept: onItemSwap result = " + result),
                        throwable -> Log.e(TAG, "accept: onItemSwap...", throwable)));
    }

    public void onItemRemoved(final City city) {
        mDisposable.add(mViewModel.deleteCity(city)
                .compose(RxSchedulers.io_main())
                .subscribe(result -> {
                    Snackbar.make(mRecyclerView, city.getCity() + " 删除成功!",
                            Snackbar.LENGTH_LONG).show();
                    Log.d(TAG, "accept: onItemRemoved city = " + city.getCity()
                            + " result = " + result);
                }, throwable -> Log.e(TAG, "accept: onItemRemoved...", throwable)));
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
        mDisposable.add(mViewModel.getWeather(city.getAreaId(), city.isLocation())
                .map(heWeather -> {
                    Log.d(TAG, "getWeather: map weather isOK = " + heWeather);
                    if (heWeather != null) {
                        String code = heWeather.getNow().getCond_code();
                        String codeTxt = heWeather.getNow().getCond_txt();
                        String tmp = heWeather.getNow().getTmp();
                        Log.d(TAG, "getWeather: codeTxt = " + codeTxt + ", code = " + code + ", tmp = " + tmp);
                        city.setCode(code);
                        city.setCodeTxt(codeTxt);
                        city.setTmp(tmp);
                        //mViewModel.updateSync(city);
                    }
                    return city;
                })
                .subscribeOn(RxSchedulers.SCHEDULER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(city1 -> {
                    Log.d(TAG, "getWeather: onNext weather city1 = " + city1);
                    mAdapter.remove(mAdapter.getItemCount() - 1);
                    mAdapter.addData(city1);
                }, throwable -> Log.e(TAG, "getWeather onError: ", throwable)));

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_manage_city;
    }

    public void onCityChange(List<City> cities) {
        mAdapter.setNewData(cities);
        if (cities == null || cities.isEmpty()) {
            mAdapter.setNewData(null);
            mAdapter.setEmptyView(mEmptyView);
        }
    }

    private void getCities() {
        mAdapter.setEmptyView(mLoadingView);
        mDisposable.add(mViewModel.getCities(true)
                .compose(RxSchedulers.io_main())
                .subscribe(cities -> onCityChange(cities),
                        throwable -> Log.e(TAG, "accept: getCities ", throwable)));
    }

}
