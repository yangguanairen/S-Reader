package com.sena.lanraragi.ui.reader

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.chad.library.adapter4.BaseQuickAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sena.lanraragi.BaseActivity
import com.sena.lanraragi.R
import com.sena.lanraragi.databinding.ActivityReaderBinding
import com.sena.lanraragi.databinding.NavReaderBottomBinding
import com.sena.lanraragi.utils.DebugLog
import kotlinx.coroutines.launch

class ReaderActivity : BaseActivity() {

    private lateinit var binding: ActivityReaderBinding

    private var arcId: String? = null
    private lateinit var vm: ReaderVM
    private lateinit var adapter: ReaderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arcId = intent.getStringExtra("arcId")
        vm = ReaderVM()

        initView()
        initVM()
        arcId?.let { initData(it) }
    }

    private fun initView() {

        setNavigation(R.drawable.ic_arrow_back_24) {
            finish()
        }
        setAppBarText("LANraragi", null)

        binding.contentReader.appBar.visibility = View.INVISIBLE

        initBottomNavigationView()


        // viewPager2详解
        // https://blog.51cto.com/u_13303/6872084
        adapter = ReaderAdapter()
        binding.contentReader.viewPager.adapter = adapter
        adapter.setOnItemLongClickListener { adapter, view, position ->
            showBottomDialog()
            true
        }
        adapter.addOnItemChildClickListener(R.id.imageView) { adapter, view, position ->

            binding.contentReader.appBar.apply {
                if (visibility == View.VISIBLE) {
                    visibility = View.INVISIBLE
                } else {
                    val cPos = binding.contentReader.viewPager.currentItem
                    val count = adapter.itemCount
                    setAppBarText("fjsdlfjlsdjl", "${count - cPos}/${count}页")
                    visibility = View.VISIBLE
                }
            }
        }
        adapter.addOnItemChildLongClickListener(R.id.imageView) { adapter, view, position ->
            showBottomDialog()
            true
        }

        binding.contentReader.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val cPos = binding.contentReader.viewPager.currentItem
                val count = adapter.itemCount
                setAppBarText("fjsdlfjlsdjl", "${count - cPos}/${count}页")
            }
        })
    }

    private fun initBottomNavigationView() {


    }


    private fun initData(id: String) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.initData(id)
            }
        }
    }

    private fun initVM() {
        vm.archive.observe(this) {

        }
        vm.pages.observe(this) {
            adapter.submitList(it.reversed())
            binding.contentReader.viewPager.setCurrentItem(it.size - 1, false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_reader, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.setting -> {
                showBottomDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showBottomDialog() {
        val dialog = BottomSheetDialog(this, R.style.reader_nav_dialog)
        val view = NavReaderBottomBinding.inflate(layoutInflater).root
        dialog.setContentView(view)
        dialog.show()
    }
}