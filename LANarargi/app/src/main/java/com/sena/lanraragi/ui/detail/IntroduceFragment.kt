package com.sena.lanraragi.ui.detail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.lifecycleScope
import com.sena.lanraragi.BaseFragment
import com.sena.lanraragi.databinding.FragmentIntroduceBinding
import com.sena.lanraragi.ui.MainActivity
import com.sena.lanraragi.utils.ImageUtils
import kotlinx.coroutines.launch


private const val ARG_ID = "arcId"

class IntroduceFragment : BaseFragment() {
    private var arcId: String? = null

    private lateinit var binding: FragmentIntroduceBinding
    private lateinit var vm: IntroduceVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            arcId = it.getString(ARG_ID)
        }

        vm = IntroduceVM()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentIntroduceBinding.inflate(inflater)

        initView()
        initViewModel()
        return binding.root
    }

    override fun lazyLoad() {
        super.lazyLoad()
        lifecycleScope.launch {
            arcId?.let { vm.initData(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        mListener?.onResumeListener(arrayListOf("详细"))
    }

    private fun initView() {
        // 设置textView字体大小
        // https://blog.csdn.net/mqdxiaoxiao/article/details/884110611
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.title.setAutoSizeTextTypeUniformWithConfiguration(10, 18, 1, TypedValue.COMPLEX_UNIT_SP)
        } else {
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(binding.title, 10, 18, 1, TypedValue.COMPLEX_UNIT_SP)
        }

        binding.tageViewer.setOnItemClickListener { header, content ->
            val query = if (header.isBlank()) content else "$header:$content"
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra("query", query)
            startActivity(intent)
        }
    }

    private fun initViewModel() {
        vm.archive.observe(viewLifecycleOwner) {
            binding.title.text = it.title
            ImageUtils.loadThumb(requireContext(), it.arcid, binding.cover)
            it.tags?.let { s ->
                binding.tageViewer.setTags(s)
            }
        }

    }


    companion object {
        @JvmStatic
        fun newInstance(id: String?) = IntroduceFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ID, id)
            }
        }
    }
}