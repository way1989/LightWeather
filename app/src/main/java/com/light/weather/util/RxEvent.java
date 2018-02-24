package com.light.weather.util;

import com.light.weather.bean.City;

import java.util.List;

/**
 * Created by android on 16-11-25.
 */

public final class RxEvent {
    public static class MainEvent {
        public List<City> cities;
        public int position;

        public MainEvent(List<City> cities, int position) {
            this.cities = cities;
            this.position = position;
        }

    }
}
