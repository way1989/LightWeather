package top.broncho.weather.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import org.jetbrains.anko.AnkoLogger

abstract class BaseFragment<T : ViewDataBinding> : Fragment(), AnkoLogger {
    protected lateinit var binding: T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            getLayoutId(),
            container,
            false
        )
        binding.lifecycleOwner = this
        return binding.root
    }

    @LayoutRes
    abstract fun getLayoutId(): Int

}