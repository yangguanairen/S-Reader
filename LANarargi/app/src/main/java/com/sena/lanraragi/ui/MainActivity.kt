package com.sena.lanraragi.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.databinding.ActivityMainBinding
import com.sena.lanraragi.R
import com.sena.lanraragi.ui.detail.DetailActivity
import com.sena.lanraragi.ui.random.RandomActivity
import com.sena.lanraragi.ui.setting.SettingActivity
import com.sena.lanraragi.ui.widet.BookmarkView
import com.sena.lanraragi.utils.INTENT_KEY_ARCHIVE
import com.sena.lanraragi.utils.INTENT_KEY_OPERATE
import com.sena.lanraragi.utils.INTENT_KEY_POS
import com.sena.lanraragi.utils.INTENT_KEY_QUERY
import com.sena.lanraragi.utils.OPERATE_KEY_VALUE1
import com.sena.lanraragi.utils.getThemeColor
import kotlinx.coroutines.launch

class MainActivity : BaseArchiveListActivity(R.menu.menu_main) {

    private lateinit var binding: ActivityMainBinding

    private val vm: MainVM by viewModels()

    private lateinit var settingLayout: LinearLayout
    private lateinit var bookmarkView: BookmarkView
    private lateinit var sortTimeButton: RadioButton
    private lateinit var sortTitleButton: RadioButton
    private lateinit var orderAscButton: RadioButton
    private lateinit var orderDescButton: RadioButton

    private var queryFromDetail: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        queryFromDetail = intent.getStringExtra(INTENT_KEY_QUERY)

