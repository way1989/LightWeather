package top.broncho.weather.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import top.broncho.weather.util.provideRetrofit
import top.broncho.weather.util.webSocketClient
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(app: Application): Retrofit = app.provideRetrofit()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = webSocketClient()
}