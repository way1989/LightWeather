package com.light.weather.data;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import retrofit2.Retrofit;

@Singleton
public class RepositoryManager implements IRepositoryManager {
    private final Map<String, Object> mRetrofitServiceCache = new HashMap<>();
    private final Map<String, Object> mRoomDatabaseCache = new HashMap<>();
    private Application mApplication;
    private Lazy<Retrofit> mRetrofit;

    @Inject
    public RepositoryManager(Application application, Lazy<Retrofit> retrofit) {
        mApplication = application;
        this.mRetrofit = retrofit;
    }

    /**
     * 根据传入的 Class 获取对应的 Retrofit service
     *
     * @param service
     * @param <T>
     * @return
     */
    @Override
    public <T> T obtainRetrofitService(Class<T> service) {
        T retrofitService;
        synchronized (mRetrofitServiceCache) {
            retrofitService = (T) mRetrofitServiceCache.get(service.getName());
            if (retrofitService == null) {
                retrofitService = mRetrofit.get().create(service);
                mRetrofitServiceCache.put(service.getName(), retrofitService);
            }
        }
        return retrofitService;
    }

}
