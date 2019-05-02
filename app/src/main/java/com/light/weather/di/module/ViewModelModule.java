package com.light.weather.di.module;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.light.weather.di.ViewModelKey;
import com.light.weather.di.WeatherViewModelFactory;
import com.light.weather.viewmodel.WeatherViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(WeatherViewModel.class)
    public abstract ViewModel bindWeatherViewModel(WeatherViewModel weatherViewModel);

    @Binds
    public abstract ViewModelProvider.Factory bindViewModelFactory(WeatherViewModelFactory factory);
}
