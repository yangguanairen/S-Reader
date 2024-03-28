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
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.databinding.FragmentIntroduceBinding
import com.sena.lanraragi.ui.MainActivity
import com.sena.lanraragi.ui.reader.ReaderActivity
import com.sena.lanraragi.utils.ImageUtils
import com.sena.lanraragi.utils.getOrNull
import kotlinx.coroutines.launch


private const val ARG_ARCHIVE = "arc_archive"

class IntroduceFragment : BaseFragment() {
    private var mArchive: Archive? = null

    private lateinit var binding: FragmentIntroduceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mArchive = getOrNull { it.getSerializable(ARG_ARCHIVE) as Archive }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentIntroduceBinding.inflate(inflater)

        mArchive?.let { initView(it) }
        return binding.root
    }

    private fun initView(archive: Archive) {
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
        binding.startRead.setOnClickListener {
            val intent = Intent(requireContext(), ReaderActivity::class.java)
            intent.putExtra("arcId", archive.arcid)
            startActivity(intent)
        }

        binding.title.text = archive.title
        ImageUtils.loadThumb(requireContext(), archive.arcid, binding.cover)
        archive.tags?.let { s ->
            binding.tageViewer.setTags(s)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(archive: Archive) = IntroduceFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_ARCHIVE, archive)
            }
        }
    }
}