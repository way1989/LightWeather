package com.light.weather.di;

import android.app.Application;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.light.weather.BuildConfig;
import com.light.weather.data.IRepositoryManager;
import com.light.weather.data.RepositoryManager;
import com.light.weather.util.AppConstant;
import com.light.weather.util.DeviceUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by android on 16-11-25.
 */
@Module(includes = ViewModelModule.class)
final class AppModule {

    @Provides
    @Singleton
    IRepositoryManager provideRepositoryManager(RepositoryManager repositoryManager) {
        return repositoryManager;
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.PROTECTED)//忽略protected字段
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Cache cache, Interceptor interceptor) {
        return new OkHttpClient.Builder()
                .readTimeout(AppConstant.READ_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(AppConstant.CONN_TIMEOUT, TimeUnit.SECONDS)
                .cache(cache)
                .addInterceptor(interceptor)
                .build();//初始化一个client,不然retrofit会自己默认添加一个
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(OkHttpClient client, Gson gson) {
        return new Retrofit.Builder()
                .client(client)//添加一个client,不然retrofit会自己默认添加一个
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BuildConfig.HEWEATHER_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides
    @Singleton
    Interceptor provideInterceptor(final Application application) {
        return new Interceptor() {
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                Request request = chain.request();
                if (!DeviceUtil.hasInternet(application)) {
                    Log.d("Interceptor", "intercept: not internet...");
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                }
                Response response = chain.proceed(request);
                if (DeviceUtil.isWifiOpen(application)) {
                    Log.d("Interceptor", "intercept: is wifi...");
                    final int maxAge = 0;
                    // 有wifi时设置缓存超时时间0个小时
                    response.newBuilder()
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .removeHeader("Pragma")// 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                            .build();
                } else if (DeviceUtil.hasInternet(application)) {
                    Log.d("Interceptor", "intercept: is 4G/3G/2G...");
                    //数据流量时设置超时时间为5分钟
                    final int maxStale = 60 * 5;
                    response.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .removeHeader("Pragma")
                            .build();
                } else {
                    // 无网络时设置超时为1周
                    final int maxStale = 60 * 60 * 24 * 7;
                    response.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .removeHeader("Pragma")
                            .build();
                }
                return response;
            }
        };
    }

    @Provides
    @Singleton
    Cache provideCache(File cacheDir) {
        return new Cache(new File(cacheDir, "weather"), 1024 * 1024 * 50);
    }

    @Provides
    @Singleton
    File provideCacheDir(Application application) {
        File cacheDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cacheDir = application.getExternalCacheDir();
        }
        if (cacheDir == null) {
            cacheDir = application.getCacheDir();
        }
        return cacheDir;
    }

}
