package com.sena.lanraragi.ui.detail

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
import com.sena.lanraragi.utils.NewHttpHelper
import com.sena.lanraragi.utils.getOrNull
import kotlinx.coroutines.launch


private const val ARG_ARCHIVE = "arc_archive"

class PreviewFragment : BaseFragment() {

    private var mArchive: Archive? = null

    private lateinit var binding: FragmentPreviewBinding
    private val adapter: PreviewAdapter by lazy { PreviewAdapter() }
    private val vm: PreviewVM by lazy { PreviewVM() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            mArchive = getOrNull { it.getSerializable(ARG_ARCHIVE) as Archive }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPreviewBinding.inflate(layoutInflater)

        mArchive?.let {
            initView(it)
            initVM()
        }

        return binding.root
    }

    private fun initView(archive: Archive) {
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.adapter = adapter
        archive.pagecount?.let {
            val emptyList = (0 until it).map { "" }
            adapter.submitList(emptyList)
        }
    }

    private fun initVM() {
        vm.pages.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    override fun lazyLoad() {
        super.lazyLoad()
        lifecycleScope.launch {
            mArchive?.arcid?.let { vm.initData(it) }
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