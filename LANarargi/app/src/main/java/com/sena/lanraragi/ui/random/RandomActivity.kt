package com.sena.lanraragi.ui.random

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.doOnAttach
import androidx.lifecycle.lifecycleScope
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.BaseArchiveListActivity
import com.sena.lanraragi.R
import com.sena.lanraragi.databinding.ActivityRandomBinding
import com.sena.lanraragi.ui.MainActivity
import com.sena.lanraragi.ui.setting.SettingInputPopup
import com.sena.lanraragi.utils.DataStoreHelper
import com.sena.lanraragi.utils.INTENT_KEY_QUERY
import com.sena.lanraragi.utils.NewHttpHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RandomActivity : BaseArchiveListActivity(R.menu.menu_random) {

    private lateinit var binding: ActivityRandomBinding

    private lateinit var randomCountCustomPop: SettingInputPopup
    private lateinit var randomCountPop: BasePopupView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRandomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setAppBarText(getString(R.string.random_toolbar_title), null)
        setNavigation(R.drawable.ic_arrow_back_24) {
            onBackPressedDispatcher.onBackPressed()
        }

        initPopup()
        initData()
    }

    override fun onTagSelected(s: String) {
        super.onTagSelected(s)
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(INTENT_KEY_QUERY, s)
        startActivity(intent)
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
            mAdapter.submitList(result) {
                binding.recyclerView.layoutManager?.scrollToPosition(0)
            }
            setAppBarText(getString(R.string.random_toolbar_title), String.format(getString(R.string.random_toolbar_subtitle), result.size))

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
                mRecyclerView?.layoutManager?.scrollToPosition(0)
            }

            R.id.gotoBottom -> {
                mRecyclerView?.layoutManager?.scrollToPosition(mAdapter.items.size - 1)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}