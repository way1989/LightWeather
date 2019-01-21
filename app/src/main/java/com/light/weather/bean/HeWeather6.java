package com.light.weather.bean;

import java.util.List;

public class HeWeather6 {

    private List<HeWeather6Bean> HeWeather6;

    public List<HeWeather6Bean> getHeWeather6() {
        return HeWeather6;
    }

    public void setHeWeather6(List<HeWeather6Bean> HeWeather6) {
        this.HeWeather6 = HeWeather6;
    }

    public static class HeWeather6Bean {
        /**
         * basic : [{"cid":"CN101280606","location":"龙岗","parent_city":"深圳","admin_area":"广东","cnty":"中国","lat":"22.72151184","lon":"114.25137329","tz":"+8.00","type":"city"}]
         * status : ok
         */

        private String status;
        private List<BasicBean> basic;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<BasicBean> getBasic() {
            return basic;
        }

        public void setBasic(List<BasicBean> basic) {
            this.basic = basic;
        }

        public static class BasicBean {
            /**
             * cid : CN101280606
             * location : 龙岗
             * parent_city : 深圳
             * admin_area : 广东
             * cnty : 中国
             * lat : 22.72151184
             * lon : 114.25137329
             * tz : +8.00
             * type : city
             */

            private String cid;
            private String location;
            private String parent_city;
            private String admin_area;
            private String cnty;
            private String lat;
            private String lon;
            private String tz;
            private String type;

            public String getCid() {
                return cid;
            }

            public void setCid(String cid) {
                this.cid = cid;
            }

            public String getLocation() {
                return location;
            }

            public void setLocation(String location) {
                this.location = location;
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

            public String getCnty() {
                return cnty;
            }

            public void setCnty(String cnty) {
                this.cnty = cnty;
            }

            public String getLat() {
                return lat;
            }

            public void setLat(String lat) {
                this.lat = lat;
            }

            public String getLon() {
                return lon;
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
        }
    }
}
