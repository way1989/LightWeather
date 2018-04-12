package com.light.weather.data;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;

import com.light.weather.util.Preconditions;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import retrofit2.Retrofit;

@Singleton
public class RepositoryManager implements IRepositoryManager {
    private Application mApplication;
    private final Map<String, Object> mRetrofitServiceCache = new HashMap<>();
    private final Map<String, Object> mRoomDatabaseCache = new HashMap<>();
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


    /**
     * 清理所有缓存
     */
    @Override
    public void clearAllCache() {
        //mRxCache.get().evictAll();
    }

    @Override
    public <DB extends RoomDatabase> DB obtainRoomDatabase(Class<DB> database, String dbName) {
        Preconditions.checkNotNull(mRoomDatabaseCache, "Cannot return null from a Cache.Factory#build(int) method");
        DB roomDatabase;
        synchronized (mRoomDatabaseCache) {
            roomDatabase = (DB) mRoomDatabaseCache.get(database.getName());
            if (roomDatabase == null) {
                RoomDatabase.Builder builder = Room.databaseBuilder(mApplication, database, dbName);
                //自定义 Room 配置
//                if (mRoomConfiguration != null) {
//                    mRoomConfiguration.configRoom(mApplication, builder);
//                }
                roomDatabase = (DB) builder.build();
                mRoomDatabaseCache.put(database.getName(), roomDatabase);
            }
        }
        return roomDatabase;
    }
}
