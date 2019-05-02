package com.light.weather.ui.base;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.light.weather.viewmodel.WeatherViewModel;

import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by android on 18-1-30.
 */

public abstract class BaseFragment extends Fragment {
    private static final String TAG = "BaseFragment";
    protected View mRootView;
    protected boolean mIsDataInitiated;
    protected boolean mIsViewInitiated;
    protected WeatherViewModel mViewModel;
    protected CompositeDisposable mDisposable;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.i(TAG, "setUserVisibleHint: ...... isVisibleToUser = " + isVisibleToUser);
        if (isVisibleToUser) {
            prepareFetchData();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated: ........");
        mDisposable = new CompositeDisposable();
        mIsViewInitiated = true;
        prepareFetchData();
    }

    public abstract void loadDataFirstTime();

    public abstract boolean isDataDirty();

    private boolean prepareFetchData() {
        if (getUserVisibleHint() && mIsViewInitiated && (!mIsDataInitiated || isDataDirty())) {
            mIsDataInitiated = true;
            loadDataFirstTime();
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ............");
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
        Log.i(TAG, "onDestroyView: ................");
        mIsDataInitiated = false;
        mIsViewInitiated = false;
        mDisposable.clear();
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
