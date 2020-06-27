package top.broncho.weather.ui.main

import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import retrofit2.Retrofit

class MainViewModel @ViewModelInject constructor(val app: Application, val retrofit: Retrofit) :
    AndroidViewModel(app) {
    fun getContext() = app
}