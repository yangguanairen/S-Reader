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
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.databinding.FragmentPreviewBinding
import com.sena.lanraragi.ui.reader.ReaderActivity
import com.sena.lanraragi.utils.INTENT_KEY_ARCHIVE
import com.sena.lanraragi.utils.INTENT_KEY_LIST
import com.sena.lanraragi.utils.INTENT_KEY_POS
import com.sena.lanraragi.utils.NewHttpHelper
import com.sena.lanraragi.utils.getOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val ARG_ARCHIVE = "arc_archive"

class PreviewFragment : BaseFragment() {

    private var mArchive: Archive? = null

    private lateinit var binding: FragmentPreviewBinding
    private val adapter: PreviewAdapter by lazy { PreviewAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            mArchive = getOrNull { it.getSerializable(ARG_ARCHIVE) as Archive }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPreviewBinding.inflate(layoutInflater)

        mArchive?.let { initView() }
        return binding.root
    }

    private fun initView() {
        adapter.setOnItemClickListener { a, _, p ->
            a.getItem(p)?.let {
                val list = a.items.toTypedArray()
                val pos = p
                val archive = mArchive
                val intent = Intent(requireContext(), ReaderActivity::class.java)
                intent.putExtra(INTENT_KEY_ARCHIVE, archive)
                intent.putExtra(INTENT_KEY_LIST, list)
                intent.putExtra(INTENT_KEY_POS, pos)
                requireContext().startActivity(intent)
            }

        }
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.adapter = adapter
    }

    override fun lazyLoad() {
        super.lazyLoad()
        lifecycleScope.launch {
            mArchive?.let { initData(it) }
        }
    }

    private fun initData(archive: Archive) {
        lifecycleScope.launch {
           val pageCount = archive.pagecount
            if (pageCount != null) {
                val emptyList = (0 until pageCount).map { "" }
                adapter.submitList(emptyList)
            }
            val result = withContext(Dispatchers.IO) {
                NewHttpHelper.extractManga(archive.arcid)
            }
            if (result != null) {
                adapter.submitList(result)
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

            R.id.gotoTop -> {
                binding.recyclerView.layoutManager?.scrollToPosition(0)
            }

            R.id.gotoBottom -> {
                binding.recyclerView.layoutManager?.scrollToPosition(adapter.items.size - 1)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        @JvmStatic
        fun newInstance(archive: Archive) = PreviewFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_ARCHIVE, archive)
            }
        }
    }
}