package com.light.weather.util;


import android.util.Log;

import java.util.concurrent.Executors;

import io.reactivex.FlowableTransformer;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * RxJava调度管理 Created by way on 2017.08.30
 */
public class RxSchedulers {
    private static final String TAG = "RxSchedulers";
    public static final Scheduler SCHEDULER
            = Schedulers.from(Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("Scheduler");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.setUncaughtExceptionHandler((t, e) ->
                Log.e(TAG, "SCHEDULER uncaughtException: t = " + t, e));
        return thread;
    }));

    public static <T> ObservableTransformer<T, T> io_main() {
        return upstream -> upstream.subscribeOn(RxSchedulers.SCHEDULER)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> FlowableTransformer<T, T> io_main_flow() {
        return upstream -> upstream.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(SCHEDULER);
    }

    public static <T> SingleTransformer<T, T> io_main_single() {
        return upstream -> upstream.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(SCHEDULER);
    }

    public static <T> FlowableTransformer<T, T> all_io_flow() {
        return upstream -> upstream.observeOn(SCHEDULER).subscribeOn(SCHEDULER);
    }

    public static <T> SingleTransformer<T, T> all_io_single() {
        return upstream -> upstream.observeOn(SCHEDULER).subscribeOn(SCHEDULER);
    }
}
