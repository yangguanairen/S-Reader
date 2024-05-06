package com.sena.lanraragi.ui.detail

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.sena.lanraragi.BaseFragment
import com.sena.lanraragi.R
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.databinding.FragmentPreviewBinding
import com.sena.lanraragi.ui.reader.ReaderActivity
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.INTENT_KEY_ARCID
import com.sena.lanraragi.utils.INTENT_KEY_POS
import com.sena.lanraragi.utils.NewHttpHelper
import com.sena.lanraragi.utils.getOrNull
import com.sena.lanraragi.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val ARG_ARCHIVE_ID = "arc_archive_id"

class PreviewFragment : BaseFragment() {

    private var mId: String? = null
    private var mArchive: Archive? = null

    private lateinit var binding: FragmentPreviewBinding
    private val mAdapter: PreviewAdapter = PreviewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            mId = it.getString(ARG_ARCHIVE_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPreviewBinding.inflate(layoutInflater)

        mId?.let { initView() }
        return binding.root
    }

    private fun initView() {
        mAdapter.setOnItemClickListener { a, _, p ->
            a.getItem(p)?.let {
                val intent = Intent(requireContext(), ReaderActivity::class.java)
                intent.putExtra(INTENT_KEY_ARCID, mId)
                intent.putExtra(INTENT_KEY_POS, p)
                requireContext().startActivity(intent)
            }

        }
        binding.recyclerView.apply {
            layoutManager = getListLayoutManager()
            adapter = mAdapter
        }
    }

    override fun lazyLoad() {
        super.lazyLoad()
        mId?.let { initData(it) }
    }

    private fun initData(id: String) {
        lifecycleScope.launch {
            val archive = withContext(Dispatchers.IO) {
                LanraragiDB.queryArchiveById(id)
            }
            if (archive == null) {
                DebugLog.e("PreviewFragment: 数据库中不存在此数据: $id")
                return@launch
            }
            mArchive = archive
            extractManga(id)
        }
    }

    private suspend fun extractManga(id: String) {
        // 服务器强行提取缩略图
        val result = withContext(Dispatchers.IO) {
            NewHttpHelper.extractManga(id)
        }
        if (result != null) {
            (1..result.size).map { index ->
                Pair(id, "$index")
            }.let {
                mAdapter.submitList(it)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.recyclerView.apply {
            // 保存当前的浏览状态
            val lm = getOrNull { layoutManager as LinearLayoutManager }
            val scrollPos = lm?.findFirstVisibleItemPosition() ?: 0
            val startView = getChildAt(0)
            val scrollTopOffset = if (startView == null) 0 else paddingTop - startView.top
            // 切换布局
            layoutManager = getListLayoutManager()
            // 恢复上次的浏览状态
            layoutManager?.scrollToPosition(scrollPos)
            lm?.scrollToPositionWithOffset(scrollPos, scrollTopOffset)
        }
    }

    private fun getListLayoutManager(): LinearLayoutManager {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >=
                Configuration.SCREENLAYOUT_SIZE_LARGE
        return when {
            isLandscape && isTablet -> GridLayoutManager(context, 4)
            isLandscape && !isTablet -> GridLayoutManager(context, 3)
            !isLandscape && isTablet -> GridLayoutManager(context, 3)
            else -> GridLayoutManager(context, 2)
        }
    }




    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_detail_preview, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.random -> {
                lifecycleScope.launch {
                    val randomArchive = NewHttpHelper.getRandomArchive(1).getOrNull(0)
                    if (randomArchive != null) {
                        mNewArchiveListener?.onGenerateArchive(randomArchive)
                    } else {
                        toast(R.string.main_get_random_failed)
                    }
                }
            }

            R.id.refresh -> {
                mId?.let {
                    lifecycleScope.launch {
                        extractManga(it)
                    }
                }
            }

            R.id.gotoTop -> {
                binding.recyclerView.layoutManager?.scrollToPosition(0)
            }

            R.id.gotoBottom -> {
                binding.recyclerView.layoutManager?.scrollToPosition(mAdapter.items.size - 1)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        @JvmStatic
        fun newInstance(id: String) = PreviewFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_ARCHIVE_ID, id)
            }
        }
    }
}