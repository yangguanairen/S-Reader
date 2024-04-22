package com.sena.lanraragi.ui.detail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.lifecycleScope
import com.sena.lanraragi.BaseFragment
import com.sena.lanraragi.R
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.databinding.FragmentIntroduceBinding
import com.sena.lanraragi.ui.MainActivity
import com.sena.lanraragi.ui.reader.ReaderActivity
import com.sena.lanraragi.utils.COVER_SHARE_ANIMATION
import com.sena.lanraragi.utils.INTENT_KEY_ARCHIVE
import com.sena.lanraragi.utils.INTENT_KEY_QUERY
import com.sena.lanraragi.utils.ImageLoad
import com.sena.lanraragi.utils.NewHttpHelper
import com.sena.lanraragi.utils.getOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val ARG_ARCHIVE = "arc_archive"

class IntroduceFragment : BaseFragment() {
    private var mArchive: Archive? = null

    private lateinit var binding: FragmentIntroduceBinding

    private val bookmarkTextMap by lazy {
        mapOf(
            true to getString(R.string.detail_introduce_cancel_bookmark),
            false to getString(R.string.detail_introduce_add_bookmark)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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

        binding.tageViewer.setOnTagSelectedListener { header, content ->
            val query = if (header.isBlank()) content else "$header:$content"
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra(INTENT_KEY_QUERY, query)
            startActivity(intent)
        }
        binding.startRead.setOnClickListener {
            val intent = Intent(requireContext(), ReaderActivity::class.java)
            intent.putExtra(INTENT_KEY_ARCHIVE, archive)
            startActivity(intent)
        }
        binding.bookmark.setOnClickListener {
            val isBookmarked = archive.isBookmark
            val fStatus = !isBookmarked
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    LanraragiDB.updateArchiveBookmark(archive.arcid, fStatus)
                }
                archive.isBookmark = fStatus
                binding.bookmark.text = bookmarkTextMap[fStatus]
            }
        }
    }

    override fun lazyLoad() {
        super.lazyLoad()
        mArchive?.let { initData(it) }
        mArchive?:let { requireActivity().supportStartPostponedEnterTransition() }
    }

    private fun initData(archive: Archive) {
        lifecycleScope.launch {

            binding.title.text = archive.title
//            val isBookmarked = withContext(Dispatchers.IO) {
//                LanraragiDB.queryArchiveById(archive.arcid)
//            }?.isBookmark ?: false

            binding.bookmark.text = bookmarkTextMap[archive.isBookmark]
            ViewCompat.setTransitionName(binding.cover, COVER_SHARE_ANIMATION)
            ImageLoad.Builder(requireContext())
                .loadThumb(archive.arcid)
                .doOnFinish {
                    requireActivity().supportStartPostponedEnterTransition()
                }
                .into(binding.cover)
                .execute()
            archive.tags?.let { s ->
                binding.tageViewer.setTags(s)
            }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_detail_introduce, menu)
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
                mArchive?.arcid?.let {
                    ImageLoad.Builder(requireContext())
                        .loadThumb(it)
                        .isIgnoreDiskCache(true)
                        .into(binding.cover)
                        .execute()
                }

            }
        }

        return super.onOptionsItemSelected(item)
    }
}