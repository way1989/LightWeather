package top.broncho.weather.model


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BaseResponse<T>(
    @SerializedName("HeWeather6")
    val heWeather6: List<HeWeather6<T>>
) {
    @Keep
    data class HeWeather6<T>(
        val basic: List<T>,
        val status: String // ok
    )
}

@Keep
data class CityBean(
    @SerializedName("admin_area")
    val adminArea: String, // 广东
    val cid: String, // CN101280601
    val cnty: String, // 中国
    val lat: String, // 22.54700089
    val location: String, // 深圳
    val lon: String, // 114.08594513
    @SerializedName("parent_city")
    val parentCity: String, // 深圳
    val type: String, // city
    val tz: String // +8.00
)