package com.light.weather.bean;

import android.text.TextUtils;

import java.util.List;

/**
 * Created by android on 16-11-10.
 */

public class HeCity {


    /**
     * basic : {"city":"深圳","cnty":"中国","id":"CN101280601","lat":"22.544000","lon":"114.109000","prov":"广东"}
     * status : ok
     */

    private List<HeWeather5Bean> HeWeather6;

    public List<HeWeather5Bean> getHeWeather5() {
        return HeWeather6;
    }

    public boolean isOK() {
        return HeWeather6 != null && !HeWeather6.isEmpty() && TextUtils.equals("ok", HeWeather6.get(0).getStatus());
    }

    @Override
    public String
    toString() {
        return "HeCity{" +
                "HeWeather5=" + HeWeather6 +
                '}';
    }

    public static class HeWeather5Bean {
        /**
         * city : 深圳 cnty : 中国 id : CN101280601 lat : 22.544000 lon : 114.109000 prov : 广东
         */

        private BasicBean basic;
        private String status;

        public BasicBean getBasic() {
            return basic;
        }

        public void setBasic(BasicBean basic) {
            this.basic = basic;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "HeWeather5Bean{" +
                    "basic=" + basic +
                    ", status='" + status + '\'' +
                    '}';
        }

        public static class BasicBean {
            private String cid;
            private String location;
            private String parent_city;
            private String admin_area;
            private String cnty;
            private String lat;
            private String lon;
            private String tz;
            private String type;

            public String getLocation() {
                return location;
            }

            public void setLocation(String city) {
                this.location = city;
            }

            public String getCnty() {
                return cnty;
            }

            public String getCid() {
                return cid;
            }

            public String getLat() {
                return lat;
            }

            public String getLon() {
                return lon;
            }

            public String getProv() {
                return admin_area;
            }

            public void setCid(String cid) {
                this.cid = cid;
            }

            public String getParent_city() {
                return parent_city;
            }

            public void setParent_city(String parent_city) {
                this.parent_city = parent_city;
            }

            public String getAdmin_area() {
                return admin_area;
            }

            public void setAdmin_area(String admin_area) {
                this.admin_area = admin_area;
            }

            public void setCnty(String cnty) {
                this.cnty = cnty;
            }

            public void setLat(String lat) {
                this.lat = lat;
            }

            public void setLon(String lon) {
                this.lon = lon;
            }

            public String getTz() {
                return tz;
            }

            public void setTz(String tz) {
                this.tz = tz;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            @Override
            public String toString() {
                return "BasicBean{" +
                        "cid='" + cid + '\'' +
                        ", location='" + location + '\'' +
                        ", parent_city='" + parent_city + '\'' +
                        ", admin_area='" + admin_area + '\'' +
                        ", cnty='" + cnty + '\'' +
                        ", lat='" + lat + '\'' +
                        ", lon='" + lon + '\'' +
                        ", tz='" + tz + '\'' +
                        ", type='" + type + '\'' +
                        '}';
            }
        }
    }
}
