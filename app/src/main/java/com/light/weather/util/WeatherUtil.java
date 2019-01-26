package com.light.weather.util;


import android.content.res.Resources;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import com.light.weather.R;
import com.light.weather.bean.HeBasic;
import com.light.weather.bean.HeWeather6;
import com.light.weather.widget.dynamic.WeatherType;
import com.light.weather.widget.dynamic.DefaultType;
import com.light.weather.widget.dynamic.FogType;
import com.light.weather.widget.dynamic.HailType;
import com.light.weather.widget.dynamic.HazeType;
import com.light.weather.widget.dynamic.OvercastType;
import com.light.weather.widget.dynamic.RainType;
import com.light.weather.widget.dynamic.SandstormType;
import com.light.weather.widget.dynamic.ShortWeatherInfo;
import com.light.weather.widget.dynamic.SnowType;
import com.light.weather.widget.dynamic.SunnyType;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by liyu on 2016/11/11.
 */

public class WeatherUtil {
    private static volatile WeatherUtil sInstance;

    private WeatherUtil() {
    }

    public static WeatherUtil getInstance() {
        if (sInstance == null) {
            synchronized (WeatherUtil.class) {
                if (sInstance == null) {
                    sInstance = new WeatherUtil();
                }
            }
        }
        return sInstance;
    }

    public static final String SUGGESTION_AIR = "空气";
    public static final String SUGGESTION_COMF = "舒适度";
    public static final String SUGGESTION_CW = "洗车";
    public static final String SUGGESTION_DRSG = "穿衣";
    public static final String SUGGESTION_FLU = "感冒";
    public static final String SUGGESTION_SPORT = "运动";
    public static final String SUGGESTION_TRAV = "旅游";
    public static final String SUGGESTION_UV = "紫外线";

    public static String getTitle(String type) {
        switch (type) {
            case "air":
                return SUGGESTION_AIR;
            case "cw":
                return SUGGESTION_CW;
            case "uv":
                return SUGGESTION_UV;
            case "trav":
                return SUGGESTION_TRAV;
            case "sport":
                return SUGGESTION_SPORT;
            case "flu":
                return SUGGESTION_FLU;
            case "drsg":
                return SUGGESTION_DRSG;
            case "comf":
                return SUGGESTION_COMF;
        }
        return "未知";
    }

    public static @DrawableRes
    int getIcon(String type) {
        switch (type) {
            case "air":
                return R.drawable.ic_air;
            case "cw":
                return R.drawable.ic_cw;
            case "uv":
                return R.drawable.ic_uv;
            case "trav":
                return R.drawable.ic_trav;
            case "sport":
                return R.drawable.ic_sport;
            case "flu":
                return R.drawable.ic_flu;
            case "drsg":
                return R.drawable.ic_drsg;
            case "comf":
                return R.drawable.ic_comf;
        }
        return R.drawable.ic_drsg;
    }

    public static int getBackground(String type) {
        switch (type) {
            case "air":
                return 0xFF7F9EE9;
            case "cw":
                return 0xFF62B1FF;
            case "uv":
                return 0xFFF0AB2A;
            case "trav":
                return 0xFFFD6C35;
            case "sport":
                return 0xFFB3CA60;
            case "flu":
                return 0xFFF98178;
            case "drsg":
                return 0xFF8FC55F;
            case "comf":
                return 0xFFE99E3C;
        }
        return 0xFFF0AB2A;
    }

    public String getShareMessage(HeWeather6<HeBasic> weather) {
        StringBuilder message = new StringBuilder();
        message.append(weather.getBasic().getLocation());
        message.append("天气：");
        message.append(weather.getNow().getCond_txt());
        message.append("，");
        message.append(weather.getNow().getFl()).append("℃");
        message.append("。");
        message.append("\r\n");
        message.append("发布：");
        message.append(weather.getUpdate().getLoc());
        if (weather.getAqi() != null) {
            message.append("\r\n");
            message.append("PM2.5：").append(weather.getAqi().getPm25());
            message.append("μg/m³ ");
            message.append(weather.getAqi().getQlty());
            message.append("。");
        }
        message.append("\r\n");
        message.append("今天：");
        message.append(weather.getDaily_forecast().get(0).getTmp_min()).append("℃-");
        message.append(weather.getDaily_forecast().get(0).getTmp_max()).append("℃");
        message.append("，");
        message.append(weather.getDaily_forecast().get(0).getCond_txt_d());
        message.append("\r\n");
        message.append("明天：");
        message.append(weather.getDaily_forecast().get(1).getTmp_min()).append("℃-");
        message.append(weather.getDaily_forecast().get(1).getTmp_max()).append("℃");
        message.append("，");
        message.append(weather.getDaily_forecast().get(1).getCond_txt_d());

        return message.toString();
    }

