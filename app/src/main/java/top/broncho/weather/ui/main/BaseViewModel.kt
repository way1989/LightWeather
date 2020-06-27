package top.broncho.weather.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn

abstract class BaseViewModel(val app: Application) : AndroidViewModel(app), AnkoLogger {

    fun launch(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch { block() }

    fun <T> launchNullable(
        block: suspend CoroutineScope.() -> T,
        success: (T?) -> Unit,
        error: (Throwable) -> Unit = {},
        complete: () -> Unit = {}
    ) = launch {
        try {
            val response = block()
            success(response)
        } catch (e: Throwable) {
            error(e)
            warn { "launchNullable: $block, exception=$e" }
        } finally {
            complete()
        }
    }

    fun <T> launchNotNull(
        block: suspend CoroutineScope.() -> T,
        success: (T) -> Unit,
        error: (Throwable) -> Unit = {},
        complete: () -> Unit = {}
    ) = launch {
        try {
            val response = block()
            success(response)
        } catch (e: Throwable) {
            error(e)
            warn { "launchNotNull: $block, exception=$e" }
        } finally {
            complete()
        }
    }
}


