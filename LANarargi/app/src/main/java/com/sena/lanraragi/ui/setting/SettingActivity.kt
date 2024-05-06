package com.sena.lanraragi.ui.setting

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.doOnAttach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.BaseActivity
import com.sena.lanraragi.R
import com.sena.lanraragi.databinding.ActivitySettingBinding
import com.sena.lanraragi.utils.AppLanguage
import com.sena.lanraragi.utils.AppTheme
import com.sena.lanraragi.utils.CardType
import com.sena.lanraragi.utils.DataStoreHelper
import com.sena.lanraragi.utils.FileUtils
import com.sena.lanraragi.utils.GlobalCrashUtils
import com.sena.lanraragi.utils.INTENT_KEY_ARCHIVE
import com.sena.lanraragi.utils.INTENT_KEY_OPERATE
import com.sena.lanraragi.utils.LanguageHelper
import com.sena.lanraragi.utils.OPERATE_KEY_VALUE1
import com.sena.lanraragi.utils.ScaleType
import com.sena.lanraragi.utils.getThemeColor
import com.sena.lanraragi.utils.toast
import kotlinx.coroutines.launch
import java.util.Locale

class SettingActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingBinding

    private lateinit var serverHostCustomPop: SettingInputPopup
    private lateinit var serverHostPop: BasePopupView
    private lateinit var serverKeyCustomPop: SettingInputPopup
    private lateinit var serverKeyPop: BasePopupView

    private lateinit var commonAppThemeCustomPop: SettingSelectPopup
    private lateinit var commonAppThemePop: BasePopupView
    private lateinit var commonAppLanguageCustomPop: SettingSelectPopup
    private lateinit var commonAppLanguagePop: BasePopupView
    private lateinit var commonViewMethodCustomPop: SettingSelectPopup
    private lateinit var commonViewMethodPop: BasePopupView

    /*
    private lateinit var readMergeMethodCustomPop: SettingSelectPopup
    private lateinit var readMergeMethodPop: BasePopupView
     */
    private lateinit var readScaleMethodCustomPop: SettingSelectPopup
    private lateinit var readScaleMethodPop: BasePopupView

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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val operate = intent.getStringExtra(INTENT_KEY_OPERATE)
                if (operate == OPERATE_KEY_VALUE1) {
                    binding.server.hostLayout.callOnClick()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val operate = intent.getStringExtra(INTENT_KEY_ARCHIVE)
        if (operate == OPERATE_KEY_VALUE1) {
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
            visibility = if (key.isEmpty()) View.GONE else View.VISIBLE
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
        /*
        val enableScrollRefresh = AppConfig.enableScrollRefresh
        binding.common.refreshButton.isChecked = enableScrollRefresh
        binding.common.scrollRefreshLayout.setOnClickListener {
            val curStatus = binding.common.refreshButton.isChecked
            val finStatus = !curStatus
            binding.common.refreshButton.isChecked = finStatus
            AppConfig.enableScrollRefresh = finStatus
            DataStoreHelper.updateValue(this, DataStoreHelper.KEY.COMMON_SCROLL_REFRESH, finStatus)
        }
         */
        val theme = AppConfig.theme
        val themeIndex = AppTheme.values().indexOf(theme)
        binding.common.themeText.text = resources.getStringArray(R.array.setting_common_app_theme_select).let {
            it.getOrNull(themeIndex) ?: it[0]
        }
        binding.common.themeLayout.setOnClickListener {
            commonAppThemePop.doOnAttach {
                commonAppThemeCustomPop.updateSelected(binding.common.themeText.text.toString())
            }
            commonAppThemePop.show()
        }
        val language = AppConfig.language
        val languageIndex = AppLanguage.values().indexOf(language)
        binding.common.languageText.text = resources.getStringArray(R.array.setting_common_app_language_select).let {
            it.getOrNull(languageIndex) ?: it[0]
        }
        binding.common.languageLayout.setOnClickListener {
            commonAppLanguagePop.doOnAttach {
                commonAppLanguageCustomPop.updateSelected(binding.common.languageText.text.toString())
            }
            commonAppLanguagePop.show()
        }
        val viewMethod = AppConfig.viewMethod
        val cardIndex = CardType.values().indexOf(viewMethod)
        binding.common.viewMethodText.text = resources.getStringArray(R.array.setting_common_view_method_select).let {
            it.getOrNull(cardIndex) ?: it[0]
        }
        binding.common.viewMethodLayout.setOnClickListener {
            commonViewMethodPop.doOnAttach {
                commonViewMethodCustomPop.updateSelected(binding.common.viewMethodText.text.toString())
            }
            commonViewMethodPop.show()
        }
        binding.common.clearPreCacheLayout.setOnClickListener {
            toast(R.string.setting_cache_clearing)
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
        /*
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
        */
        val scaleMethod = AppConfig.scaleMethod
        val scaleIndex = ScaleType.values().indexOf(scaleMethod)
        binding.reader.scaleText.text = resources.getStringArray(R.array.setting_read_scale_select).let {
            it.getOrNull(scaleIndex) ?: it[0]
        }
        binding.reader.scaleLayout.setOnClickListener {
            readScaleMethodPop.doOnAttach {
                readScaleMethodCustomPop.updateSelected(binding.reader.scaleText.text.toString())
            }
            readScaleMethodPop.show()
        }
        val enableScreenLight = AppConfig.enableScreenLight
        binding.reader.keepLightButton.isChecked = enableScreenLight
        binding.reader.keepLightLayout.setOnClickListener {
            val curStatus = binding.reader.keepLightButton.isChecked
            val finStatus = !curStatus
            binding.reader.keepLightButton.isChecked = finStatus
            AppConfig.enableScreenLight = finStatus
            DataStoreHelper.updateValue(this, DataStoreHelper.KEY.READ_KEEP_SCREEN_LIGHT, finStatus)
        }
        val enableSyn = AppConfig.enableSyn
        binding.reader.synButton.isChecked = enableSyn
        binding.reader.synLayout.setOnClickListener {
            val curStatus = binding.reader.synButton.isChecked
            val finStatus = !curStatus
            binding.reader.synButton.isChecked = finStatus
            AppConfig.enableSyn = finStatus
            DataStoreHelper.updateValue(this, DataStoreHelper.KEY.READ_SYN_PROGRESS, finStatus)
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
            val intent = Intent(this, OpenLicenseActivity::class.java)
            startActivity(intent)
        }
        binding.source.gplv3Layout.setOnClickListener {
            val intent = Intent(this, MyLicenseActivity::class.java)
            startActivity(intent)
        }
        binding.source.githubLayout.setOnClickListener {
            val url = "https://github.com/yangguanairen/LANraragi"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            val chooser = Intent.createChooser(intent, "Open with")
            startActivity(chooser)
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
        binding.debug.copyCrashLayout.setOnClickListener {
            val crashLog = GlobalCrashUtils.getCrashText(this)
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(null, crashLog)
            cm.setPrimaryClip(clipData)
        }
    }

    private fun initServerPop() {
        serverHostCustomPop = SettingInputPopup(this, R.string.setting_server_host_title)
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

        serverKeyCustomPop = SettingInputPopup(this, R.string.setting_server_key_title)
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
        commonAppThemeCustomPop = SettingSelectPopup(this, R.string.setting_common_app_theme_title, R.array.setting_common_app_theme_select)
        commonAppThemeCustomPop.setOnSelectedListener { pos, s ->
            binding.common.themeText.text = s
            AppConfig.theme = AppTheme.values().getOrNull(pos) ?: AppTheme.Dark
            DataStoreHelper.updateValue(this@SettingActivity, DataStoreHelper.KEY.COMMON_THEME, AppConfig.theme)
            val themeId = when (AppConfig.theme) {
                AppTheme.HVerse -> R.style.AppTheme_HVerse
                else -> R.style.AppTheme_Dark
            }
            setTheme(themeId)
            onThemeChanged(themeId)
        }
        commonAppThemePop = XPopup.Builder(this)
            .asCustom(commonAppThemeCustomPop)

        commonAppLanguageCustomPop = SettingSelectPopup(this, R.string.setting_common_app_language_title, R.array.setting_common_app_language_select)
        commonAppLanguageCustomPop.setOnSelectedListener { pos, s ->
            binding.common.languageText.text = s
            AppConfig.language = AppLanguage.values().getOrNull(pos) ?: AppLanguage.CHINA
            DataStoreHelper.updateValue(this@SettingActivity, DataStoreHelper.KEY.COMMON_LANGUAGE, AppConfig.language)
            val realLanguage = when (AppConfig.language) {
                AppLanguage.CHINA -> Locale.CHINA
                AppLanguage.JAPAN -> Locale.JAPAN
                else -> Locale.ENGLISH
            }
            LanguageHelper.setAppLanguage(this, realLanguage)
            onLanguageChange()
        }
        commonAppLanguagePop = XPopup.Builder(this)
            .asCustom(commonAppLanguageCustomPop)

        commonViewMethodCustomPop = SettingSelectPopup(this, R.string.setting_common_view_method_title, R.array.setting_common_view_method_select)
        commonViewMethodCustomPop.setOnSelectedListener { pos, s ->
            binding.common.viewMethodText.text = s
            AppConfig.viewMethod = CardType.values().getOrNull(pos) ?: CardType.LAND
            DataStoreHelper.updateValue(this@SettingActivity, DataStoreHelper.KEY.COMMON_VIEW_METHOD, AppConfig.viewMethod)
        }
        commonViewMethodPop = XPopup.Builder(this)
            .asCustom(commonViewMethodCustomPop)
    }

    private fun initReadPop() {
        /*
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
         */

        readScaleMethodCustomPop = SettingSelectPopup(this, R.string.setting_read_scale_method_title, R.array.setting_read_scale_select)
        readScaleMethodCustomPop.setOnSelectedListener { pos, s ->
            binding.reader.scaleText.text = s
            AppConfig.scaleMethod = ScaleType.values().getOrNull(pos) ?: ScaleType.FIT_PAGE
            DataStoreHelper.updateValue(this@SettingActivity, DataStoreHelper.KEY.READ_SCALE_METHOD, AppConfig.scaleMethod)
        }
        readScaleMethodPop = XPopup.Builder(this)
            .asCustom(readScaleMethodCustomPop)
    }

    private fun initSearchPop() {
        searchDelayCustomPop = SettingInputPopup(this, R.string.setting_search_delay_title, true)
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
        randomCountCustomPop = SettingInputPopup(this, R.string.setting_random_count_title, true)
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


    /*
    private fun changeViewStatus(view: View, b: Boolean) {
        val mAlpha = if (b) 1f else 0.5f
        view.apply {
            this.isEnabled = b
            this.alpha = mAlpha
        }
    }
     */

    override fun onThemeChanged(theme: Int) {
        super.onThemeChanged(theme)

        val bgColor1 = getThemeColor(R.attr.bgColor1)!!
        val bgColor2 = getThemeColor(R.attr.bgColor2)!!

        val textColor1 = getThemeColor(R.attr.textColor1)!!
        val textColor2 = getThemeColor(R.attr.textColor2)!!
        val textColor3 = getThemeColor(R.attr.textColor3)!!


        val textColor1ViewList = arrayListOf(
            binding.server.hostTitle, binding.server.keyTitle,
            binding.cache.cacheTitle,
            /* binding.common.refreshTitle, */
            binding.common.themeTitle, binding.common.viewMethodTitle,
            binding.common.previewCacheTitle, binding.common.languageTitle,
            binding.reader.rtlTitle, binding.reader.voiceTitle,
            /*
            binding.reader.mergeTitle, binding.reader.reverseMergeTitle, binding.reader.mergeMethodTitle,
             */
            binding.reader.scaleTitle, binding.reader.keepLightTitle, binding.reader.synTitle,
            binding.search.localSearchTitle, binding.search.searchDelayTitle,
            binding.random.randomCountTitle,
            binding.source.licensesTitle, binding.source.gplv3Title, binding.source.githubTitle,
            binding.debug.detailTitle, binding.debug.crashTitle, binding.debug.copyCrashTitle

        )
        val textColor2ViewList = arrayListOf(
            binding.server.hostText, binding.server.keyText,
            binding.cache.cacheText,
            binding.common.themeText, binding.common.viewMethodText, binding.common.previewCacheText,
            binding.common.languageText,
            /*
            binding.reader.mergeText, binding.reader.reverseMergeText, binding.reader.mergerMethodText,
             */
            binding.reader.scaleText, binding.reader.keepLightText, binding.reader.synText,
            binding.search.localSearchText, binding.search.searchDelayText,
            binding.random.randomCountText,
            binding.source.licensesText, binding.source.gplv3Text, binding.source.githubText,
            binding.debug.detailText, binding.debug.copyCrashText

        )
        val textColor3ViewList = arrayListOf(
            binding.server.serverTheme, binding.cache.cacheTheme, binding.common.commonTheme,
            binding.reader.readerTheme, binding.search.searchTheme, binding.random.randomTheme,
            binding.source.sourceTheme, binding.debug.debugTheme
        )

        binding.root.setBackgroundColor(bgColor1)
        // toolbar
        setNavigation(R.drawable.ic_arrow_back_24) {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.toolbar.setTitleTextColor(textColor1)
        binding.toolbar.setSubtitleTextColor(textColor2)
        binding.toolbar.setBackgroundColor(bgColor2)


        textColor1ViewList.forEach {
            it.setTextColor(textColor1)
        }
        textColor2ViewList.forEach {
            it.setTextColor(textColor2)
        }
        textColor3ViewList.forEach {
            it.setTextColor(textColor3)
        }
    }

    private fun onLanguageChange() {
        setAppBarText(getString(R.string.setting_toolbar_title), null)
        binding.server.serverTheme.text = getString(R.string.setting_server_theme)
        binding.server.hostTitle.text = getString(R.string.setting_server_host_title)
        binding.server.keyTitle.text = getString(R.string.setting_server_key_title)
        binding.cache.cacheTheme.text = getString(R.string.setting_cache_theme)
        binding.cache.cacheTitle.text = getString(R.string.setting_cache_clear_title)
        binding.common.commonTheme.text = getString(R.string.setting_common_theme)
        binding.common.themeTitle.text = getString(R.string.setting_common_app_theme_title)
        binding.common.themeText.text = resources.getStringArray(R.array.setting_common_app_theme_select).let {
            it.getOrNull(AppTheme.values().indexOf(AppConfig.theme)) ?: it[0]
        }
        binding.common.languageTitle.text = getString(R.string.setting_common_app_language_title)
        binding.common.viewMethodTitle.text = getString(R.string.setting_common_view_method_title)
        binding.common.viewMethodText.text = resources.getStringArray(R.array.setting_common_view_method_select).let {
            it.getOrNull(CardType.values().indexOf(AppConfig.viewMethod)) ?: it[0]
        }
        binding.common.previewCacheTitle.text = getString(R.string.setting_common_clear_title)
        binding.common.previewCacheText.text = getString(R.string.setting_common_clear_subtitle)
        binding.reader.readerTheme.text = getString(R.string.setting_read_theme)
        binding.reader.rtlTitle.text = getString(R.string.setting_read_rtl_title)
        binding.reader.voiceTitle.text = getString(R.string.setting_read_voice_title)
        binding.reader.scaleTitle.text = getString(R.string.setting_read_scale_method_title)
        binding.reader.scaleText.text = resources.getStringArray(R.array.setting_read_scale_select).let {
            it.getOrNull(ScaleType.values().indexOf(AppConfig.scaleMethod)) ?: it[0]
        }
        binding.reader.keepLightTitle.text = getString(R.string.setting_read_keep_screen_light_title)
        binding.reader.keepLightText.text = getString(R.string.setting_read_keep_screen_light_subtitle)
        binding.reader.synTitle.text = getString(R.string.setting_read_syn_progress_title)
        binding.reader.synText.text = getString(R.string.setting_read_syn_progress_subtitle)
        binding.search.searchTheme.text = getString(R.string.setting_search_theme)
        binding.search.localSearchTitle.text = getString(R.string.setting_search_local_title)
        binding.search.localSearchText.text = getString(R.string.setting_search_local_subtitle)
        binding.search.searchDelayTitle.text = getString(R.string.setting_search_delay_title)
        binding.search.searchDelayText.text = String.format(getString(R.string.setting_search_delay_subtitle), AppConfig.searchDelay)
        binding.random.randomTheme.text = getString(R.string.setting_random_theme)
        binding.random.randomCountTitle.text = getString(R.string.setting_random_count_title)
        binding.random.randomCountText.text = String.format(getString(R.string.setting_random_count_subtitle), AppConfig.randomCount)
        binding.source.sourceTheme.text = getString(R.string.setting_source_theme)
        binding.debug.debugTheme.text = getString(R.string.setting_debug_theme)
        binding.debug.detailTitle.text = getString(R.string.setting_debug_detail_title)
        binding.debug.detailText.text = getString(R.string.setting_debug_detail_subtitle)
        binding.debug.crashTitle.text = getString(R.string.setting_debug_crash_title)
        binding.debug.copyCrashTitle.text = getString(R.string.setting_debug_copy_crash_title)
        binding.debug.copyCrashText.text = getString(R.string.setting_debug_copy_crash_subtitle)
    }

}