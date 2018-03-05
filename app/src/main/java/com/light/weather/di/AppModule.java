package com.light.weather.di;

import android.app.Application;
import android.os.Environment;

import com.light.weather.BuildConfig;
import com.light.weather.data.IRepositoryManager;
import com.light.weather.data.RepositoryManager;
import com.light.weather.util.AppConstant;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.rx_cache2.internal.RxCache;
import io.victoralbertos.jolyglot.GsonSpeaker;
import okhttp3.OkHttpClient;
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
    OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder().readTimeout(AppConstant.READ_TIMEOUT, TimeUnit.MINUTES)
                .connectTimeout(AppConstant.CONN_TIMEOUT, TimeUnit.SECONDS).build();//初始化一个client,不然retrofit会自己默认添加一个
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
    File getCacheDir(Application application) {
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

    @Provides
    @Singleton
    RxCache provideRxCache(File cacheDir) {
        return new RxCache.Builder()
                .persistence(cacheDir, new GsonSpeaker());
    }
}
