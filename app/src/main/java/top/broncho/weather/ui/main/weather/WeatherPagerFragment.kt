package top.broncho.weather.ui.main.weather

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import top.broncho.weather.R
import top.broncho.weather.databinding.FragmentWeatherPagerBinding
import top.broncho.weather.ui.main.BaseFragment

class WeatherPagerFragment : BaseFragment<FragmentWeatherPagerBinding>() {

    override fun getLayoutId() = R.layout.fragment_weather_pager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPager.adapter = WeatherPagerAdapter(childFragmentManager)
    }

    private class WeatherPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(
        fragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
        override fun getItem(position: Int): Fragment {
            return WeatherFragment.newInstance();
        }

        override fun getCount(): Int {
            return 3;
        }

    }
}