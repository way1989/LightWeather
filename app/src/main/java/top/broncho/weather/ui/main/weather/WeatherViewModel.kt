package top.broncho.weather.ui.main.weather

import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import retrofit2.Retrofit

class WeatherViewModel @ViewModelInject constructor(val app: Application, val retrofit: Retrofit) :
    AndroidViewModel(app) {
}