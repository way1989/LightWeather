package top.broncho.weather.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class Bean(
    @SerializedName("HeWeather6")
    val heWeather6: List<HeWeather6>
) {
    @Keep
    data class HeWeather6(
        val basic: Basic,
        @SerializedName("daily_forecast")
        val dailyForecast: List<DailyForecast>,
        val hourly: List<Hourly>,
        val lifestyle: List<Lifestyle>,
        val now: Now,
        val status: String, // ok
        val update: Update
    ) {
        @Keep
        data class Basic(
            @SerializedName("admin_area")
            val adminArea: String, // 广东
            val cid: String, // CN101280601
            val cnty: String, // 中国
            val lat: String, // 22.54700089
            val location: String, // 深圳
            val lon: String, // 114.08594513
            @SerializedName("parent_city")
            val parentCity: String, // 深圳
            val tz: String // +8.00
        )

        @Keep
        data class DailyForecast(
            @SerializedName("cond_code_d")
            val condCodeD: String, // 101
            @SerializedName("cond_code_n")
            val condCodeN: String, // 101
            @SerializedName("cond_txt_d")
            val condTxtD: String, // 多云
            @SerializedName("cond_txt_n")
            val condTxtN: String, // 多云
            val date: String, // 2020-06-27
            val hum: String, // 80
            val mr: String, // 11:18
            val ms: String, // 00:00
            val pcpn: String, // 1.0
            val pop: String, // 55
            val pres: String, // 999
            val sr: String, // 05:41
            val ss: String, // 19:12
            @SerializedName("tmp_max")
            val tmpMax: String, // 33
            @SerializedName("tmp_min")
            val tmpMin: String, // 28
            @SerializedName("uv_index")
            val uvIndex: String, // 9
            val vis: String, // 24
            @SerializedName("wind_deg")
            val windDeg: String, // 195
            @SerializedName("wind_dir")
            val windDir: String, // 西南风
            @SerializedName("wind_sc")
            val windSc: String, // 3-4
            @SerializedName("wind_spd")
            val windSpd: String // 24
        )

        @Keep
        data class Hourly(
            val cloud: String, // 94
            @SerializedName("cond_code")
            val condCode: String, // 101
            @SerializedName("cond_txt")
            val condTxt: String, // 多云
            val dew: String, // 25
            val hum: String, // 77
            val pop: String, // 7
            val pres: String, // 1000
            val time: String, // 2020-06-28 01:00
            val tmp: String, // 28
            @SerializedName("wind_deg")
            val windDeg: String, // 214
            @SerializedName("wind_dir")
            val windDir: String, // 西南风
            @SerializedName("wind_sc")
            val windSc: String, // 3-4
            @SerializedName("wind_spd")
            val windSpd: String // 17
        )

        @Keep
        data class Lifestyle(
            val brf: String, // 较不舒适
            val txt: String, // 白天天气多云，并且空气湿度偏大，在这种天气条件下，您会感到有些闷热，不很舒适。
            val type: String // comf
        )

        @Keep
        data class Now(
            val cloud: String, // 96
            @SerializedName("cond_code")
            val condCode: String, // 101
            @SerializedName("cond_txt")
            val condTxt: String, // 多云
            val fl: String, // 32
            val hum: String, // 83
            val pcpn: String, // 0.0
            val pres: String, // 1001
            val tmp: String, // 28
            val vis: String, // 16
            @SerializedName("wind_deg")
            val windDeg: String, // 211
            @SerializedName("wind_dir")
            val windDir: String, // 西南风
            @SerializedName("wind_sc")
            val windSc: String, // 1
            @SerializedName("wind_spd")
            val windSpd: String // 4
        )

        @Keep
        data class Update(
            val loc: String, // 2020-06-28 00:30
            val utc: String // 2020-06-27 16:30
        )
    }
}