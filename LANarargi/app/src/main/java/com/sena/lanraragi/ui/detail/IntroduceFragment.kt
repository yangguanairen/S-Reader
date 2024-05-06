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
import androidx.core.view.ViewCompat
import androidx.core.view.doOnAttach
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.lifecycleScope
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.BaseFragment
import com.sena.lanraragi.R
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.databinding.FragmentIntroduceBinding
import com.sena.lanraragi.ui.MainActivity
import com.sena.lanraragi.ui.reader.ReaderActivity
import com.sena.lanraragi.utils.COVER_SHARE_ANIMATION
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.INTENT_KEY_ARCID
import com.sena.lanraragi.utils.INTENT_KEY_POS
import com.sena.lanraragi.utils.INTENT_KEY_QUERY
import com.sena.lanraragi.utils.ImageLoad
import com.sena.lanraragi.utils.NewHttpHelper
import com.sena.lanraragi.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val ARG_ARCHIVE_ID = "arc_archive_id"

class IntroduceFragment : BaseFragment() {

    private var mId: String? = null
    private var mArchive: Archive? = null

    private lateinit var binding: FragmentIntroduceBinding

    private val bookmarkTextMap by lazy {
        mapOf(
            true to getString(R.string.detail_introduce_cancel_bookmark),
            false to getString(R.string.detail_introduce_add_bookmark)
        )
    }

    private lateinit var tagEditPop: TagEditPopup
    private lateinit var baseTagEditPop: BasePopupView
    private lateinit var categoryEditPop: CategoryEditPopup
    private lateinit var baseCategoryEditPop: BasePopupView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            mId = it.getString(ARG_ARCHIVE_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentIntroduceBinding.inflate(inflater)

        mId?.let {
            initView(it)
            initPopup(it)
        }
        return binding.root
    }

    private fun initView(id: String) {
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
        binding.categoryViewer.setOnTagSelectedListener { header, content ->
            // TODO: 跳转MainActivity
        }
        binding.startRead.setOnClickListener {
            val intent = Intent(requireContext(), ReaderActivity::class.java)
            intent.putExtra(INTENT_KEY_ARCID, mId)
            // 存储的是page(1开始计数), 阅读页用的是pos(0开始计数)
            mArchive?.progress?.let { p -> intent.putExtra(INTENT_KEY_POS, p - 1) }
            startActivity(intent)
        }
        binding.bookmark.setOnClickListener {
            val isBookmarked = binding.bookmark.text.toString() == getString(R.string.detail_introduce_cancel_bookmark)
            val fStatus = !isBookmarked
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    LanraragiDB.updateBookmark(id, fStatus)
                }
                binding.bookmark.text = bookmarkTextMap[fStatus]
            }
        }
    }

    private fun initPopup(id: String) {
        tagEditPop = TagEditPopup(requireContext())
        tagEditPop.setOnConfirmClickListener { tags ->
            lifecycleScope.launch {
                val isSuccess = withContext(Dispatchers.IO) {
                    NewHttpHelper.updateArchiveTag(id, tags)
                }
                if (!isSuccess) {
                    toast(R.string.detail_tag_failed)
                    return@launch
                }
                withContext(Dispatchers.IO) {
                    LanraragiDB.updateTags(id, tags)
                }
                mArchive?.tags = tags
                binding.tageViewer.setTags(tags)
            }
        }
        baseTagEditPop = XPopup.Builder(requireContext())
            .isDestroyOnDismiss(false)
            .asCustom(tagEditPop)
        categoryEditPop = CategoryEditPopup(requireContext(), id)
        categoryEditPop.setOnDismissListener {
            lifecycleScope.launch {
                val categories = withContext(Dispatchers.IO) {
                    LanraragiDB.queryCategoriesById(id)
                }
                binding.categoryViewer.setCategories(categories)
            }
        }
        baseCategoryEditPop = XPopup.Builder(requireContext())
            .isDestroyOnDismiss(false)
            .asCustom(categoryEditPop)
    }

    override fun lazyLoad() {
        super.lazyLoad()
        mId?.let { initData(it) }
    }

    override fun onResume() {
        super.onResume()
        mId?.let { refreshBookmark(it) }
        mId?:let { requireActivity().supportStartPostponedEnterTransition() }
    }

    private fun refreshBookmark(id: String) {
        lifecycleScope.launch {
            val isBookmark = withContext(Dispatchers.IO) {
                LanraragiDB.isBookmarked(id)
            }
            binding.bookmark.text = bookmarkTextMap[isBookmark]
        }
    }

    private fun initData(id: String) {
        lifecycleScope.launch {
            val archive = withContext(Dispatchers.IO) {
                LanraragiDB.queryArchiveById(id)
            }
            if (archive == null) {
                DebugLog.e("IntroduceFragment: 数据库中不存在此数据: $id")
                return@launch
            }

            val categories = withContext(Dispatchers.IO) {
                LanraragiDB.queryCategoriesById(id)
            }

            mArchive = archive
            binding.title.text = archive.title
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
            binding.categoryViewer.apply {
                setTitle(getString(R.string.category_view_title))
                setCategories(categories)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(id: String) = IntroduceFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_ARCHIVE_ID, id)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_detail_introduce, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (AppConfig.serverSecretKey.isBlank()) {
            menu.removeItem(R.id.editTags)
            menu.removeItem(R.id.editCategory)
        }
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
                    ImageLoad.Builder(requireContext())
                        .loadThumb(it)
                        .isIgnoreDiskCache(true)
                        .into(binding.cover)
                        .execute()
                }
            }
            R.id.editTags -> {
                mArchive?.tags?.let { tags ->
                    baseTagEditPop.doOnAttach { tagEditPop.setTags(tags) }
                    baseTagEditPop.show()
                }
            }
            R.id.editCategory -> {
                lifecycleScope.launch {
                    val categories = withContext(Dispatchers.IO) {
                        LanraragiDB.queryAllCategories()
                    }
                    baseCategoryEditPop.doOnAttach { categoryEditPop.setCategories(categories) }
                    baseCategoryEditPop.show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}