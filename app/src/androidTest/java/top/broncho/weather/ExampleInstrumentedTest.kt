package top.broncho.weather

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import top.broncho.weather.api.HeWeatherApi
import top.broncho.weather.util.provideRetrofit
import retrofit2.create

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest : AnkoLogger {
    private lateinit var api: HeWeatherApi

    private val context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }
    @Before
    fun init() {
        info { "before test..." }
        api = context.provideRetrofit().create()
    }

    @Test
    fun searchCity() {
        info { "searchCity E..." }
        val response = runBlocking {
            api.searchCity(location = "shenzhen")
        }
        info { "searchCity X... $response" }
        assertTrue(!response.heWeather6.isNullOrEmpty())
        assertEquals(response.heWeather6[0].status, "ok")
    }

    @Test
    fun getWeather() {
        info { "getWeather E..." }
        val response = runBlocking {
            api.getWeather(location = "shenzhen")
        }
        info { "getWeather X... $response" }
        assertTrue(!response.heWeather6.isNullOrEmpty())
        assertEquals(response.heWeather6[0].status, "ok")
    }
}