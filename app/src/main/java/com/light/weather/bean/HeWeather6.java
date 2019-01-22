package com.light.weather.bean;

import java.util.List;

public class HeWeather6<T> {

    /**
     * basic : {"cid":"CN101010100","location":"北京","parent_city":"北京","admin_area":"北京","cnty":"中国","lat":"39.90498734","lon":"116.40528870","tz":"8.0"}
     * daily_forecast : [{"cond_code_d":"103","cond_code_n":"101","cond_txt_d":"晴间多云","cond_txt_n":"多云","date":"2017-10-26","hum":"57","pcpn":"0.0","pop":"0","pres":"1020","tmp_max":"16","tmp_min":"8","uv_index":"3","vis":"16","wind_deg":"0","wind_dir":"无持续风向","wind_sc":"微风","wind_spd":"5"},{"cond_code_d":"101","cond_code_n":"501","cond_txt_d":"多云","cond_txt_n":"雾","date":"2017-10-27","hum":"56","pcpn":"0.0","pop":"0","pres":"1018","tmp_max":"18","tmp_min":"9","uv_index":"3","vis":"20","wind_deg":"187","wind_dir":"南风","wind_sc":"微风","wind_spd":"6"},{"cond_code_d":"101","cond_code_n":"101","cond_txt_d":"多云","cond_txt_n":"多云","date":"2017-10-28","hum":"26","pcpn":"0.0","pop":"0","pres":"1029","tmp_max":"17","tmp_min":"5","uv_index":"2","vis":"20","wind_deg":"2","wind_dir":"北风","wind_sc":"3-4","wind_spd":"19"}]
     * hourly : [{"cloud":"8","cond_code":"100","cond_txt":"晴","hum":"84","pop":"0","pres":"1018","time":"2017-10-27 01:00","tmp":"8","wind_deg":"49","wind_dir":"东北风","wind_sc":"微风","wind_spd":"2"},{"cloud":"8","cond_code":"100","cond_txt":"晴","hum":"81","pop":"0","pres":"1018","time":"2017-10-27 04:00","tmp":"8","wind_deg":"29","wind_dir":"东北风","wind_sc":"微风","wind_spd":"2"},{"cloud":"6","cond_code":"100","cond_txt":"晴","hum":"95","pop":"0","pres":"1019","time":"2017-10-27 07:00","tmp":"8","wind_deg":"37","wind_dir":"东北风","wind_sc":"微风","wind_spd":"2"},{"cloud":"2","cond_code":"100","cond_txt":"晴","hum":"75","pop":"0","pres":"1018","time":"2017-10-27 10:00","tmp":"14","wind_deg":"108","wind_dir":"东南风","wind_sc":"微风","wind_spd":"3"},{"cloud":"0","cond_code":"100","cond_txt":"晴","hum":"62","pop":"0","pres":"1016","time":"2017-10-27 13:00","tmp":"16","wind_deg":"158","wind_dir":"东南风","wind_sc":"微风","wind_spd":"6"},{"cloud":"0","cond_code":"100","cond_txt":"晴","hum":"73","pop":"0","pres":"1016","time":"2017-10-27 16:00","tmp":"15","wind_deg":"162","wind_dir":"东南风","wind_sc":"微风","wind_spd":"6"},{"cloud":"3","cond_code":"100","cond_txt":"晴","hum":"92","pop":"0","pres":"1018","time":"2017-10-27 19:00","tmp":"13","wind_deg":"206","wind_dir":"西南风","wind_sc":"微风","wind_spd":"4"},{"cloud":"19","cond_code":"100","cond_txt":"晴","hum":"96","pop":"0","pres":"1019","time":"2017-10-27 22:00","tmp":"13","wind_deg":"212","wind_dir":"西南风","wind_sc":"微风","wind_spd":"1"}]
     * lifestyle : [{"brf":"舒适","txt":"今天夜间不太热也不太冷，风力不大，相信您在这样的天气条件下，应会感到比较清爽和舒适。","type":"comf"},{"brf":"较舒适","txt":"建议着薄外套、开衫牛仔衫裤等服装。年老体弱者应适当添加衣物，宜着夹克衫、薄毛衣等。","type":"drsg"},{"brf":"少发","txt":"各项气象条件适宜，无明显降温过程，发生感冒机率较低。","type":"flu"},{"brf":"适宜","txt":"天气较好，赶快投身大自然参与户外运动，尽情感受运动的快乐吧。","type":"sport"},{"brf":"适宜","txt":"天气较好，但丝毫不会影响您出行的心情。温度适宜又有微风相伴，适宜旅游。","type":"trav"},{"brf":"弱","txt":"紫外线强度较弱，建议出门前涂擦SPF在12-15之间、PA+的防晒护肤品。","type":"uv"},{"brf":"较不宜","txt":"较不宜洗车，未来一天无雨，风力较大，如果执意擦洗汽车，要做好蒙上污垢的心理准备。","type":"cw"},{"brf":"较差","txt":"气象条件较不利于空气污染物稀释、扩散和清除，请适当减少室外活动时间。","type":"air"}]
     * now : {"cond_code":"501","cond_txt":"雾","fl":"8","hum":"94","pcpn":"0","pres":"1018","tmp":"9","vis":"2","wind_deg":"48","wind_dir":"东北风","wind_sc":"微风","wind_spd":"7"}
     * status : ok
     * update : {"loc":"2017-10-26 23:09","utc":"2017-10-26 15:09"}
     */
    protected long updateTime;//不会被gson解析的字段
    private T basic;
    private NowBean now;
    private String status;
    private UpdateBean update;
    private List<DailyForecastBean> daily_forecast;
    private List<HourlyBean> hourly;
    private List<LifestyleBean> lifestyle;
    private AqiBean air_now_city;
    private List<AlarmsBean> alarm;

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public AqiBean getAqi() {
        return air_now_city;
    }

