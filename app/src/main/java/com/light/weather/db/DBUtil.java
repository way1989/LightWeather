package com.light.weather.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.light.weather.bean.City;

import java.util.ArrayList;

/**
 * Created by android on 16-11-15.
 */

public class DBUtil {
    private static final String TAG = "DBUtil";

    public static ArrayList<City> getCityFromCache(Context context) {
        ArrayList<City> cities = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(CityProvider.CITY_CONTENT_URI, null, null, null,
                CityProvider.CityConstants.DEFAULT_SORT_ORDER);
        Log.d(TAG, "getCityFromCache cursor = " + cursor);
        if (cursor == null) return cities;
        Log.d(TAG, "getCityFromCache cursor.size = " + cursor.getCount());
        if (cursor.getCount() < 1) {
            cursor.close();
            return cities;
        }
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            String city = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.CITY));
            String country = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.COUNTRY));
            String areaId = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.AREA_ID));
            String lat = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.LATITUDE));
            String lon = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.LONGITUDE));
            String prov = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.PROVINCE));
            int isLocation = cursor.getInt(cursor.getColumnIndex(CityProvider.CityConstants.IS_LOCATION));
            String codeTxt = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.WEATHER_NAME));
            String code = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.WEATHER_CODE));
            String tmp = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.WEATHER_TEMPERATURE));
            City c = new City(city, country, areaId, lat, lon, prov);
            c.setLocation(isLocation == 1);
            c.setCodeTxt(codeTxt);
            c.setCode(code);
            c.setTmp(tmp);
            cities.add(c);
        }
        if (!cursor.isClosed()) cursor.close();
        Log.d(TAG, "getCityFromCache cities.size = " + cities.size());
        return cities;
    }

    public static boolean isExist(Context context, City city) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(CityProvider.CITY_CONTENT_URI,
                new String[]{CityProvider.CityConstants.CITY},
                CityProvider.CityConstants.AREA_ID + "=?", new String[]{city.getAreaId()}, null);
        Log.d(TAG, "getCityFromCache cursor = " + cursor);
        if (cursor == null) return false;
        int size = cursor.getCount();
        Log.d(TAG, "getCityFromCache cursor.size = " + size);
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return size > 0;
    }

    public static boolean addCity(Context context, City city, boolean autoLocation) {
        ContentValues values = new ContentValues();
        values.put(CityProvider.CityConstants.CITY, city.getCity());
        values.put(CityProvider.CityConstants.AREA_ID, city.getAreaId());
        values.put(CityProvider.CityConstants.COUNTRY, city.getCountry());
        values.put(CityProvider.CityConstants.LATITUDE, city.getLat());
        values.put(CityProvider.CityConstants.LONGITUDE, city.getLon());
        values.put(CityProvider.CityConstants.PROVINCE, city.getProv());
        values.put(CityProvider.CityConstants.IS_LOCATION, autoLocation ? 1 : 0);
        values.put(CityProvider.CityConstants.ORDER_INDEX, getCacheCitySize(context));
        values.put(CityProvider.CityConstants.WEATHER_NAME, city.getCodeTxt());
        values.put(CityProvider.CityConstants.WEATHER_CODE, city.getCode());
        values.put(CityProvider.CityConstants.WEATHER_TEMPERATURE, city.getTmp());

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = contentResolver.insert(CityProvider.CITY_CONTENT_URI, values);
        return uri != null;
    }

    public static boolean updateCity(Context context, City city, boolean autoLocation) {
        ContentValues values = new ContentValues();
        values.put(CityProvider.CityConstants.CITY, city.getCity());
        values.put(CityProvider.CityConstants.AREA_ID, city.getAreaId());
        values.put(CityProvider.CityConstants.COUNTRY, city.getCountry());
        values.put(CityProvider.CityConstants.LATITUDE, city.getLat());
        values.put(CityProvider.CityConstants.LONGITUDE, city.getLon());
        values.put(CityProvider.CityConstants.PROVINCE, city.getProv());
        values.put(CityProvider.CityConstants.WEATHER_NAME, city.getCodeTxt());
        values.put(CityProvider.CityConstants.WEATHER_CODE, city.getCode());
        values.put(CityProvider.CityConstants.WEATHER_TEMPERATURE, city.getTmp());
        ContentResolver contentResolver = context.getContentResolver();
        String where = autoLocation ? CityProvider.CityConstants.IS_LOCATION + "=?"
                : CityProvider.CityConstants.AREA_ID + "=?";
        String[] selectionArgs = autoLocation ? new String[]{"1"} : new String[]{city.getAreaId()};
        int rowsModified = contentResolver.update(CityProvider.CITY_CONTENT_URI,
                values, where, selectionArgs);
        if (rowsModified == 0) {
            values.put(CityProvider.CityConstants.IS_LOCATION, autoLocation ? 1 : 0);
            values.put(CityProvider.CityConstants.ORDER_INDEX, getCacheCitySize(context));
            // If no prior row existed, insert a new one
            Uri uri = contentResolver.insert(CityProvider.CITY_CONTENT_URI, values);
            return uri != null;
        }
        return rowsModified > 0;
    }

    public static int getCacheCitySize(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(CityProvider.CITY_CONTENT_URI,
                new String[]{CityProvider.CityConstants.CITY}, null, null, null);
        Log.d(TAG, "getCityFromCache cursor = " + cursor);
        if (cursor == null) return 0;
        int size = cursor.getCount();
        Log.d(TAG, "getCityFromCache cursor.size = " + size);
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return size;
    }

    public static boolean updateIndex(Context context, City city, int i) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CityProvider.CityConstants.ORDER_INDEX, i);
        int rowsModified = contentResolver.update(CityProvider.CITY_CONTENT_URI,
                values, CityProvider.CityConstants.AREA_ID + "=?",
                new String[]{city.getAreaId()});
        return rowsModified > 0;
    }

    public static boolean deleteCity(Context context, City city) {
        ContentResolver contentResolver = context.getContentResolver();
        int rowsModified = contentResolver.delete(CityProvider.CITY_CONTENT_URI,
                CityProvider.CityConstants.AREA_ID + "=?", new String[]{city.getAreaId()});
        return rowsModified > 0;
    }

    public static boolean undoCity(Context context, City city) {
        ContentValues values = new ContentValues();
        values.put(CityProvider.CityConstants.CITY, city.getCity());
        values.put(CityProvider.CityConstants.AREA_ID, city.getAreaId());
        values.put(CityProvider.CityConstants.COUNTRY, city.getCountry());
        values.put(CityProvider.CityConstants.LATITUDE, city.getLat());
        values.put(CityProvider.CityConstants.LONGITUDE, city.getLon());
        values.put(CityProvider.CityConstants.PROVINCE, city.getProv());
        values.put(CityProvider.CityConstants.IS_LOCATION, city.isLocation() ? 1 : 0);
        values.put(CityProvider.CityConstants.ORDER_INDEX, city.getIndex());
        values.put(CityProvider.CityConstants.WEATHER_CODE, city.getCode());
        values.put(CityProvider.CityConstants.WEATHER_NAME, city.getCodeTxt());
        values.put(CityProvider.CityConstants.WEATHER_TEMPERATURE, city.getTmp());

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = contentResolver.insert(CityProvider.CITY_CONTENT_URI, values);
        return uri != null;
    }

    public static boolean insertAutoLocation(Context context) {
        ContentValues values = new ContentValues();
        values.put(CityProvider.CityConstants.IS_LOCATION, 1);
        return context.getContentResolver().insert(CityProvider.CITY_CONTENT_URI, values) != null;
    }

    public static City getCityByLocation(Context context) {
        City result = null;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(CityProvider.CITY_CONTENT_URI, null, CityProvider.CityConstants.IS_LOCATION + "=1", null,
                CityProvider.CityConstants.DEFAULT_SORT_ORDER);
        Log.d(TAG, "getCityFromCache cursor = " + cursor);
        if (cursor == null) return result;
        Log.d(TAG, "getCityFromCache cursor.size = " + cursor.getCount());
        if (cursor.getCount() < 1) {
            cursor.close();
            return result;
        }
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            String city = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.CITY));
            String country = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.COUNTRY));
            String areaId = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.AREA_ID));
            String lat = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.LATITUDE));
            String lon = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.LONGITUDE));
            String prov = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.PROVINCE));
            int isLocation = cursor.getInt(cursor.getColumnIndex(CityProvider.CityConstants.IS_LOCATION));
            String codeTxt = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.WEATHER_NAME));
            String code = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.WEATHER_CODE));
            String tmp = cursor.getString(cursor.getColumnIndex(CityProvider.CityConstants.WEATHER_TEMPERATURE));
            City c = new City(city, country, areaId, lat, lon, prov);
            c.setLocation(isLocation == 1);
            c.setCodeTxt(codeTxt);
            c.setCode(code);
            c.setTmp(tmp);
            result = c;
        }
        if (!cursor.isClosed()) cursor.close();
        return result;
    }
}

