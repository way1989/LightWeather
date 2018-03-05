package com.light.weather.ui.base;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.light.weather.ui.common.WeatherViewModel;
import com.trello.rxlifecycle2.components.support.RxFragment;

import butterknife.ButterKnife;

/**
 * Created by android on 18-1-30.
 */

public abstract class BaseFragment extends RxFragment {

    protected View mRootView;
    protected boolean mIsViewInitiated;
    protected boolean mIsDataInitiated;
    //@Inject
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

        initView();
        return mRootView;
    }

    protected abstract void initView();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    /**
     * must override this method
     *
     * @return resource layout id
     */
    protected abstract
    @LayoutRes
    int getLayoutId();

}
