package com.light.weather.util;

import android.app.Application;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

/**
 * Created by liweiping on 18-1-23.
 */

public class RxLocation extends Observable<String> {
    private static final String TAG = "RxLocation";
    private AMapLocationClient mLocationClient;

    public RxLocation(Application application) {
        mLocationClient = new AMapLocationClient(application);
        mLocationClient.setLocationOption(getAMapLocationClientOption());
    }

    @Override
    protected void subscribeActual(Observer<? super String> observer) {
        Listener listener = new Listener(mLocationClient, observer);
        observer.onSubscribe(listener);
        mLocationClient.setLocationListener(listener);
        mLocationClient.startLocation();
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
        private final Observer<? super String> mObserver;
        private int mLocationCount;

        Listener(AMapLocationClient locationHelper, Observer<? super String> observer) {
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
                final double longitude = location.getLongitude();
                final double latitude = location.getLatitude();
                String coordinate = latitude + "," + longitude;
                Log.d(TAG, "onLocationChanged: coordinate = " + coordinate + ", try times = " + mLocationCount);
                mObserver.onNext(coordinate);
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