    /**
     * 把Weather转换为对应的BaseDrawer.Type
     */
    public static @DrawableRes
    int convertWeatherIcon(String weatherCode) {
        final int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        final boolean isNotNight = hourOfDay >= 7 && hourOfDay <= 18;
        final int w = Integer.valueOf(TextUtils.isEmpty(weatherCode) ? "999" : weatherCode);
        switch (w) {
            case 100:
                return isNotNight ? R.drawable.ic_stat_icon_sun : R.drawable.ic_stat_icon_sun_night;
            case 101:// 多云
            case 102:// 少云
            case 103:// 晴间多云
                return isNotNight ? R.drawable.ic_stat_icon_cloudy : R.drawable.ic_stat_icon_cloudy_night;
            case 104:// 阴
                return R.drawable.ic_stat_icon_overcast;
            // 200 - 213是风
            case 200:
            case 201:
            case 202:
            case 203:
            case 204:
            case 205:
            case 206:
            case 207:
            case 208:
            case 209:
            case 210:
            case 211:
            case 212:
            case 213:
                return R.drawable.ic_stat_icon_sun;
            case 300:// 阵雨Shower Rain
            case 305:// 小雨 Light Rain
            case 308:// 极端降雨 Extreme Rain
            case 309:// 毛毛雨/细雨 Drizzle Rain
                return R.drawable.ic_stat_icon_lightrain;
            case 301:// 强阵雨 Heavy Shower Rain
            case 302:// 雷阵雨 Thundershower
            case 303:// 强雷阵雨 Heavy Thunderstorm
            case 304:// 雷阵雨伴有冰雹 Hail
                return R.drawable.ic_stat_icon_thundershower;
            case 306:// 中雨 Moderate Rain
            case 307:// 大雨 Heavy Rain
                return R.drawable.ic_stat_icon_moderaterain;
            case 310:// 暴雨 Storm
            case 311:// 大暴雨 Heavy Storm
            case 312:// 特大暴雨 Severe Storm
                return R.drawable.ic_stat_icon_heavyrain;
            case 313:// 冻雨 Freezing Rain
                return R.drawable.ic_stat_icon_icerain;
            case 400:// 小雪 Light Snow
            case 401:// 中雪 Moderate Snow
            case 407:// 阵雪 Snow Flurry
                return R.drawable.ic_stat_icon_lightsnow;
            case 402:// 大雪 Heavy Snow
            case 403:// 暴雪 Snowstorm
                return R.drawable.ic_stat_icon_snowstorm;
            case 404:// 雨夹雪 Sleet
            case 405:// 雨雪天气 Rain And Snow
            case 406:// 阵雨夹雪 Shower Snow
                return R.drawable.ic_stat_icon_sleet;
            case 500:// 薄雾
            case 501:// 雾
                return R.drawable.ic_stat_icon_foggy;
            case 502:// 霾
            case 504:// 浮尘
                return R.drawable.ic_stat_icon_haze;
            case 503:// 扬沙
            case 506:// 火山灰
            case 507:// 沙尘暴
            case 508:// 强沙尘暴
                return R.drawable.ic_stat_icon_sand;
            default:
                return R.drawable.ic_stat_icon_na;
        }
    }

