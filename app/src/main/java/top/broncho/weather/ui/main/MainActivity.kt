package top.broncho.weather.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import top.broncho.weather.R

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AnkoLogger {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        info { "onCreate: viewModel=${viewModel.app}" }
    }
}