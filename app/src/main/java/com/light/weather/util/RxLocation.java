package com.light.weather.util;

import android.app.Application;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.light.weather.bean.City;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

/**
 * Created by liweiping on 18-1-23.
 */

public class RxLocation extends Observable<City> {
    private static final String TAG = "RxLocation";
    private static volatile RxLocation sInstance;
    private Application mApplication;

    private RxLocation(Application application) {
        mApplication = application;
    }

    public static RxLocation getInstance(Application application) {
        if (sInstance == null) {
            synchronized (RxLocation.class) {
                if (sInstance == null) {
                    sInstance = new RxLocation(application);
                }
            }
        }
        return sInstance;
    }

    @Override
    protected void subscribeActual(Observer<? super City> observer) {
        final AMapLocationClient locationClient = new AMapLocationClient(mApplication);
        locationClient.setLocationOption(getAMapLocationClientOption());

        final Listener listener = new Listener(locationClient, observer);
        observer.onSubscribe(listener);
        locationClient.setLocationListener(listener);
        locationClient.startLocation();
        Log.d(TAG, "subscribeActual: startLocation...");
    }

    private AMapLocationClientOption getAMapLocationClientOption() {
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        option.setOnceLocationLatest(true);
        option.setHttpTimeOut(20000L);
        return option;
    }

    private static final class Listener extends MainThreadDisposable implements AMapLocationListener {
        private static final int MAX_NUM = 3;
        private final AMapLocationClient mMapLocationClient;
        private final Observer<? super City> mObserver;
        private int mLocationCount;

        Listener(AMapLocationClient locationHelper, Observer<? super City> observer) {
            this.mMapLocationClient = locationHelper;
            this.mObserver = observer;
            this.mLocationCount = 1;
        }

        @Override
        public void onLocationChanged(AMapLocation location) {
            Log.d(TAG, "onLocationChanged: isDisposed = " + isDisposed() + ", location = " + location);
            if (!isDisposed()) {
                if (location.getErrorCode() != 0) {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e(TAG, "onLocationChanged Error!!! ErrCode:" + location.getErrorCode()
                            + ", errInfo:" + location.getErrorInfo());
                    mLocationCount++;
                    if (mLocationCount > MAX_NUM) {
                        mObserver.onError(new Throwable(location.getAdCode() + ": " + location.getErrorInfo()));
                    }
                    return;
                }
                City city = new City(location.getDistrict(), location.getCountry(),
                        String.format("%s,%s", location.getLongitude(), location.getLatitude()),
                        String.valueOf(location.getLatitude()),
                        String.valueOf(location.getLongitude()), location.getProvince());
                city.setLocation(true);
                Log.d(TAG, "onLocationChanged: city = " + city + ", try times = " + mLocationCount);
                mObserver.onNext(city);
                mObserver.onComplete();
                dispose();
            }
        }

        @Override
        protected void onDispose() {
            Log.d(TAG, "onDispose: stopLocation...");
            mMapLocationClient.unRegisterLocationListener(this);
            mMapLocationClient.stopLocation();
            mMapLocationClient.onDestroy();
        }

    }
}
