package com.sena.lanraragi.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
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
            layoutManager = GridLayoutManager(context, 2)
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
                        Toast.makeText(requireContext(), "获取随机档案失败...", Toast.LENGTH_SHORT).show()
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