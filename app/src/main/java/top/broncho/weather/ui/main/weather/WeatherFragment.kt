package top.broncho.weather.ui.main.weather

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import top.broncho.weather.R
import top.broncho.weather.databinding.WeatherFragmentBinding
import top.broncho.weather.ui.main.BaseFragment
import top.broncho.weather.ui.main.MainViewModel

@AndroidEntryPoint
class WeatherFragment : BaseFragment<WeatherFragmentBinding>() {

    companion object {
        fun newInstance() = WeatherFragment()
    }

    private val viewModel: MainViewModel by viewModels()

    override fun getLayoutId() = R.layout.weather_fragment

}