package com.light.weather.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.light.weather.BuildConfig;
import com.light.weather.R;
import com.light.weather.api.ApiService;
import com.light.weather.bean.BaseResponse;
import com.light.weather.bean.City;
import com.light.weather.bean.HeBasic;
import com.light.weather.bean.HeWeather6;
import com.light.weather.bean.HotCity;
import com.light.weather.bean.SearchItem;
import com.light.weather.data.IRepositoryManager;
import com.light.weather.db.DBUtil;
import com.light.weather.ui.main.MainActivity;
import com.light.weather.util.RetryWithDelay;
import com.light.weather.util.RxLocation;
import com.light.weather.util.RxSchedulers;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import me.jessyan.retrofiturlmanager.RetrofitUrlManager;

/**
 * Created by android on 18-1-29.
 */
public class WeatherViewModel extends AndroidViewModel {
    private static final String TAG = "WeatherViewModel";
    private static final long TIMEOUT_DURATION = 10L;//10s timeout
    private IRepositoryManager mRepositoryManager;//用于管理网络请求层,以及数据缓存层

    @Inject
    public WeatherViewModel(@NonNull Application application, IRepositoryManager manager) {
        super(application);
        mRepositoryManager = manager;
    }

    public Observable<List<SearchItem>> getDefaultCities() {
        return Observable.create(e -> {
            List<SearchItem> searchItems = new ArrayList<>();

            final City cityByLocation = DBUtil.getCityByLocation(getApplication());
            SearchItem locationTitle = new SearchItem(true, getApplication().getString(R.string.location_city_title));
            searchItems.add(locationTitle);
            searchItems.add(new SearchItem(cityByLocation));

            String json = getApplication().getString(R.string.hot_city_json);
            List<City> hotCities = new GsonBuilder()
                    .excludeFieldsWithModifiers(Modifier.PROTECTED)//忽略protected字段
                    .create().fromJson(json, HotCity.class).getCities();
            SearchItem hotTitle = new SearchItem(true, getApplication().getString(R.string.hot_city_title));
            searchItems.add(hotTitle);
            for (City city : hotCities) {
                searchItems.add(new SearchItem(city));
            }
            e.onNext(searchItems);
            e.onComplete();
        });
    }

    public Observable<List<City>> search(String query) {
        RetrofitUrlManager.getInstance().fetchDomain(ApiService.DOMAIN_NAME_SEARCH);
        return mRepositoryManager.obtainRetrofitService(ApiService.class)
                .searchCity(BuildConfig.HEWEATHER_KEY, query)
                .map(heSearch -> {
                    Log.d(TAG, "searchCity... result heSearch = " + heSearch);
                    ArrayList<City> cities = new ArrayList<>();
                    if (heSearch == null || !heSearch.isOK())
                        return cities;
                    List<HeBasic> beanList = heSearch.getWeather().getBasic();
                    for (HeBasic bean : beanList) {
                        City city = new City(bean.getLocation(), bean.getCnty(),
                                bean.getCid(), bean.getLat(), bean.getLon(),
                                bean.getAdmin_area());
                        cities.add(city);
                    }
                    Log.d(TAG, "searchCity... end size = " + cities.size());
                    return cities;
                });
    }

    public Observable<City> addCity(final City city) {
        Log.d(TAG, "addCity... city = " + city.getCity());
        return Observable.create(e -> {
            final boolean exist = DBUtil.isExist(getApplication(), city);
            Log.d(TAG, "addCity... exist = " + exist);
            boolean result = false;
            if (!exist) {
                city.setLocation(false);
                city.setIndex(DBUtil.getCacheCitySize(getApplication()));
                result = DBUtil.addCity(getApplication(), city, false);
            }
            if (!exist && result) {
                e.onNext(city);
                e.onComplete();
            } else {
                e.onError(new Throwable("city is exist"));
            }
        });
    }

    public Observable<List<City>> getCities(final boolean isManager) {
        return Observable.create(e -> {
            List<City> cities = DBUtil.getCityFromCache(getApplication());
            if (!isManager && cities.isEmpty()) {
                City city = new City();
                city.setLocation(true);
                city.setCity(MainActivity.UNKNOWN_CITY);
                cities.add(city);
                boolean result = DBUtil.insertAutoLocation(getApplication());
            }
            e.onNext(cities);
            e.onComplete();
        });
    }

    public Observable<Boolean> swapCity(final List<City> cities) {
        return Observable.create(e -> {
            for (int i = 0; i < cities.size(); i++) {
                City city = cities.get(i);
                city.setIndex(i);
                DBUtil.updateCity(getApplication(), city, city.isLocation());
            }
            e.onNext(true);
            e.onComplete();
        });
    }

    public Observable<Boolean> deleteCity(final City city) {
        return Observable.create(e -> {
            boolean result = DBUtil.deleteCity(getApplication(), city);
            e.onNext(result);
            e.onComplete();
        });
    }

    public Observable<City> getLocation() {
        return RxLocation.getInstance(getApplication());
    }

    public Observable<HeWeather6<HeBasic>> getWeather(String areaId, final boolean isLocation) {
        RetrofitUrlManager.getInstance().fetchDomain(ApiService.DOMAIN_NAME_WEATHER);
        final ApiService apiService = mRepositoryManager.obtainRetrofitService(ApiService.class);
        final RetryWithDelay retryWithDelay = new RetryWithDelay(3, 3000L);
        return apiService.getWeather(areaId, BuildConfig.HEWEATHER_KEY)
                .timeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
                .retryWhen(retryWithDelay)
                .map((Function<BaseResponse<HeBasic>, HeWeather6<HeBasic>>) response -> {
                    HeWeather6 weather6 = response.getWeather();
                    if (weather6 == null) {
                        throw new Exception("get weather failed!");
                    }
                    return weather6;
                })
                .observeOn(RxSchedulers.SCHEDULER)
                .doOnNext(heWeather -> {
                    Log.d(TAG, "getWeather: doOnNext weather = " + heWeather);
                    if (heWeather != null) {
                        HeBasic basic = heWeather.getBasic();
                        City copy = new City(basic.getLocation(), basic.getCnty(),
                                basic.getCid(), basic.getLat(), basic.getLon(), basic.getAdmin_area());
                        String code = heWeather.getNow().getCond_code();
                        String codeTxt = heWeather.getNow().getCond_txt();
                        String tmp = heWeather.getNow().getTmp();
                        copy.setCode(code);
                        copy.setCodeTxt(codeTxt);
                        copy.setTmp(tmp);
                        boolean result = DBUtil.updateCity(getApplication(), copy, isLocation);
                        Log.d(TAG, "getWeather: doOnNext update DB result = " + result
                                + ", city = " + copy);
                    }
                });
    }

    public Observable<HeWeather6.AqiBean> getAqi(final String areaId) {
        RetrofitUrlManager.getInstance().fetchDomain(ApiService.DOMAIN_NAME_WEATHER);
        final ApiService apiService = mRepositoryManager.obtainRetrofitService(ApiService.class);
        final RetryWithDelay retryWithDelay = new RetryWithDelay(3, 3000L);
        return apiService.getAqi(areaId, BuildConfig.HEWEATHER_KEY)
                .timeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
                .retryWhen(retryWithDelay)
                .map(response -> {
                    HeWeather6 weather6 = response.getWeather();
                    if (weather6 == null) {
                        throw new Exception("get aqi failed!");
                    }
                    return weather6.getAqi();
                });
    }
}
