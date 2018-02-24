package com.light.weather.fragment;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.light.weather.App;
import com.light.weather.dagger2.AppComponent;
import com.light.weather.dagger2.DaggerWeatherComponent;
import com.light.weather.dagger2.WeatherModule;
import com.light.weather.dagger2.WeatherViewModel;
import com.trello.rxlifecycle2.components.support.RxFragment;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * Created by android on 18-1-30.
 */

public abstract class BaseFragment extends RxFragment {
    protected View mRootView;
    protected boolean mIsViewInitiated;
    protected boolean mIsDataInitiated;
    @Inject
    protected WeatherViewModel mViewModel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mIsViewInitiated = true;
        prepareFetchData();
    }

    @Override

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)
            prepareFetchData();
    }

    public abstract void loadDataFirstTime();

    private boolean prepareFetchData() {
        if (getUserVisibleHint() && mIsViewInitiated && !mIsDataInitiated) {
            loadDataFirstTime();
            mIsDataInitiated = true;
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(getLayoutId(), container, false);
        ButterKnife.bind(this, mRootView);

        initPresenter(((App) getActivity().getApplication()).getAppComponent());
        initView();
        return mRootView;
    }

    protected abstract void initView();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    //public abstract String getTitle();


    /**
     * should override this method when use MVP
     */
    protected void initPresenter(AppComponent appComponent) {
        DaggerWeatherComponent.builder().appComponent(appComponent)
                .weatherModule(new WeatherModule((AppCompatActivity) getActivity())).build().inject(this);
    }

    /**
     * must override this method
     *
     * @return resource layout id
     */
    protected abstract
    @LayoutRes
    int getLayoutId();

    public abstract void onShareItemClick();
}
