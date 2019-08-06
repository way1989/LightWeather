package com.light.weather;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.billy.android.swipe.SmartSwipeBack;
import com.facebook.stetho.Stetho;
import com.light.weather.di.AppInjector;
import com.light.weather.ui.main.MainActivity;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.IOException;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.github.skyhacker2.sqliteonweb.SQLiteOnWeb;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;


/**
 * Created by way on 16/6/10.
 */
public class App extends Application implements HasActivityInjector {
    private static final String TAG = "App";
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), BuildConfig.APP_ID, BuildConfig.DEBUG);
        LeakCanary.install(this);
        SQLiteOnWeb.init(this).start();
        AppInjector.init(this);

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
//            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                    .detectAll().penaltyDeath().build());
//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                    .detectAll().penaltyDeath().build());
        }

        setRxJavaErrorHandler();
        //仿微信带联动效果的透明侧滑返回
        SmartSwipeBack.activitySlidingBack(this, activity -> !(activity instanceof MainActivity));
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }

    private void setRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if (e instanceof IOException) {
                // fine, irrelevant network problem or API that throws on cancellation
                //return;
            }
            if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                //return;
            }
            if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                // that's likely a bug in the application
                //return;
            }
            if (e instanceof IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                //return;
            }
            Log.w(TAG, "Undeliverable exception received, not sure what to do ", e);
        });
    }

}
