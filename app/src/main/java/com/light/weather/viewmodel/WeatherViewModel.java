package com.light.weather.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.light.weather.BuildConfig;
import com.light.weather.R;
import com.light.weather.api.ApiService;
import com.light.weather.bean.City;
import com.light.weather.bean.HeCity;
import com.light.weather.bean.HeWeather;
import com.light.weather.bean.HeWeather6;
import com.light.weather.bean.HotCity;
import com.light.weather.bean.SearchItem;
import com.light.weather.data.IRepositoryManager;
import com.light.weather.db.CityDao;
import com.light.weather.db.CityDatabase;
import com.light.weather.ui.main.MainActivity;
import com.light.weather.util.RetryWithDelay;
import com.light.weather.util.RxLocation;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.retrofiturlmanager.RetrofitUrlManager;

/**
 * Created by android on 18-1-29.
 */
public class WeatherViewModel extends AndroidViewModel {
    private static final String TAG = "WeatherViewModel";
    private static final long TIMEOUT_DURATION = 10L;//10s timeout
    private static final String LANG = "zh-cn";
    private IRepositoryManager mRepositoryManager;//用于管理网络请求层,以及数据缓存层

    @Inject
    public WeatherViewModel(@NonNull Application application, IRepositoryManager manager) {
        super(application);
        mRepositoryManager = manager;
    }

    public Observable<List<SearchItem>> getDefaultCities() {
        return Observable.create(new ObservableOnSubscribe<List<SearchItem>>() {
            @Override
            public void subscribe(ObservableEmitter<List<SearchItem>> e) {
                List<SearchItem> searchItems = new ArrayList<>();

                CityDao cityDao = getDB(mRepositoryManager);
                final City cityByLocation = cityDao.getCityByLocation();
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
            }
        });
    }

    public Observable<List<City>> search(String query) {
        return search(mRepositoryManager, query)
                .map(new Function<HeWeather6, List<City>>() {
                    @Override
                    public List<City> apply(HeWeather6 heCity) throws Exception {
                        Log.d(TAG, "searchCity... result heCity = " + heCity);
//                        ArrayList<City> cities = new ArrayList<>();
//                        if (heCity == null || !heCity.isOK())
//                            return cities;
//                        List<HeCity.HeWeather5Bean> beanList = heCity.getHeWeather5();
//                        for (HeCity.HeWeather5Bean bean : beanList) {
//                            if (TextUtils.equals(bean.getStatus(), "ok")) {
//                                HeCity.HeWeather5Bean.BasicBean basicBean = bean.getBasic();
//                                City city = new City(basicBean.getLocation(), basicBean.getCnty(),
//                                        basicBean.getCid(), basicBean.getLat(), basicBean.getLon(),
//                                        basicBean.getProv());
//                                cities.add(city);
//                            }
//                        }
//                        Log.d(TAG, "searchCity... end size = " + cities.size());
//                        return cities;
                        return null;
                    }

                });
    }

    public Observable<City> addCity(final City city) {
        Log.d(TAG, "addCity... city = " + city.getCity());
        return Observable.create(new ObservableOnSubscribe<City>() {
            @Override
            public void subscribe(ObservableEmitter<City> e) throws Exception {
                CityDao cityDao = getDB(mRepositoryManager);
                final boolean exist = cityDao.getCityByAreaId(city.getAreaId()) != null;
                Log.d(TAG, "addCity... exist = " + exist);
                long id = -1;
                if (!exist) {
                    city.setIsLocation(0);
                    city.setIndex(cityDao.getCityCount());
                    id = cityDao.insert(city);
                }
                if (!exist && id > 0) {
                    e.onNext(city);
                    e.onComplete();
                } else {
                    e.onError(new Throwable("city is exist"));
                }
            }
        });
    }

    public Observable<List<City>> getCities(final boolean isManager) {
        return Observable.create(new ObservableOnSubscribe<List<City>>() {
            @Override
            public void subscribe(ObservableEmitter<List<City>> e) throws Exception {
                CityDao cityDao = getDB(mRepositoryManager);
                List<City> cities = cityDao.getCityAll();
                if (!isManager && cities.isEmpty()) {
                    City city = new City();
                    city.setIsLocation(1);
                    city.setCity(MainActivity.UNKNOWN_CITY);
                    cities.add(city);
                }
                e.onNext(cities);
                e.onComplete();
            }
        });
    }

