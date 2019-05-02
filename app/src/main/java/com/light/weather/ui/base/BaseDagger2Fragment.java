package com.light.weather.ui.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.light.weather.di.Injectable;

import java.lang.reflect.ParameterizedType;

import javax.inject.Inject;


public abstract class BaseDagger2Fragment<VM extends ViewModel> extends BaseFragment implements Injectable {
    protected VM mViewModel;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Class<VM> entityClass = (Class<VM>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(entityClass);
        super.onActivityCreated(savedInstanceState);
    }
}
