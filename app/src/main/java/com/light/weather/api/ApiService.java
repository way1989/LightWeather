package com.light.weather.api;

import com.light.weather.bean.BaseResponse;
import com.light.weather.bean.HeBasic;

import java.util.List;

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
     * @param key      用户认证key
     * @param location 输入需要查询的城市名称，支持模糊搜索，可输入中文（至少一个汉字）、英文（至少2个字母）、
     *                 IP地址、坐标（经度在前纬度在后，英文,分割）、ADCode。
     *                 location=北
     *                 location=北京
     *                 location=beij
     *                 location=60.194.130.1
     *                 location=116.4,39.1
     * @return 搜索到的城市结果
     */
    @Headers({DOMAIN_NAME_HEADER + DOMAIN_NAME_SEARCH})
    @GET("find?")
    Observable<BaseResponse<List<HeBasic>>> searchCity(@Query("key") String key, @Query("location") String location);

    /**
     * @param location 需要查询的城市或地区，可输入以下值：
     *                 1. 城市ID：城市列表
     *                 2. 经纬度格式：经度,纬度（经度在前纬度在后，英文,分隔，十进制格式，北纬东经为正，南纬西经为负
     *                 3. 城市名称，支持中英文和汉语拼音
     *                 4. 城市名称，上级城市 或 省 或 国家，英文,分隔，此方式可以在重名的情况下只获取想要的地区的天气数据，例如 西安,陕西
     *                 5. IP
     *                 6. 根据请求自动判断，根据用户的请求获取IP，通过 IP 定位并获取城市数据
     *                 1. location=CN101010100
     *                 2. location=116.40,39.9
     *                 3. location=北京、 location=北京市、 location=beijing
     *                 4. location=朝阳,北京、 location=chaoyang,beijing
     *                 5. location=60.194.130.1
     *                 6. location=auto_ip
     * @param key      用户认证key
     * @return
     */
    @Headers({DOMAIN_NAME_HEADER + DOMAIN_NAME_WEATHER})
    @GET("weather?")
    Observable<BaseResponse<HeBasic>> getWeather(@Query("location") String location, @Query("key") String key);

    @Headers({DOMAIN_NAME_HEADER + DOMAIN_NAME_WEATHER})
    @GET("air/now?")
    Observable<BaseResponse<HeBasic>> getAqi(@Query("location") String location, @Query("key") String key);

    @Headers({DOMAIN_NAME_HEADER + DOMAIN_NAME_WEATHER})
    @GET("alarm?")
    Observable<BaseResponse<HeBasic>> getAlarm(@Query("location") String location, @Query("key") String key);
}