        initViewModel()
        initView()
    }

    override fun onResume() {
        super.onResume()
        if (AppConfig.serverHost.isBlank()) {
            binding.contentMain.errorLayout.visibility = View.VISIBLE
            binding.contentMain.listLayout.visibility = View.INVISIBLE
        } else {
            binding.contentMain.errorLayout.visibility = View.INVISIBLE
            binding.contentMain.listLayout.visibility = View.VISIBLE
            initData()
        }
    }

    private fun initView() {
        setAppBarText(getString(R.string.main_toolbar_title), null)
        setNavigation(if (queryFromDetail != null) R.drawable.ic_arrow_back_24 else R.drawable.ic_menu_24) {
            if (queryFromDetail != null) {
                finish()
            } else {
                binding.drawerLayout.openDrawer(binding.leftNav)
            }
        }

        initLeftNavigationView()
        initRightNavigationView()
        initContentView()
    }

    private fun initLeftNavigationView() {
        binding.leftNav.getHeaderView(0).apply {
            settingLayout = findViewById(R.id.settingLayout)
            bookmarkView = findViewById(R.id.bookmarkView)
        }
        settingLayout.setOnClickListener {
            binding.drawerLayout.closeDrawer(binding.leftNav)
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
        bookmarkView.setOnItemClickListener { a, _, p ->
            a.getItem(p)?.let {
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra(INTENT_KEY_ARCHIVE, it)
                startActivity(intent)
            }
        }
        bookmarkView.setOnTagSelectedListener { header, content ->
            binding.drawerLayout.closeDrawer(binding.leftNav)
            onTagSelected(header, content)
        }
    }

    private fun initRightNavigationView() {
        binding.rightNav.getHeaderView(0).apply {
            sortTimeButton = findViewById(R.id.sortTime)
            sortTitleButton = findViewById(R.id.sortTitle)
            orderAscButton = findViewById(R.id.orderAsc)
            orderDescButton = findViewById(R.id.orderDesc)
        }
        sortTimeButton.isChecked = AppConfig.sort == LanraragiDB.DBHelper.SORT.TIME
        sortTitleButton.isChecked = AppConfig.sort == LanraragiDB.DBHelper.SORT.TITLE
        orderAscButton.isChecked = AppConfig.order == LanraragiDB.DBHelper.ORDER.ASC
        orderDescButton.isChecked = AppConfig.order == LanraragiDB.DBHelper.ORDER.DESC

        sortTimeButton.setOnClickListener {
            vm.setSort(LanraragiDB.DBHelper.SORT.TIME)
        }
        sortTitleButton.setOnClickListener {
            vm.setSort(LanraragiDB.DBHelper.SORT.TITLE)
        }
        orderAscButton.setOnClickListener {
            vm.setOrder(LanraragiDB.DBHelper.ORDER.ASC)
        }
        orderDescButton.setOnClickListener {
            vm.setOrder(LanraragiDB.DBHelper.ORDER.DESC)
        }
    }

    private fun initContentView() {
        binding.contentMain.errorLayout.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            intent.putExtra(INTENT_KEY_OPERATE, OPERATE_KEY_VALUE1)
            startActivity(intent)
        }

        binding.contentMain.searchView.setOnAfterInputFinishListener {
            vm.setQueryText(it)
        }
        binding.contentMain.searchView.setOnClearTextListener {
            vm.setQueryText("")
        }
        binding.contentMain.searchView.setOnSearchDoneListener {
            vm.setQueryText(it)
        }

        if (queryFromDetail != null) {
            binding.contentMain.isNew.visibility = View.GONE
            binding.contentMain.random.visibility = View.GONE
        }

        binding.contentMain.isNew.setOnClickListener {
            vm.setNewState(binding.contentMain.isNew.isChecked)
        }
        binding.contentMain.random.setOnClickListener {
            val count = AppConfig.randomCount
            if (count == 1) {
                lifecycleScope.launch {
                    val result = vm.getSingleRandomArchive()
                    if (result != null) {
                        val i = Intent(this@MainActivity, DetailActivity::class.java)
                        i.putExtra(INTENT_KEY_ARCHIVE, result)
                        startActivity(i)
                    } else {
                        Toast.makeText(this@MainActivity, "无法获取随机档案", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                val intent = Intent(this, RandomActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun initViewModel() {
        vm.serverArchiveCount.observe(this) { n: Int? ->
            binding.contentMain.toolbar.subtitle = String.format(getString(R.string.main_toolbar_subtitle), n ?: 0)
        }
        vm.filterOrder.observe(this) {
            when (it) {
                LanraragiDB.DBHelper.ORDER.ASC -> {
                    orderAscButton.isChecked = true
                    orderDescButton.isChecked = false

                }
                LanraragiDB.DBHelper.ORDER.DESC -> {
                    orderAscButton.isChecked = false
                    orderDescButton.isChecked = true
                }
                else -> {

                }
            }

        }
        vm.filterSort.observe(this) {
            when (it) {
                LanraragiDB.DBHelper.SORT.TIME -> {
                    sortTimeButton.isChecked = true
                    sortTitleButton.isChecked = false
                }
                LanraragiDB.DBHelper.SORT.TITLE -> {
                    sortTimeButton.isChecked = false
                    sortTitleButton.isChecked = true
                }
                else -> {

                }
            }
        }
        vm.dataList.observe(this) {
            mAdapter.submitList(it) {
//                mRecyclerView?.layoutManager?.scrollToPosition(0)
                val historyPos = intent.getIntExtra(INTENT_KEY_POS, -1)
                if (historyPos > -1) {
                    intent.putExtra(INTENT_KEY_POS, -1)
                    mRecyclerView?.layoutManager?.scrollToPosition(historyPos)
                }
            }
        }
        vm.queryText.observe(this) {
            binding.contentMain.searchView.setText(it)
        }
        vm.isNew.observe(this) {
            binding.contentMain.isNew.isChecked = it
        }
    }

    private fun initData() {
        queryFromDetail?.let {
            vm.setQueryText(it)
        }
        queryFromDetail?:let{
            vm.forceRefreshData()
        }
    }

    override fun onTagSelected(header: String, content: String) {
        super.onTagSelected(header, content)
        val query = if (header.isBlank()) content else "$header:$content"
        vm.setQueryText(query)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         when (item.itemId) {
            R.id.refresh -> {
                vm.forceRefreshData()
            }

            R.id.filter -> {
                binding.drawerLayout.openDrawer(binding.rightNav)
            }

            R.id.gotoTop -> {
                mRecyclerView?.layoutManager?.scrollToPosition(0)
            }

            R.id.gotoBottom -> {
                mRecyclerView?.layoutManager?.scrollToPosition(mAdapter.items.size - 1)
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onThemeChanged(theme: Int) {
        super.onThemeChanged(theme)
        // 重启当前活动
        val curPos = (mRecyclerView?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        intent.putExtra(INTENT_KEY_POS, curPos)
        finish()
        overridePendingTransition(0, 0) // 可选，去除动画效果
        startActivity(intent)
        overridePendingTransition(0, 0) // 可选，去除动画效果
    }
}