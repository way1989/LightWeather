package com.light.weather.dagger2;

import com.light.weather.activity.BaseActivity;
import com.light.weather.fragment.BaseFragment;
import com.light.weather.util.ActivityScope;

import dagger.Component;

/**
 * Created by android on 16-11-25.
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules = WeatherModule.class)
public interface WeatherComponent {
    void inject(BaseActivity activity);

    void inject(BaseFragment fragment);
}
