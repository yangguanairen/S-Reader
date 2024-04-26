package com.sena.lanraragi.ui.reader

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.BaseActivity
import com.sena.lanraragi.R
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.databinding.ActivityReaderBinding
import com.sena.lanraragi.ui.widet.BookmarkView
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.INTENT_KEY_ARCID
import com.sena.lanraragi.utils.INTENT_KEY_POS
import com.sena.lanraragi.utils.ScaleType
import com.sena.lanraragi.utils.TouchZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReaderActivity : BaseActivity() {

    private var mId: String? = null
    private var mArchive: Archive? = null
    private var mPos: Int = -1
//    private var mFileNameList: List<String>? = null

    private val binding: ActivityReaderBinding by lazy { ActivityReaderBinding.inflate(layoutInflater) }
    private val vm: ReaderVM by lazy { ReaderVM() }
    private lateinit var viewPagerAdapter: ReaderAdapter
    private lateinit var webtoonAdapter: ReaderAdapter
    private lateinit var bottomPopup: BasePopupView

    private lateinit var bookmarkMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mId = intent.getStringExtra(INTENT_KEY_ARCID)
        mPos = intent.getIntExtra(INTENT_KEY_POS, -1)

        if (mId == null) {
            DebugLog.e("ReaderActivity.onCreate(): arcId is null")
            return
        }
        if (AppConfig.enableScreenLight) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        initVM()
        initView()
        mId?.let { initData(it) }
    }

    @SuppressLint("SetTextI18n")
    private fun initVM() {
        vm.fileNameList.observe(this) { list ->
            binding.contentReader.apply {
                seekbar.max = list.size - 1
                totalPage.text = list.size.toString()
                if (AppConfig.scaleMethod == ScaleType.WEBTOON) {
                    recyclerView.visibility = View.VISIBLE
                    viewPager.visibility = View.INVISIBLE
                    webtoonAdapter.submitList(list)
                    if (mPos > 0) {
                        vm.setCurPosition(mPos)
                        mPos = -1
                    } else {
                        vm.updateList()
                    }
                    webtoonAdapter.onScaleChange(AppConfig.scaleMethod)
                } else {
                    recyclerView.visibility = View.INVISIBLE
                    viewPager.visibility = View.VISIBLE
                    viewPagerAdapter.submitList(list)
                    if (mPos > 0) {
                        vm.setCurPosition(mPos)
                        mPos = -1
                    } else {
                        vm.updateList()
                    }
                    viewPagerAdapter.onScaleChange(AppConfig.scaleMethod)
                }
            }

        }
        vm.curPos.observe(this) { page ->
            val title = mArchive?.title
            val subtitle = "${page + 1}/${vm.fileNameList.value?.size ?: 0}页"
            setAppBarText(title, subtitle)
            binding.contentReader.apply {
                seekbar.progress = page
                curPage.text = (page + 1).toString()
                if (AppConfig.scaleMethod == ScaleType.WEBTOON) {
                    if (!vm.fromWebtoon) {
                        recyclerView.scrollToPosition(page)
                    } else {
                        vm.fromWebtoon = false
                    }
                } else {
                    viewPager.setCurrentItem(page, false)
                }
            }
        }
    }

    private fun initView() {
        initToolbar()
        // 初始popup
        val customPopup = ReaderBottomPopup(this).apply {
            setOnScaleTypeChangeListener {
                vm.setFileNameList(vm.fileNameList.value ?: emptyList())
            }
            setOnItemClickListener { layoutId: Int ->
                when (layoutId) {
                    R.id.goToDetail -> {
                        onBackPressedDispatcher.onBackPressed()
                    }
                    R.id.selectPage -> {
                        val size = vm.fileNameList.value ?.size
                        val pos = vm.curPos.value ?: 0
                        val id = mId
                        if (size == null || size <= 0 || size - 1 < pos || id == null) return@setOnItemClickListener
                        val list = (1..size).map { Pair(id, it.toString()) }
                        XPopup.Builder(this@ReaderActivity)
                            .isDestroyOnDismiss(true)
                            .asCustom(ReaderFullScreenPopup(this@ReaderActivity, pos, list).apply {
                                setOnPageSelectedListener {
                                    vm.setCurPosition(it)
                                }
                            })
                            .show()
                    }
                    R.id.showBookmark -> {
                        displayToolbar()
                        binding.drawerLayout.openDrawer(binding.leftNav)
                    }
                }
            }
        }
        bottomPopup = XPopup.Builder(this)
            .borderRadius(8f)
            .isDestroyOnDismiss(false)
            .asCustom(customPopup)

        initViewPager()
        initWebtoon()
        initLeftNav()

        binding.contentReader.seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            private var isHuman = false

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    vm.setCurPosition(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isHuman = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isHuman = false
            }

        })
    }

    private fun initToolbar() {
        // 初始Toolbar
        setNavigation(R.drawable.ic_arrow_back_24) { finish() }
        binding.contentReader.appBar.visibility = View.INVISIBLE
        binding.contentReader.seekbarLayout.visibility = View.INVISIBLE
    }

    private fun initViewPager() {
        // viewPager2详解
        // https://blog.51cto.com/u_13303/6872084
        val pageChangeListener = object : OnPageChangeCallback() {
            var isHuman = false
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (isHuman) {
                    vm.setCurPosition(position)
                    isHuman = false
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == SCROLL_STATE_DRAGGING) { // 判断是否是用户主动进行的拖动
                    isHuman = true
                }
            }
        }
        viewPagerAdapter = ReaderAdapter()
        viewPagerAdapter.setOnImageClickListener { onItemTap(it) }
        viewPagerAdapter.setOnImageLongClickListener {
            onItemLongPress()
            true
        }
        binding.contentReader.viewPager.apply {
            layoutDirection = if (AppConfig.enableRtl) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            registerOnPageChangeCallback(pageChangeListener)
            adapter = viewPagerAdapter
        }
    }

    private fun initWebtoon() {
        webtoonAdapter = ReaderAdapter()
        webtoonAdapter.setOnImageLongClickListener{
            onItemLongPress()
            true
        }
        webtoonAdapter.setOnImageClickListener { onItemTap(it) }
        val onScrollListener = object : RecyclerView.OnScrollListener() {
            private var isHuman = false
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as LinearLayoutManager
                val firstVisiblePos = lm.findFirstVisibleItemPosition()
                val lastVisiblePos = lm.findLastVisibleItemPosition()
                val finalPos = if (dy >= 0) lastVisiblePos else firstVisiblePos
                if (isHuman) {
                    vm.fromWebtoon = true
                    vm.setCurPosition(finalPos)
                    isHuman = false
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) { // 判断是否是用户主动进行的拖动
                    isHuman = true
                }
            }
        }

        binding.contentReader.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ReaderActivity)
            addOnScrollListener(onScrollListener)
            adapter = webtoonAdapter
            isFocusable= false
            tapListener = ::onItemTap
            longPressListener = ::onItemLongPress
        }
    }

    private fun initLeftNav() {
        val bookmarkView = binding.leftNav.getHeaderView(0).findViewById<BookmarkView>(R.id.bookmarkView)
        bookmarkView.setOnItemClickListener { a, _, p ->
            // TODO: 更换数据源
           //  a.getItem(p)?.let { initData(it) }
        }
    }


    private fun initData(id: String) {
        lifecycleScope.launch {
            val archive = withContext(Dispatchers.IO) {
                LanraragiDB.queryArchiveById(id)
            }
            if (archive == null) {
                DebugLog.e("ReaderActivity: 数据库中不存在此数据: $id")
                return@launch
            }

            mArchive = archive
//            binding.contentReader.toolbar.title = archive.title
            val pageCount = archive.pagecount
            if (pageCount != null) {
                val emptyList = (0 until pageCount).map { "" }
                vm.setFileNameList(emptyList)
            }

            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.initData(archive.arcid)
            }
        }
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_reader, menu)
        menu?.let {
            bookmarkMenuItem = it.findItem(R.id.bookmark)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.setting -> {
                bottomPopup.show()
            }
            R.id.bookmark -> {
                val archive = mArchive ?: return false
                val isBookmarked = archive.isBookmark
                val finStatus = !isBookmarked
                val icon = if (finStatus) R.drawable.ic_bookmarked_24 else R.drawable.ic_bookmark_border_24
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        LanraragiDB.updateArchiveBookmark(archive.arcid, finStatus)
                    }
                    mArchive?.isBookmark = finStatus
                    item.setIcon(icon)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (AppConfig.scaleMethod != ScaleType.WEBTOON) {
            viewPagerAdapter.onConfigChange()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (!AppConfig.enableVoice) return false
                val cPos = vm.curPos.value ?: 0
                vm.setCurPosition(cPos + 1)
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (!AppConfig.enableVoice) return false
                val cPos = vm.curPos.value ?: 0
                vm.setCurPosition(cPos - 1)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun displayToolbar() {
        binding.contentReader.appBar.apply {
            mId?.let { id ->
                lifecycleScope.launch {
                    val isBookmarked = withContext(Dispatchers.IO) {
                        LanraragiDB.queryArchiveById(id)
                    }?.isBookmark ?: false
                    val icon = if (isBookmarked) R.drawable.ic_bookmarked_24 else R.drawable.ic_bookmark_border_24
                    bookmarkMenuItem.setIcon(icon)
                }
            }
            if (visibility == View.VISIBLE) {
                visibility = View.INVISIBLE
                binding.contentReader.seekbarLayout.visibility = View.INVISIBLE
            } else {
                visibility = View.VISIBLE
                binding.contentReader.seekbarLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun onItemTap(zone: TouchZone) {
        if (zone == TouchZone.Center || AppConfig.scaleMethod == ScaleType.WEBTOON) {
            displayToolbar()
            return
        }
        val isRtl = AppConfig.enableRtl
        val curPos = vm.curPos.value ?: return
        if (zone == TouchZone.Left) {
            vm.setCurPosition(if (isRtl) curPos + 1 else curPos - 1)
        } else if (zone == TouchZone.Right) {
            vm.setCurPosition(if (isRtl) curPos - 1 else curPos + 1)
        }
    }

    private fun onItemLongPress() {
        bottomPopup.show()
    }
}