package top.broncho.weather.ui.main

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import org.jetbrains.anko.AnkoLogger

abstract class BaseActivity<T : ViewDataBinding> : AppCompatActivity(), AnkoLogger {
    protected lateinit var binding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDisplayHomeAsUpEnabled()) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        binding.lifecycleOwner = this
    }

    open fun isDisplayHomeAsUpEnabled() = true

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    @LayoutRes
    abstract fun getLayoutId(): Int

}