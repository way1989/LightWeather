package com.light.weather.ui.base;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.lang.reflect.ParameterizedType;

import javax.inject.Inject;

public abstract class BaseDagger2NonFragmentInjectorActivity<VM extends ViewModel> extends BaseActivity {

    protected VM mViewModel;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Class<VM> entityClass = (Class<VM>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(entityClass);
    }
}
