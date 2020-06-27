package top.broncho.weather.util

import android.content.Context
import android.net.ConnectivityManager
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor.Companion.invoke
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.warn
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*


private const val READ_TIMEOUT = 20L//读取超时时间,单位  秒
private const val WRITE_TIMEOUT = 20L//读取超时时间,单位  秒
private const val CONN_TIMEOUT = 20L//连接超时时间,单位  秒

private const val CACHE_DIR = "retrofit_manager_disk_cache"

fun Context.hasInternet(): Boolean {
    val cm =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val info = cm.activeNetworkInfo
    return info != null && info.isAvailable && info.isConnected
}

private fun Context.provideCacheDir(): File {
    return externalCacheDir.takeIf { it != null } ?: cacheDir
}

private fun Context.provideCache(): Cache {
    return Cache(File(provideCacheDir(), CACHE_DIR), 1024 * 1024 * 50)
}

private fun Context.provideOkHttpClient(): OkHttpClient {
    //打印请求log
    val logging = HttpLoggingInterceptor().apply {
        level =
                /*if (BuildConfig.DEBUG)*/ HttpLoggingInterceptor.Level.BODY
//            else HttpLoggingInterceptor.Level.NONE
    }
    return OkHttpClient.Builder()
        .addNetworkInterceptor(provideNetCacheInterceptor())
        .addInterceptor(provideOfflineCacheInterceptor())
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .connectTimeout(CONN_TIMEOUT, TimeUnit.SECONDS)
        .cache(provideCache())
        .addInterceptor(logging)
        .build()//初始化一个client,不然retrofit会自己默认添加一个
}

private fun Context.provideOfflineCacheInterceptor(): Interceptor {
    return invoke { chain ->
        var request = chain.request()
        if (!hasInternet()) {
            val offlineCacheTime = 3 //离线的时候的缓存的过期时间
            request = request.newBuilder()
                .cacheControl(
                    CacheControl
                        .Builder()
                        .maxStale(offlineCacheTime, TimeUnit.DAYS)
                        .onlyIfCached()
                        .build()
                )
                .build()
        }
        chain.proceed(request)
    }
}

private fun provideNetCacheInterceptor(): Interceptor {
    return invoke { chain ->
        val request = chain.request()
        val response = chain.proceed(request)
//        val onlineCacheTime = 60 * 60 * 24 //在线的时候的缓存过期时间，如果想要不缓存，直接时间设置为0
        val onlineCacheTime = 5 * 60 //在线的时候的缓存过期时间，如果想要不缓存，直接时间设置为0
        response.newBuilder()
            .removeHeader("Pragma")
            .removeHeader("Cache-Control")
            .header("Cache-Control", "public, max-age=$onlineCacheTime")
            .build()
    }
}

fun Context.provideRetrofit(): Retrofit {
    val client = provideOkHttpClient()
    return Retrofit.Builder().client(client)
        .baseUrl(BASE_URL)
        .callFactory(CallFactoryProxy(client))
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

fun webSocketClient() = OkHttpClient.Builder()
    .readTimeout(5, TimeUnit.SECONDS)//设置读取超时时间
    .writeTimeout(5, TimeUnit.SECONDS)//设置写的超时时间
    .connectTimeout(5, TimeUnit.SECONDS)//设置连接超时时间
    .sslSocketFactory(getSSLSocketFactory(), getX509TrustManager())
    .hostnameVerifier(getHostnameVerifier())
    //.pingInterval(40, TimeUnit.SECONDS)
    .build()

//获取HostnameVerifier
fun getHostnameVerifier(): HostnameVerifier = HostnameVerifier { _, _ -> true }

//获取这个SSLSocketFactory
fun getSSLSocketFactory(): SSLSocketFactory = try {
    val sslContext: SSLContext = SSLContext.getInstance("SSL")
    sslContext.init(null, getTrustManager(), SecureRandom())
    sslContext.socketFactory
} catch (e: Exception) {
    throw RuntimeException(e)
}

fun getTrustManager(): Array<TrustManager> = arrayOf(getX509TrustManager())

fun getX509TrustManager(): X509TrustManager = object : X509TrustManager {
    override fun checkClientTrusted(
        chain: Array<X509Certificate>,
        authType: String
    ) {
    }

    override fun checkServerTrusted(
        chain: Array<X509Certificate>,
        authType: String
    ) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }
}

const val DOMAIN_NAME = "Domain-Name"
const val BASE_URL = "http://broncho.top"

class CallFactoryProxy(private val delegate: Call.Factory) : Call.Factory, AnkoLogger {

    override fun newCall(request: Request): Call {
        val newBaseUrl = request.header(DOMAIN_NAME)
        if (newBaseUrl != null) {
            val oldUrl = request.url.toString()
            val newUrl = oldUrl.replace(BASE_URL.toRegex(), newBaseUrl)
            info { "newCall: oldUrl = $oldUrl, newUrl = $newUrl" }
            val newRequest = request.newBuilder().url(newUrl.toHttpUrl()).build()
            return delegate.newCall(newRequest)
        } else {
            warn { "newCall: return null when newBaseUrl is null!" }
        }
        return delegate.newCall(request)
    }

}
