package com.sena.lanraragi.ui.setting

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.doOnAttach
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.BaseActivity
import com.sena.lanraragi.R
import com.sena.lanraragi.databinding.ActivitySettingBinding
import com.sena.lanraragi.utils.DataStoreHelper
import com.sena.lanraragi.utils.FileUtils


const val INTENT_KEY_OPERATE = "operate"
const val INTENT_OPERATE_VALUE = "openHostPop"

class SettingActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingBinding

    private lateinit var serverHostCustomPop: SettingInputPopup
    private lateinit var serverHostPop: BasePopupView
    private lateinit var serverKeyCustomPop: SettingInputPopup
    private lateinit var serverKeyPop: BasePopupView

    private lateinit var commonAppThemeCustomPop: SettingSelectPopup
    private lateinit var commonAppThemePop: BasePopupView
    private lateinit var commonViewMethodCustomPop: SettingSelectPopup
    private lateinit var commonViewMethodPop: BasePopupView

    private lateinit var readMergeMethodCustomPop: SettingSelectPopup
    private lateinit var readMergeMethodPop: BasePopupView
    private lateinit var readScaleMethodCustomPop: SettingSelectPopup
    private lateinit var readScaleMethodPop: BasePopupView
    private lateinit var readTimeCustomPop: SettingInputPopup
    private lateinit var readTimePop: BasePopupView

    private lateinit var searchDelayCustomPop: SettingInputPopup
    private lateinit var searchDelayPop: BasePopupView

    private lateinit var randomCountCustomPop: SettingInputPopup
    private lateinit var randomCountPop: BasePopupView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setAppBarText(getString(R.string.setting_toolbar_title), null)
        setNavigation(R.drawable.ic_arrow_back_24) {
            finish()
        }

        initPopup()
        initView()
    }

    override fun onResume() {
        super.onResume()
        val operate = intent.getStringExtra(INTENT_KEY_OPERATE)
        if (operate == INTENT_OPERATE_VALUE) {
            serverHostPop.doOnAttach {// 必须在onAttach时, 之前onCreate还未执行, 变量未初始化
                serverHostCustomPop.setInputContent(AppConfig.serverHost)
            }
            serverHostPop.show()
        }
    }

    private fun initView() {
        initServer()
        initCache()
        initCommon()
        initReader()
        initSearch()
        initRandom()
        initSource()
        initDebug()
    }

    private fun initPopup() {
        initServerPop()
        initCommonPop()
        initReadPop()
        initSearchPop()
        initRandomPop()
    }


    private fun initServer() {
        val host = AppConfig.serverHost
        binding.server.hostText.apply {
            visibility = if (host.isEmpty()) View.GONE else View.VISIBLE
            text = host
        }
        binding.server.hostLayout.setOnClickListener {
            serverHostPop.doOnAttach {// 必须在onAttach时, 之前onCreate还未执行, 变量未初始化
                serverHostCustomPop.setInputContent(AppConfig.serverHost)
            }
            serverHostPop.show()
        }
        val key = AppConfig.serverSecretKey
        binding.server.keyText.apply {
            visibility = if (host.isEmpty()) View.GONE else View.VISIBLE
            text = key
        }
        binding.server.keyLayout.setOnClickListener {
            serverKeyPop.doOnAttach {// 必须在onAttach时, 之前onCreate还未执行, 变量未初始化
                serverKeyCustomPop.setInputContent(AppConfig.serverSecretKey)
            }
            serverKeyPop.show()
        }
    }

    private fun initCache() {
        val cacheSize = FileUtils.getCacheSize(this)
        binding.cache.cacheText.text = String.format(getString(R.string.setting_cache_clear_subtitle), cacheSize)
        binding.cache.clearCacheLayout.setOnClickListener {
            FileUtils.clearAllCache(this)
            binding.cache.cacheText.text = String.format(getString(R.string.setting_cache_clear_subtitle), 0)
        }
    }

    private fun initCommon() {
        val enableScrollRefresh = AppConfig.enableScrollRefresh
        binding.common.refreshButton.isChecked = enableScrollRefresh
        binding.common.scrollRefreshLayout.setOnClickListener {
            val curStatus = binding.common.refreshButton.isChecked
            val finStatus = !curStatus
            binding.common.refreshButton.isChecked = finStatus
            AppConfig.enableScrollRefresh = finStatus
            DataStoreHelper.updateValue(this, DataStoreHelper.KEY.COMMON_SCROLL_REFRESH, finStatus)
        }
        val theme = AppConfig.theme
        binding.common.themeText.text = theme.ifBlank { getString(R.string.setting_common_apptheme_select_1) }
        binding.common.themeLayout.setOnClickListener {
            commonAppThemePop.doOnAttach {
                commonAppThemeCustomPop.updateSelected(AppConfig.theme)
            }
            commonAppThemePop.show()
        }
        val viewMethod = AppConfig.viewMethod
        binding.common.viewMethodText.text = viewMethod.ifBlank { getString(R.string.setting_common_view_method_select_1) }
        binding.common.viewMethodLayout.setOnClickListener {
            commonViewMethodPop.doOnAttach {
                commonViewMethodCustomPop.updateSelected(AppConfig.viewMethod)
            }
            commonViewMethodPop.show()
        }
        binding.common.clearPreCacheLayout.setOnClickListener {
            Toast.makeText(this, "清理缓存...", Toast.LENGTH_SHORT).show()
            FileUtils.clearAllCache(this)
        }
    }

    private fun initReader() {
        val enableRtl = AppConfig.enableRtl
        binding.reader.rtlButton.isChecked = enableRtl
        binding.reader.rtlLayout.setOnClickListener {
            val curStatus = binding.reader.rtlButton.isChecked
            val finStatus = !curStatus
            binding.reader.rtlButton.isChecked = finStatus
            AppConfig.enableRtl = finStatus
            DataStoreHelper.updateValue(this, DataStoreHelper.KEY.READ_RTL, finStatus)
        }
        val enableVoice = AppConfig.enableVoice
        binding.reader.voiceButton.isChecked = enableVoice
        binding.reader.voiceLayout.setOnClickListener {
            val curStatus = binding.reader.voiceButton.isChecked
            val finStatus = !curStatus
            binding.reader.voiceButton.isChecked = finStatus
            AppConfig.enableVoice = finStatus
            DataStoreHelper.updateValue(this, DataStoreHelper.KEY.READ_VOICE, finStatus)
        }
        val enableMerge = AppConfig.enableMerge
        binding.reader.mergerButton.isChecked = enableMerge
        binding.reader.mergeLayout.setOnClickListener {
            val curStatus = binding.reader.mergerButton.isChecked
            val finStatus = !curStatus
            binding.reader.mergerButton.isChecked = finStatus
            AppConfig.enableMerge = finStatus
            DataStoreHelper.updateValue(this, DataStoreHelper.KEY.READ_MERGE, finStatus)
            changeViewStatus(binding.reader.reverseMergeLayout, finStatus)
            changeViewStatus(binding.reader.mergeMethodLayout, finStatus)
        }
        val enableReverseMerge = AppConfig.enableReverseMerge
        binding.reader.reverseMergerButton.isChecked = enableReverseMerge
        binding.reader.reverseMergeLayout.setOnClickListener {
            val curStatus = binding.reader.reverseMergerButton.isChecked
            val finStatus = !curStatus
            binding.reader.reverseMergerButton.isChecked = finStatus
            AppConfig.enableReverseMerge = finStatus
            DataStoreHelper.updateValue(this, DataStoreHelper.KEY.READ_REVERSE_MERGE, finStatus)
        }
        val mergeMethod = AppConfig.mergeMethod
        binding.reader.mergerMethodText.text = mergeMethod.ifBlank { getString(R.string.setting_read_merge_method_select_1) }
        binding.reader.mergeMethodLayout.setOnClickListener {
            readMergeMethodPop.doOnAttach {
                readMergeMethodCustomPop.updateSelected(AppConfig.mergeMethod)
            }
            readMergeMethodPop.show()
        }
        changeViewStatus(binding.reader.reverseMergeLayout, enableMerge)
        changeViewStatus(binding.reader.mergeMethodLayout, enableMerge)
        val scaleMethod = AppConfig.scaleMethod
        binding.reader.scaleText.text = scaleMethod.ifBlank { getString(R.string.setting_read_scale_method_select_1) }
        binding.reader.scaleLayout.setOnClickListener {
            readScaleMethodPop.doOnAttach {
                readScaleMethodCustomPop.updateSelected(AppConfig.scaleMethod)
            }
            readScaleMethodPop.show()
        }
        val screenOvertime = AppConfig.screenOvertime
        binding.reader.overtimeText.text = String.format(getString(R.string.setting_read_overtime_subtitle), screenOvertime)
        binding.reader.overtimeLayout.setOnClickListener {
            readTimePop.doOnAttach {// 必须在onAttach时, 之前onCreate还未执行, 变量未初始化
                readTimeCustomPop.setInputContent(AppConfig.screenOvertime.toString())
            }
            readTimePop.show()
        }

    }

    private fun initSearch() {
        val enableLocal = AppConfig.enableLocalSearch
        binding.search.localSearchButton.isChecked = enableLocal
        binding.search.localSearchLayout.setOnClickListener {
            val curStatus = binding.search.localSearchButton.isChecked
            val finStatus = !curStatus
            binding.search.localSearchButton.isChecked = finStatus
            AppConfig.enableLocalSearch = finStatus
            DataStoreHelper.updateValue(this, DataStoreHelper.KEY.SEARCH_LOCAL, finStatus)
        }
        val delay = AppConfig.searchDelay
        binding.search.searchDelayText.text = String.format(getString(R.string.setting_search_delay_subtitle), delay)
        binding.search.searchDelayLayout.setOnClickListener {
            searchDelayPop.doOnAttach {// 必须在onAttach时, 之前onCreate还未执行, 变量未初始化
                searchDelayCustomPop.setInputContent(AppConfig.searchDelay.toString())
            }
            searchDelayPop.show()
        }
    }

    private fun initRandom() {
        val randomCount = AppConfig.randomCount
        binding.random.randomCountText.text = String.format(getString(R.string.setting_random_count_subtitle), randomCount)
        binding.random.randomCountLayout.setOnClickListener {
            randomCountPop.doOnAttach {// 必须在onAttach时, 之前onCreate还未执行, 变量未初始化
                randomCountCustomPop.setInputContent(AppConfig.randomCount.toString())
            }
            randomCountPop.show()
        }
    }

    private fun initSource() {
        binding.source.licensesLayout.setOnClickListener {
            // TODO: 跳转activity
        }
        binding.source.gplv3Layout.setOnClickListener {
            // TODO: 跳转activity
        }
        binding.source.githubLayout.setOnClickListener {
            // TODO: 跳转activity
        }
    }

    private fun initDebug() {
        val enableDetail = AppConfig.enableShowDetail
        binding.debug.detailButton.isChecked = enableDetail
        binding.debug.detailLayout.setOnClickListener {
            val curStatus = binding.debug.detailButton.isChecked
            val finStatus = !curStatus
            binding.debug.detailButton.isChecked = finStatus
            AppConfig.enableShowDetail = finStatus
            DataStoreHelper.updateValue(this, DataStoreHelper.KEY.DEBUG_DETAIL, finStatus)
        }
        val enableCrash = AppConfig.enableCrashInfo
        binding.debug.crashButton.isChecked = enableCrash
        binding.debug.crashLayout.setOnClickListener {
            val curStatus = binding.debug.crashButton.isChecked
            val finStatus = !curStatus
            binding.debug.crashButton.isChecked = finStatus
            AppConfig.enableCrashInfo = finStatus
            DataStoreHelper.updateValue(this, DataStoreHelper.KEY.DEBUG_CRASH, finStatus)
        }
    }

    private fun initServerPop() {
        serverHostCustomPop = SettingInputPopup(this, getString(R.string.setting_server_host_title))
        serverHostCustomPop.setOnConfirmClickListener { t ->
            var finalText = t
            if (!t.startsWith("http://") && !t.startsWith("https://")) {
                finalText = "http://$t"
            }
            binding.server.hostText.apply {
                visibility = if (t.isBlank()) View.GONE else View.VISIBLE
                text = finalText
            }
            AppConfig.serverHost = finalText
            DataStoreHelper.updateValue(this@SettingActivity, DataStoreHelper.KEY.SERVER_HOST, finalText)
        }
        serverHostPop = XPopup.Builder(this)
            .autoFocusEditText(true)
            .autoOpenSoftInput(true)
            .moveUpToKeyboard(true)
            .asCustom(serverHostCustomPop)

        serverKeyCustomPop = SettingInputPopup(this, getString(R.string.setting_server_key_title))
        serverKeyCustomPop.setOnConfirmClickListener { t ->
            binding.server.keyText.apply {
                visibility = if (t.isBlank()) View.GONE else View.VISIBLE
                text = t
            }
            AppConfig.serverSecretKey = t
            DataStoreHelper.updateValue(this@SettingActivity, DataStoreHelper.KEY.SERVER_SECRET_KEY, t)
        }
        serverKeyPop = XPopup.Builder(this)
            .autoFocusEditText(true)
            .autoOpenSoftInput(true)
            .moveUpToKeyboard(true)
            .asCustom(serverKeyCustomPop)
    }

    private fun initCommonPop() {
        val appThemeList = arrayListOf(
            getString(R.string.setting_common_apptheme_select_1),
            getString(R.string.setting_common_apptheme_select_2),
            getString(R.string.setting_common_apptheme_select_3)
        )
        commonAppThemeCustomPop = SettingSelectPopup(this, getString(R.string.setting_common_apptheme_title), appThemeList)
        commonAppThemeCustomPop.setOnSelectedListener { _, s ->
            binding.common.themeText.text = s
            AppConfig.theme = s
            DataStoreHelper.updateValue(this@SettingActivity, DataStoreHelper.KEY.COMMON_THEME, s)
        }
        commonAppThemePop = XPopup.Builder(this)
            .asCustom(commonAppThemeCustomPop)

        val viewMethodList = arrayListOf(
            getString(R.string.setting_common_view_method_select_1),
            getString(R.string.setting_common_view_method_select_2)
        )
        commonViewMethodCustomPop = SettingSelectPopup(this, getString(R.string.setting_common_view_method_title), viewMethodList)
        commonViewMethodCustomPop.setOnSelectedListener { _, s ->
            binding.common.viewMethodText.text = s
            AppConfig.viewMethod = s
            DataStoreHelper.updateValue(this@SettingActivity, DataStoreHelper.KEY.COMMON_VIEW_METHOD, s)
        }
        commonViewMethodPop = XPopup.Builder(this)
            .asCustom(commonViewMethodCustomPop)
    }

    private fun initReadPop() {
        val mergeMethodList = arrayListOf(
            getString(R.string.setting_read_merge_method_select_1),
            getString(R.string.setting_read_merge_method_select_2)
        )
        readMergeMethodCustomPop = SettingSelectPopup(this, getString(R.string.setting_read_merge_method_title), mergeMethodList)
        readMergeMethodCustomPop.setOnSelectedListener { _, s ->
            binding.reader.mergerMethodText.text = s
            AppConfig.mergeMethod = s
            DataStoreHelper.updateValue(this@SettingActivity, DataStoreHelper.KEY.READ_MERGE_METHOD, s)
        }
        readMergeMethodPop = XPopup.Builder(this)
            .asCustom(readMergeMethodCustomPop)

        val scaleMethodList = arrayListOf(
            getString(R.string.setting_read_scale_method_select_1),
            getString(R.string.setting_read_scale_method_select_2),
            getString(R.string.setting_read_scale_method_select_3),
            getString(R.string.setting_read_scale_method_select_4),
        )
        readScaleMethodCustomPop = SettingSelectPopup(this, getString(R.string.setting_read_scale_method_title), scaleMethodList)
        readScaleMethodCustomPop.setOnSelectedListener { _, s ->
            binding.reader.scaleText.text = s
            AppConfig.scaleMethod = s
            DataStoreHelper.updateValue(this@SettingActivity, DataStoreHelper.KEY.READ_SCALE_METHOD, s)
        }
        readScaleMethodPop = XPopup.Builder(this)
            .asCustom(readScaleMethodCustomPop)

        readTimeCustomPop = SettingInputPopup(this, getString(R.string.setting_read_overtime_title), true)
        readTimeCustomPop.setOnConfirmClickListener { t ->
            val number = t.toIntOrNull() ?: return@setOnConfirmClickListener
            binding.reader.overtimeText.text = String.format(getString(R.string.setting_read_overtime_subtitle), number)
            AppConfig.screenOvertime = number
            DataStoreHelper.updateValue(this@SettingActivity, DataStoreHelper.KEY.READ_SCREEN_OVER_TIME, number)
        }
        readTimePop = XPopup.Builder(this)
            .autoFocusEditText(true)
            .autoOpenSoftInput(true)
            .moveUpToKeyboard(true)
            .asCustom(readTimeCustomPop)
    }

    private fun initSearchPop() {
        searchDelayCustomPop = SettingInputPopup(this, getString(R.string.setting_search_delay_title), true)
        searchDelayCustomPop.setOnConfirmClickListener { t ->
            val number = t.toIntOrNull() ?: return@setOnConfirmClickListener
            binding.search.searchDelayText.text = String.format(getString(R.string.setting_search_delay_subtitle), number)
            AppConfig.searchDelay = number
            DataStoreHelper.updateValue(this@SettingActivity, DataStoreHelper.KEY.SEARCH_DELAY, number)
        }
        searchDelayPop = XPopup.Builder(this)
            .autoFocusEditText(true)
            .autoOpenSoftInput(true)
            .moveUpToKeyboard(true)
            .asCustom(searchDelayCustomPop)
    }

    private fun initRandomPop() {
        randomCountCustomPop = SettingInputPopup(this, getString(R.string.setting_random_count_title), true)
        randomCountCustomPop.setOnConfirmClickListener { t ->
            var number = t.toIntOrNull() ?: return@setOnConfirmClickListener
            if (number <= 0) number = 1
            binding.random.randomCountText.text = String.format(getString(R.string.setting_random_count_subtitle), number)
            AppConfig.randomCount = number
            DataStoreHelper.updateValue(this@SettingActivity, DataStoreHelper.KEY.RANDOM_COUNT, number)
        }
        randomCountPop = XPopup.Builder(this)
            .autoFocusEditText(true)
            .autoOpenSoftInput(true)
            .moveUpToKeyboard(true)
            .asCustom(randomCountCustomPop)
    }


    private fun changeViewStatus(view: View, b: Boolean) {
        val mAlpha = if (b) 1f else 0.5f
        view.apply {
            this.isEnabled = b
            this.alpha = mAlpha
        }
    }

}