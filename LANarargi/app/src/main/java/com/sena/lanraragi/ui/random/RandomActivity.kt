package com.sena.lanraragi.ui.random

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.doOnAttach
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.BaseActivity
import com.sena.lanraragi.R
import com.sena.lanraragi.databinding.ActivityRandomBinding
import com.sena.lanraragi.ui.INTENT_KEY_ARCHIVE
import com.sena.lanraragi.ui.MainAdapter
import com.sena.lanraragi.ui.detail.DetailActivity
import com.sena.lanraragi.ui.setting.SettingInputPopup
import com.sena.lanraragi.utils.DataStoreHelper
import com.sena.lanraragi.utils.NewHttpHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RandomActivity : BaseActivity(R.menu.menu_random) {

    private lateinit var binding: ActivityRandomBinding
    private lateinit var adapter: MainAdapter

    private lateinit var randomCountCustomPop: SettingInputPopup
    private lateinit var randomCountPop: BasePopupView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRandomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setAppBarText(getString(R.string.random_toolbar_title), null)
        setNavigation(R.drawable.ic_arrow_back_24) {
            finish()
        }

        initPopup()
        initView()
    }

    override fun onResume() {
        super.onResume()
        initData()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.recyclerView.apply {
            var cPos = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (cPos < 0) cPos = 0
            layoutManager = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                GridLayoutManager(this@RandomActivity, 2)
            } else {
                LinearLayoutManager(this@RandomActivity)
            }
            layoutManager?.scrollToPosition(cPos)
        }
    }

    private fun initView() {
        binding.recyclerView.layoutManager = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager(this@RandomActivity, 2)
        } else {
            LinearLayoutManager(this)
        }
        adapter = MainAdapter()
        binding.recyclerView.adapter = adapter
        adapter.setOnItemClickListener { _, _, p ->
            val itemData = adapter.getItem(p)
            if (itemData != null) {
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra(INTENT_KEY_ARCHIVE, itemData)
                startActivity(intent)
            }
        }
    }

    private fun initPopup() {
        randomCountCustomPop = SettingInputPopup(this, getString(R.string.setting_random_count_title), true)
        randomCountCustomPop.setOnConfirmClickListener { t ->
            val number = t.toIntOrNull() ?: return@setOnConfirmClickListener
            AppConfig.randomCount = number
            DataStoreHelper.updateValue(this@RandomActivity, DataStoreHelper.KEY.RANDOM_COUNT, number)
            initData()
        }
        randomCountPop = XPopup.Builder(this)
            .autoFocusEditText(true)
            .autoOpenSoftInput(true)
            .moveUpToKeyboard(true)
            .asCustom(randomCountCustomPop)
    }

    private fun initData() {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                NewHttpHelper.getRandomArchive()
            }
            adapter.submitList(result)
            setAppBarText(getString(R.string.random_toolbar_title), String.format(getString(R.string.random_toolbar_subtitle), result.size))
            binding.recyclerView.layoutManager?.scrollToPosition(0)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> {
                initData()
            }

            R.id.changeRandomCount -> {
                randomCountPop.doOnAttach {// 必须在onAttach时, 之前onCreate还未执行, 变量未初始化
                    randomCountCustomPop.setInputContent(AppConfig.randomCount.toString())
                }
                randomCountPop.show()
            }

            R.id.gotoTop -> {
                binding.recyclerView.layoutManager?.scrollToPosition(0)
            }

            R.id.gotoBottom -> {
                binding.recyclerView.layoutManager?.scrollToPosition(adapter.items.size - 1)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}