    public Observable<Boolean> swapCity(final List<City> cities) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                CityDao cityDao = getDB(mRepositoryManager);
                for (int i = 0; i < cities.size(); i++) {
                    City city = cities.get(i);
                    city.setIndex(i);
                    cityDao.update(city);
                }
                e.onNext(true);
                e.onComplete();
            }
        });
    }

    public Observable<Boolean> deleteCity(final City city) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                CityDao cityDao = getDB(mRepositoryManager);
                boolean result = cityDao.delete(city) > 0;
                e.onNext(result);
                e.onComplete();
            }
        });
    }

    public Observable<City> getLocation() {
        return RxLocation.getInstance(getApplication())
//                .timeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<City>>() {
                    @Override
                    public ObservableSource<City> apply(String s) throws Exception {
                        Log.d(TAG, "getLocation flatMap: location = " + s);
                        return search(mRepositoryManager, s)
                                .map(new Function<HeWeather6, City>() {
                                    @Override
                                    public City apply(HeWeather6 heCity) throws Exception {
                                        Log.i(TAG, "getLocation map: heCity = " + heCity);
//                                        HeWeather6.HeWeather6Bean.BasicBean basicBean = heCity.getHeWeather6().get(0).getBasic();
//                                        City city = new City(basicBean.getLocation(), basicBean.getCnty(),
//                                                basicBean.getCid(), basicBean.getLat(), basicBean.getLon(), basicBean.getProv());
//                                        city.setIsLocation(1);
//                                        CityDao cityDao = getDB(mRepositoryManager);
//                                        final City exist = cityDao.getCityByLocation();
//                                        Log.d(TAG, "getLocation exist = " + exist);
//                                        if (exist == null) {
//                                            cityDao.insert(city);
//                                        } else if (!TextUtils.equals(exist.getAreaId(), city.getAreaId())) {
//                                            cityDao.delete(exist);
//                                            cityDao.insert(city);
//                                        }
                                        return null;
                                    }
                                });
                    }
                });
    }

    private CityDao getDB(IRepositoryManager repositoryManager) {
        return repositoryManager.obtainRoomDatabase(CityDatabase.class,
                CityDatabase.DATABASE_NAME).cityDao();
    }

    private Observable<HeWeather6> search(IRepositoryManager repositoryManager, String query) {
        RetrofitUrlManager.getInstance().fetchDomain(ApiService.DOMAIN_NAME_SEARCH);
        return repositoryManager.obtainRetrofitService(ApiService.class)
                .searchCity(BuildConfig.HEWEATHER_KEY, query);
    }

    public Observable<HeWeather> getWeather(final City city) {
        final ApiService apiService = mRepositoryManager.obtainRetrofitService(ApiService.class);
        final RetryWithDelay retryWithDelay = new RetryWithDelay(3, 3000L);
        return apiService.getWeather(BuildConfig.HEWEATHER_KEY, city.getAreaId(), Locale.getDefault().getCountry())
                .timeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<HeWeather>() {
                    @Override
                    public void accept(HeWeather heWeather) throws Exception {
                        Log.d(TAG, "getWeather: map weather isOK = " + heWeather.isOK());
                        if (heWeather.isOK()) {
                            String code = heWeather.getWeather().getNow().getCond().getCode();
                            String codeTxt = heWeather.getWeather().getNow().getCond().getTxt();
                            String tmp = heWeather.getWeather().getNow().getTmp();
                            Log.d(TAG, "getWeather: codeTxt = " + codeTxt + ", code = " + code + ", tmp = " + tmp);
                            city.setCode(code);
                            city.setCodeTxt(codeTxt);
                            city.setTmp(tmp);
                            getDB(mRepositoryManager).update(city);
                        }
                    }
                }).retryWhen(retryWithDelay);
    }
}