    public void setAqi(AqiBean air_now_city) {
        this.air_now_city = air_now_city;
    }

    public void setAlarm(List<AlarmsBean> alarm) {
        this.alarm = alarm;
    }

    public List<AlarmsBean> getAlarm() {
        return alarm;
    }

    public T getBasic() {
        return basic;
    }

    public void setBasic(T basic) {
        this.basic = basic;
    }

    public NowBean getNow() {
        return now;
    }

    public void setNow(NowBean now) {
        this.now = now;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UpdateBean getUpdate() {
        return update;
    }

    public void setUpdate(UpdateBean update) {
        this.update = update;
    }

    public List<DailyForecastBean> getDaily_forecast() {
        return daily_forecast;
    }

    public void setDaily_forecast(List<DailyForecastBean> daily_forecast) {
        this.daily_forecast = daily_forecast;
    }

    public List<HourlyBean> getHourly() {
        return hourly;
    }

    public void setHourly(List<HourlyBean> hourly) {
        this.hourly = hourly;
    }

    public List<LifestyleBean> getLifestyle() {
        return lifestyle;
    }

    public void setLifestyle(List<LifestyleBean> lifestyle) {
        this.lifestyle = lifestyle;
    }

    public static class NowBean {
        /**
         * cond_code : 501
         * cond_txt : 雾
         * fl : 8
         * hum : 94
         * pcpn : 0
         * pres : 1018
         * tmp : 9
         * vis : 2
         * wind_deg : 48
         * wind_dir : 东北风
         * wind_sc : 微风
         * wind_spd : 7
         */

        private String cond_code;
        private String cond_txt;
        private String fl;
        private String hum;
        private String pcpn;
        private String pres;
        private String tmp;
        private String vis;
        private String wind_deg;
        private String wind_dir;
        private String wind_sc;
        private String wind_spd;

        public String getCond_code() {
            return cond_code;
        }

        public void setCond_code(String cond_code) {
            this.cond_code = cond_code;
        }

        public String getCond_txt() {
            return cond_txt;
        }

        public void setCond_txt(String cond_txt) {
            this.cond_txt = cond_txt;
        }

        public String getFl() {
            return fl;
        }

        public void setFl(String fl) {
            this.fl = fl;
        }

        public String getHum() {
            return hum;
        }

        public void setHum(String hum) {
            this.hum = hum;
        }

        public String getPcpn() {
            return pcpn;
        }

        public void setPcpn(String pcpn) {
            this.pcpn = pcpn;
        }

        public String getPres() {
            return pres;
        }

        public void setPres(String pres) {
            this.pres = pres;
        }

        public String getTmp() {
            return tmp;
        }

        public void setTmp(String tmp) {
            this.tmp = tmp;
        }

        public String getVis() {
            return vis;
        }

        public void setVis(String vis) {
            this.vis = vis;
        }

        public String getWind_deg() {
            return wind_deg;
        }

        public void setWind_deg(String wind_deg) {
            this.wind_deg = wind_deg;
        }

        public String getWind_dir() {
            return wind_dir;
        }

        public void setWind_dir(String wind_dir) {
            this.wind_dir = wind_dir;
        }

        public String getWind_sc() {
            return wind_sc;
        }

        public void setWind_sc(String wind_sc) {
            this.wind_sc = wind_sc;
        }

        public String getWind_spd() {
            return wind_spd;
        }

        public void setWind_spd(String wind_spd) {
            this.wind_spd = wind_spd;
        }
    }

    public static class UpdateBean {
        /**
         * loc : 2017-10-26 23:09
         * utc : 2017-10-26 15:09
         */

        private String loc;
        private String utc;

        public String getLoc() {
            return loc;
        }

        public void setLoc(String loc) {
            this.loc = loc;
        }

        public String getUtc() {
            return utc;
        }

        public void setUtc(String utc) {
            this.utc = utc;
        }
    }

    public static class DailyForecastBean {
        /**
         * cond_code_d : 305
         * cond_code_n : 305
         * cond_txt_d : 小雨
         * cond_txt_n : 小雨
         * date : 2018-01-28
         * hum : 64
         * mr : 14:50
         * ms : 03:25
         * pcpn : 0.5
         * pop : 86
         * pres : 1016
         * sr : 07:03
         * ss : 18:09
         * tmp_max : 18
         * tmp_min : 8
         * uv_index : 6
         * vis : 18
         * wind_deg : 1
         * wind_dir : 北风
         * wind_sc : 4-5
         * wind_spd : 24
         */

        private String cond_code_d;
        private String cond_code_n;
        private String cond_txt_d;
        private String cond_txt_n;
        private String date;
        private String hum;
        private String mr;
        private String ms;
        private String pcpn;
        private String pop;
        private String pres;
        private String sr;
        private String ss;
        private String tmp_max;
        private String tmp_min;
        private String uv_index;
        private String vis;
        private String wind_deg;
        private String wind_dir;
        private String wind_sc;
        private String wind_spd;

        public String getCond_code_d() {
            return cond_code_d;
        }

        public void setCond_code_d(String cond_code_d) {
            this.cond_code_d = cond_code_d;
        }

        public String getCond_code_n() {
            return cond_code_n;
        }

        public void setCond_code_n(String cond_code_n) {
            this.cond_code_n = cond_code_n;
        }

        public String getCond_txt_d() {
            return cond_txt_d;
        }

        public void setCond_txt_d(String cond_txt_d) {
            this.cond_txt_d = cond_txt_d;
        }

        public String getCond_txt_n() {
            return cond_txt_n;
        }

        public void setCond_txt_n(String cond_txt_n) {
            this.cond_txt_n = cond_txt_n;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getHum() {
            return hum;
        }

        public void setHum(String hum) {
            this.hum = hum;
        }

        public String getMr() {
            return mr;
        }

        public void setMr(String mr) {
            this.mr = mr;
        }

        public String getMs() {
            return ms;
        }

        public void setMs(String ms) {
            this.ms = ms;
        }

        public String getPcpn() {
            return pcpn;
        }

        public void setPcpn(String pcpn) {
            this.pcpn = pcpn;
        }

        public String getPop() {
            return pop;
        }

        public void setPop(String pop) {
            this.pop = pop;
        }

        public String getPres() {
            return pres;
        }

        public void setPres(String pres) {
            this.pres = pres;
        }

        public String getSr() {
            return sr;
        }

        public void setSr(String sr) {
            this.sr = sr;
        }

        public String getSs() {
            return ss;
        }

        public void setSs(String ss) {
            this.ss = ss;
        }

        public String getTmp_max() {
            return tmp_max;
        }

        public void setTmp_max(String tmp_max) {
            this.tmp_max = tmp_max;
        }

        public String getTmp_min() {
            return tmp_min;
        }

        public void setTmp_min(String tmp_min) {
            this.tmp_min = tmp_min;
        }

        public String getUv_index() {
            return uv_index;
        }

        public void setUv_index(String uv_index) {
            this.uv_index = uv_index;
        }

        public String getVis() {
            return vis;
        }

        public void setVis(String vis) {
            this.vis = vis;
        }

        public String getWind_deg() {
            return wind_deg;
        }

        public void setWind_deg(String wind_deg) {
            this.wind_deg = wind_deg;
        }

        public String getWind_dir() {
            return wind_dir;
        }

        public void setWind_dir(String wind_dir) {
            this.wind_dir = wind_dir;
        }

        public String getWind_sc() {
            return wind_sc;
        }

        public void setWind_sc(String wind_sc) {
            this.wind_sc = wind_sc;
        }

        public String getWind_spd() {
            return wind_spd;
        }

        public void setWind_spd(String wind_spd) {
            this.wind_spd = wind_spd;
        }

        @Override
        public String toString() {
            return "DailyForecastBean{" +
                    "cond_code_d='" + cond_code_d + '\'' +
                    ", cond_code_n='" + cond_code_n + '\'' +
                    ", cond_txt_d='" + cond_txt_d + '\'' +
                    ", cond_txt_n='" + cond_txt_n + '\'' +
                    ", date='" + date + '\'' +
                    ", hum='" + hum + '\'' +
                    ", mr='" + mr + '\'' +
                    ", ms='" + ms + '\'' +
                    ", pcpn='" + pcpn + '\'' +
                    ", pop='" + pop + '\'' +
                    ", pres='" + pres + '\'' +
                    ", sr='" + sr + '\'' +
                    ", ss='" + ss + '\'' +
                    ", tmp_max='" + tmp_max + '\'' +
                    ", tmp_min='" + tmp_min + '\'' +
                    ", uv_index='" + uv_index + '\'' +
                    ", vis='" + vis + '\'' +
                    ", wind_deg='" + wind_deg + '\'' +
                    ", wind_dir='" + wind_dir + '\'' +
                    ", wind_sc='" + wind_sc + '\'' +
                    ", wind_spd='" + wind_spd + '\'' +
                    '}';
        }
    }

    public static class HourlyBean {
        /**
         * cloud : 8
         * cond_code : 100
         * cond_txt : 晴
         * hum : 84
         * pop : 0
         * pres : 1018
         * time : 2017-10-27 01:00
         * tmp : 8
         * wind_deg : 49
         * wind_dir : 东北风
         * wind_sc : 微风
         * wind_spd : 2
         */

        private String cloud;
        private String cond_code;
        private String cond_txt;
        private String hum;
        private String pop;
        private String pres;
        private String time;
        private String tmp;
        private String wind_deg;
        private String wind_dir;
        private String wind_sc;
        private String wind_spd;

        public String getCloud() {
            return cloud;
        }

        public void setCloud(String cloud) {
            this.cloud = cloud;
        }

        public String getCond_code() {
            return cond_code;
        }

        public void setCond_code(String cond_code) {
            this.cond_code = cond_code;
        }

        public String getCond_txt() {
            return cond_txt;
        }

        public void setCond_txt(String cond_txt) {
            this.cond_txt = cond_txt;
        }

        public String getHum() {
            return hum;
        }

        public void setHum(String hum) {
            this.hum = hum;
        }

        public String getPop() {
            return pop;
        }

        public void setPop(String pop) {
            this.pop = pop;
        }

        public String getPres() {
            return pres;
        }

        public void setPres(String pres) {
            this.pres = pres;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getTmp() {
            return tmp;
        }

        public void setTmp(String tmp) {
            this.tmp = tmp;
        }

        public String getWind_deg() {
            return wind_deg;
        }

        public void setWind_deg(String wind_deg) {
            this.wind_deg = wind_deg;
        }

        public String getWind_dir() {
            return wind_dir;
        }

        public void setWind_dir(String wind_dir) {
            this.wind_dir = wind_dir;
        }

        public String getWind_sc() {
            return wind_sc;
        }

        public void setWind_sc(String wind_sc) {
            this.wind_sc = wind_sc;
        }

        public String getWind_spd() {
            return wind_spd;
        }

        public void setWind_spd(String wind_spd) {
            this.wind_spd = wind_spd;
        }
    }

    public static class LifestyleBean {
        /**
         * brf : 舒适
         * txt : 今天夜间不太热也不太冷，风力不大，相信您在这样的天气条件下，应会感到比较清爽和舒适。
         * type : comf
         */

        private String brf;
        private String txt;
        private String type;

        public String getBrf() {
            return brf;
        }

        public void setBrf(String brf) {
            this.brf = brf;
        }

        public String getTxt() {
            return txt;
        }

        public void setTxt(String txt) {
            this.txt = txt;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class AqiBean {

        /**
         * aqi : 19
         * co : 0
         * main :
         * no2 : 34
         * o3 : 31
         * pm10 : 18
         * pm25 : 8
         * pub_time : 2017-11-07 22:00
         * qlty : 优
         * so2 : 2
         */

        private String aqi;
        private String co;
        private String main;
        private String no2;
        private String o3;
        private String pm10;
        private String pm25;
        private String pub_time;
        private String qlty;
        private String so2;

        public String getAqi() {
            return aqi;
        }

        public void setAqi(String aqi) {
            this.aqi = aqi;
        }

        public String getCo() {
            return co;
        }

        public void setCo(String co) {
            this.co = co;
        }

        public String getMain() {
            return main;
        }

        public void setMain(String main) {
            this.main = main;
        }

        public String getNo2() {
            return no2;
        }

        public void setNo2(String no2) {
            this.no2 = no2;
        }

        public String getO3() {
            return o3;
        }

        public void setO3(String o3) {
            this.o3 = o3;
        }

        public String getPm10() {
            return pm10;
        }

        public void setPm10(String pm10) {
            this.pm10 = pm10;
        }

        public String getPm25() {
            return pm25;
        }

        public void setPm25(String pm25) {
            this.pm25 = pm25;
        }

        public String getPub_time() {
            return pub_time;
        }

        public void setPub_time(String pub_time) {
            this.pub_time = pub_time;
        }

        public String getQlty() {
            return qlty;
        }

        public void setQlty(String qlty) {
            this.qlty = qlty;
        }

        public String getSo2() {
            return so2;
        }

        public void setSo2(String so2) {
            this.so2 = so2;
        }
    }

    public static class AlarmsBean {
        private String title;
        private String stat;
        private String level;
        private String type;
        private String txt;

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getStat() {
            return stat;
        }

        public void setStat(String stat) {
            this.stat = stat;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTxt() {
            return txt;
        }

        public void setTxt(String txt) {
            this.txt = txt;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
