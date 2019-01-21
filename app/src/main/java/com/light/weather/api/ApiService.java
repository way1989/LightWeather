package com.light.weather.api;

import com.light.weather.bean.HeCity;
import com.light.weather.bean.HeWeather;
import com.light.weather.bean.HeWeather6;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

import static me.jessyan.retrofiturlmanager.RetrofitUrlManager.DOMAIN_NAME_HEADER;

/**
 * Created by android on 16-9-30.
 */

public interface ApiService {
    String BASE_URL_WEATHER = "https://free-api.heweather.net/s6/";
    String BASE_URL_SEARCH = "https://search.heweather.net/";

    String DOMAIN_NAME_WEATHER = "weather";
    String DOMAIN_NAME_SEARCH = "search";
    /**
     * @param key  用户认证key
     * @param city 城市名称 city可通过城市中英文名称、ID和IP地址进行，例如city=北京，city=beijing，city=CN101010100，city=
     *             60.194.130.1
     * @param lang 多语言，默认为中文，可选参数
     * @return 天气结果
     */
    @GET("weather?")
    Observable<HeWeather> getWeather(@Query("key") String key, @Query("city") String city,
                                     @Query("lang") String lang);

    /**
     * @param key  用户认证key
     * @param city 城市名称 city可通过城市中英文名称、ID和IP地址进行，例如city=北京，city=beijing，city=CN101010100，city=
     *             60.194.130.1
     * @return 搜索到的城市结果
     */
    @Headers({DOMAIN_NAME_HEADER + DOMAIN_NAME_WEATHER})
    @GET("find?")
    Observable<HeWeather6> searchCity(@Query("key") String key, @Query("location") String location);
}
