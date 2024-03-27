package com.sena.lanraragi.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.sena.lanraragi.BaseFragment
import com.sena.lanraragi.databinding.FragmentPreviewBinding
import kotlinx.coroutines.launch


private const val ARG_ID = "arcId"


class PreviewFragment : BaseFragment() {

    private var arcId: String? = null

    private lateinit var binding: FragmentPreviewBinding
    private lateinit var adapter: PreviewAdapter
    private lateinit var vm: PreviewVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            arcId = it.getString(ARG_ID)
        }
        vm = PreviewVM()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPreviewBinding.inflate(layoutInflater)
        initView()
        initVM()
        return binding.root
    }

    private fun initView() {
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        adapter = PreviewAdapter()
//        adapter.setArcid(arcId)
        binding.recyclerView.adapter = adapter
    }

    private fun initVM() {
        vm.archive.observe(viewLifecycleOwner) {
            val count = it.pagecount ?: 0
            val list = (1..count).map { "" }
            adapter.submitList(list)
        }
        vm.pages.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    override fun lazyLoad() {
        super.lazyLoad()
        lifecycleScope.launch {
            lifecycleScope.launch {
                arcId?.let { vm.initData(it) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val list = arrayListOf(
            "预览图", (vm.archive.value?.pagecount ?: -1).toString()
        )
        mListener?.onResumeListener(list)
    }

    companion object {
        @JvmStatic
        fun newInstance(id: String?) = PreviewFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ID, id)
            }
        }
    }
}