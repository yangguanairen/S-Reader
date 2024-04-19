package com.sena.lanraragi.ui.reader

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.sena.lanraragi.BaseActivity
import com.sena.lanraragi.R
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.databinding.ActivityReaderBinding
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.INTENT_KEY_ARCHIVE
import com.sena.lanraragi.utils.getOrNull
import kotlinx.coroutines.launch

class ReaderActivity : BaseActivity() {

    private var mArchive: Archive? = null
//    private var mFileNameList: List<String>? = null

    private val binding: ActivityReaderBinding by lazy { ActivityReaderBinding.inflate(layoutInflater) }
    private val vm: ReaderVM by lazy { ReaderVM() }
    private lateinit var adapter: ReaderAdapter
    private lateinit var webtoonAdapter: WebtoonAdapter
    private lateinit var bottomPopup: BasePopupView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mArchive = getOrNull { intent.getSerializableExtra(INTENT_KEY_ARCHIVE) as Archive }
//        mFileNameList = getOrNull { intent.getStringArrayListExtra("fileNameList")?.toList() }
        if (mArchive == null) {
            DebugLog.e("ReaderActivity.onCreate(): archive is null")
            return
        }
//        val title = mArchive?.title
//        val pageCount = mFileNameList?.size ?: mArchive?.pagecount

        initView()
        initVM()
        initData()
    }


    private fun initView() {
        // 初始Toolbar
        setNavigation(R.drawable.ic_arrow_back_24) { finish() }
        binding.contentReader.appBar.visibility = View.INVISIBLE
        binding.contentReader.seekbar.visibility = View.INVISIBLE

        // 初始popup
        val customPopup = ReaderBottomPopup(this).apply {
            setOnScaleTypeChangeListener { scaleType ->
                when (scaleType) {
                    ReaderBottomPopup.ScaleType.WEBTOON -> {
                        // binding.contentReader.viewPager.layoutDirection = View.LAYOUT_DIRECTION_LTR
                        // binding.contentReader.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
                        binding.contentReader.recyclerView.visibility= View.VISIBLE
                        binding.contentReader.viewPager.visibility = View.GONE
                        val list = adapter.items.map { p -> p.first }
                        webtoonAdapter.submitList(list)
                        val pos2 = vm.curPos2.value ?: 0
                        binding.contentReader.recyclerView.scrollToPosition(pos2)
                    }
                    else -> {
                        // binding.contentReader.viewPager.layoutDirection = View.LAYOUT_DIRECTION_RTL
                        // binding.contentReader.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                        binding.contentReader.recyclerView.visibility= View.GONE
                        binding.contentReader.viewPager.visibility = View.VISIBLE
                        val list = adapter.items.map { p -> Pair(p.first, scaleType) }
                        adapter.submitList(list)
                        val pos2 = vm.curPos2.value ?: 0
                        binding.contentReader.viewPager.setCurrentItem(pos2, true)
                    }
                }
            }
        }
        bottomPopup = XPopup.Builder(this)
            .borderRadius(8f)
            .isDestroyOnDismiss(false)
            .asCustom(customPopup)

        // 初始ViewPager
        // 改为从右到左阅读
        binding.contentReader.viewPager.apply {
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    vm.setCurPosition2(position)
                }
            })
        }
        binding.contentReader.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as LinearLayoutManager
                val firstVisiblePos = lm.findFirstVisibleItemPosition()
                vm.setCurPosition2(firstVisiblePos)
            }
        })


        initViewPager()
        initWebtoon()

        binding.contentReader.seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            private var isHuman = false

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    vm.setCurPosition1(progress)
                } else {
//                    DebugLog.e("非人类")
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


    private fun initVM() {
        vm.fileNameList.observe(this) {
            binding.contentReader.seekbar.max = it.size
            adapter.let { a ->
                // TODO: 默认从右往左读，后续读取设置情况
                a.submitList(it.map { s -> Pair(s, ReaderBottomPopup.ScaleType.FIT_WIDTH) })
                val pos = vm.curPos2.value ?: -1
                val finalPos = if (pos < 0 || pos > it.size - 1) 0 else pos
                binding.contentReader.viewPager.setCurrentItem(finalPos, false)
            }
            webtoonAdapter.submitList(it)
        }
        vm.curPos1.observe(this) {
             binding.contentReader.viewPager.setCurrentItem(it, true)
             binding.contentReader.recyclerView.scrollToPosition(it)
        }
        vm.curPos2.observe(this) {
            val title = mArchive?.title
            val subtitle = "${it + 1}/${adapter.itemCount}页"
            setAppBarText(title, subtitle)
            // 更改seekbar的progress
            binding.contentReader.seekbar.progress = it
        }

    }

    private fun initViewPager() {
        // viewPager2详解
        // https://blog.51cto.com/u_13303/6872084
        adapter = ReaderAdapter()
        adapter.setOnImageClickListener { _, _, _ ->
            binding.contentReader.appBar.apply {
                if (visibility == View.VISIBLE) {
                    visibility = View.INVISIBLE
                    binding.contentReader.seekbar.visibility = View.INVISIBLE
                } else {
                    visibility = View.VISIBLE
                    binding.contentReader.seekbar.visibility = View.VISIBLE
                }
            }
        }
        adapter.setOnImageLongClickListener { _, _, _ ->
            bottomPopup.show()
            true
        }
        binding.contentReader.viewPager.adapter = adapter
    }

    private fun initWebtoon() {
        webtoonAdapter = WebtoonAdapter()
        webtoonAdapter.setOnImageClickListener { _, _, _ ->
            binding.contentReader.appBar.apply {
                if (visibility == View.VISIBLE) {
                    visibility = View.INVISIBLE
                    binding.contentReader.seekbar.visibility = View.INVISIBLE
                } else {
                    visibility = View.VISIBLE
                    binding.contentReader.seekbar.visibility = View.VISIBLE
                }
            }
        }
        webtoonAdapter.setOnImageLongClickListener { _, _, _ ->
            bottomPopup.show()
            true
        }
        binding.contentReader.recyclerView.adapter = webtoonAdapter
        binding.contentReader.recyclerView.layoutManager = LinearLayoutManager(this)
    }


    private fun initData() {

        val pageCount = mArchive?.pagecount
        if (pageCount != null) {
            val emptyList = (0 until pageCount).map { Pair("", ReaderBottomPopup.ScaleType.FIT_WIDTH) }
            adapter.submitList(emptyList)
            webtoonAdapter.submitList(emptyList())
            binding.contentReader.seekbar.max = pageCount
            vm.setCurPosition1(0)
        }

        // 临时测试用
        binding.contentReader.viewPager.visibility = View.VISIBLE
        binding.contentReader.recyclerView.visibility = View.GONE

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                if (mFileNameList != null) {
//                    vm.setFileNameList(mFileNameList!!)
//                } else {
//                    vm.initData(id)
//                }
                mArchive?.arcid?.let { vm.initData(it) }
            }
        }
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_reader, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.setting -> {
                bottomPopup.show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}