    /**
     * 是否是今天2015-11-05 04:00 合法data格式： 2015-11-05 04:00 或者2015-11-05
     */
    public static boolean isToday(String date) {
        if (TextUtils.isEmpty(date) || date.length() < 10) {// 2015-11-05
            // length=10
            return false;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String today = format.format(new Date());
            if (TextUtils.equals(today, date.substring(0, 10))) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 转换日期2015-11-05为今天、明天、昨天，或者是星期几
     */
    public static String prettyDate(String date) {
        try {
            final String[] strs = date.split("-");
            final int year = Integer.valueOf(strs[0]);
            final int month = Integer.valueOf(strs[1]);
            final int day = Integer.valueOf(strs[2]);
            Calendar c = Calendar.getInstance();
            int curYear = c.get(Calendar.YEAR);
            int curMonth = c.get(Calendar.MONTH) + 1;// Java月份从0月开始
            int curDay = c.get(Calendar.DAY_OF_MONTH);
            if (curYear == year && curMonth == month) {
                if (curDay == day) {
                    return "今天";
                } else if ((curDay + 1) == day) {
                    return "明天";
                } else if ((curDay - 1) == day) {
                    return "昨天";
                }
            }
            c.set(year, month - 1, day);
            // http://www.tuicool.com/articles/Avqauq
            // 一周第一天是否为星期天
            boolean isFirstSunday = (c.getFirstDayOfWeek() == Calendar.SUNDAY);
            // 获取周几
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            // 若一周第一天为星期天，则-1
            if (isFirstSunday) {
                dayOfWeek = dayOfWeek - 1;
                if (dayOfWeek == 0) {
                    dayOfWeek = 7;
                }
            }
            // 若当天为2014年10月13日（星期一），则打印输出：1
            // 若当天为2014年10月17日（星期五），则打印输出：5
            // 若当天为2014年10月19日（星期日），则打印输出：7
            switch (dayOfWeek) {
                case 1:
                    return "周一";
                case 2:
                    return "周二";
                case 3:
                    return "周三";
                case 4:
                    return "周四";
                case 5:
                    return "周五";
                case 6:
                    return "周六";
                case 7:
                    return "周日";
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    private static WeatherType getHeWeatherType(Resources context, ShortWeatherInfo info) {
        if (info != null && TextUtils.isDigitsOnly(info.getCode())) {
            int code = Integer.parseInt(info.getCode());
            if (code == 100) {//晴
                return new SunnyType(context, info);
            } else if (code >= 101 && code <= 103) {//多云
                SunnyType sunnyType = new SunnyType(context, info);
                sunnyType.setCloud(true);
                return sunnyType;
            } else if (code == 104) {//阴
                return new OvercastType(context, info);
            } else if (code >= 200 && code <= 213) {//各种风
                return new SunnyType(context, info);
            } else if (code >= 300 && code <= 303) {//各种阵雨
                if (code >= 300 && code <= 301) {
                    return new RainType(context, RainType.RAIN_LEVEL_2, RainType.WIND_LEVEL_2);
                } else {
                    RainType rainType = new RainType(context, RainType.RAIN_LEVEL_2, RainType.WIND_LEVEL_2);
                    rainType.setFlashing(true);
                    return rainType;
                }
            } else if (code == 304) {//阵雨加冰雹
                return new HailType(context);
            } else if (code >= 305 && code <= 312) {//各种雨
                if (code == 305 || code == 309) {//小雨
                    return new RainType(context, RainType.RAIN_LEVEL_1, RainType.WIND_LEVEL_1);
                } else if (code == 306) {//中雨
                    return new RainType(context, RainType.RAIN_LEVEL_2, RainType.WIND_LEVEL_2);
                } else//大到暴雨
                    return new RainType(context, RainType.RAIN_LEVEL_3, RainType.WIND_LEVEL_3);
            } else if (code == 313) {//冻雨
                return new HailType(context);
            } else if (code >= 400 && code <= 407) {//各种雪
                if (code == 400) {
                    return new SnowType(context, SnowType.SNOW_LEVEL_1);
                } else if (code == 401) {
                    return new SnowType(context, SnowType.SNOW_LEVEL_2);
                } else if (code <= 403) {
                    return new SnowType(context, SnowType.SNOW_LEVEL_3);
                } else if (code <= 406) {
                    RainType rainSnowType = new RainType(context, RainType.RAIN_LEVEL_1, RainType.WIND_LEVEL_1);
                    rainSnowType.setSnowing(true);
                    return rainSnowType;
                } else {
                    return new SnowType(context, SnowType.SNOW_LEVEL_2);
                }
            } else if (code >= 500 && code <= 501) {//雾
                return new FogType(context);
            } else if (code == 502) {//霾
                return new HazeType(context);
            } else if (code >= 503 && code <= 508) {//各种沙尘暴
                return new SandstormType(context);
            } else if (code == 900) {//热
                return new SunnyType(context, info);
            } else if (code == 901) {//冷
                return new SnowType(context, SnowType.SNOW_LEVEL_1);
            } else {//未知
                return new DefaultType(context);
            }
        } else
            return null;
    }

    public static WeatherType getType(Resources context, ShortWeatherInfo info) {
        return getHeWeatherType(context, info);
    }
}
