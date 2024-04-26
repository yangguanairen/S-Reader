package com.sena.lanraragi.ui.setting

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sena.lanraragi.BaseActivity
import com.sena.lanraragi.R
import com.sena.lanraragi.databinding.ActivityMyLicenseBinding
import com.sena.lanraragi.utils.getOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder

class MyLicenseActivity : BaseActivity() {

    private lateinit var binding: ActivityMyLicenseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyLicenseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        setNavigation(R.drawable.ic_arrow_back_24) {
            onBackPressedDispatcher.onBackPressed()
        }
        setAppBarText(getString(R.string.setting_source_gplv3_title), null)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val s = withContext(Dispatchers.IO) {
                    val sb = StringBuilder()
                    getOrNull {
                        BufferedReader(InputStreamReader(assets.open("gplv3License.txt"))).readLines().forEach {
                            sb.appendLine(it)
                        }
                    }
                    sb.toString()
                }
                binding.content.text = s
            }
        }

    }
}