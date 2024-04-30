package com.sena.lanraragi.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayout
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.databinding.ActivityMainBinding
import com.sena.lanraragi.R
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.database.category.Category
import com.sena.lanraragi.databinding.ItemTagBinding
import com.sena.lanraragi.ui.detail.DetailActivity
import com.sena.lanraragi.ui.random.RandomActivity
import com.sena.lanraragi.ui.setting.SettingActivity
import com.sena.lanraragi.ui.widet.BookmarkView
import com.sena.lanraragi.utils.INTENT_KEY_ARCHIVE
import com.sena.lanraragi.utils.INTENT_KEY_OPERATE
import com.sena.lanraragi.utils.INTENT_KEY_POS
import com.sena.lanraragi.utils.INTENT_KEY_QUERY
import com.sena.lanraragi.utils.OPERATE_KEY_VALUE1
import kotlinx.coroutines.launch

class MainActivity : BaseArchiveListActivity(R.menu.menu_main) {

    private lateinit var binding: ActivityMainBinding

    private val vm: MainVM by viewModels()

    private lateinit var sortTimeButton: RadioButton
    private lateinit var sortTitleButton: RadioButton
    private lateinit var orderAscButton: RadioButton
    private lateinit var orderDescButton: RadioButton
    private lateinit var categoryLayout: FlexboxLayout
    private var forceRefreshButton: MenuItem? = null

    private var lastHost: String = AppConfig.serverHost
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

            if (AppConfig.serverHost != lastHost) { // ServerHost被更新
                lastHost = AppConfig.serverHost
                setAppBarSubtitle(null)
                mAdapter.submitList(emptyList())
                if (!vm.queryText.value.isNullOrBlank()) {
                    callQueryTextChange("")
                } else if (vm.categories.value != null) {
                    callOnCategoryChange(null)
                }
                vm.refreshData(true)
            } else {
                initData()
            }
        }
    }

    private fun initViewModel() {
        vm.filterOrder.observe(this) { onOrderChanged(it) }
        vm.filterSort.observe(this) { onSortChanged(it) }
        vm.dataList.observe(this) { onDataListChanged(it) }
        vm.queryText.observe(this) { onQueryTextChanged(it) }
        vm.isNew.observe(this) { onNewChanged(it) }
        vm.categories.observe(this) { onCategoriesChanged(it) }
        vm.curCategory.observe(this) { onCategoryChanged(it) }
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

    private fun initData() {
        queryFromDetail?.let { callQueryTextChange(it) }
        queryFromDetail?:let { vm.refreshData(false) }
    }

    private fun initLeftNavigationView() {
        val settingLayout: LinearLayout
        val bookmarkView: BookmarkView
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
            categoryLayout = findViewById(R.id.categoryLayout)
            if (queryFromDetail != null) findViewById<LinearLayout>(R.id.categoryRootLayout).apply {
                visibility = View.GONE
            }
        }
        sortTimeButton.isChecked = AppConfig.sort == LanraragiDB.DBHelper.SORT.TIME
        sortTitleButton.isChecked = AppConfig.sort == LanraragiDB.DBHelper.SORT.TITLE
        orderAscButton.isChecked = AppConfig.order == LanraragiDB.DBHelper.ORDER.ASC
        orderDescButton.isChecked = AppConfig.order == LanraragiDB.DBHelper.ORDER.DESC

        sortTimeButton.setOnClickListener { callSortChange(LanraragiDB.DBHelper.SORT.TIME) }
        sortTitleButton.setOnClickListener { callSortChange(LanraragiDB.DBHelper.SORT.TITLE) }
        orderAscButton.setOnClickListener { callOrderChange(LanraragiDB.DBHelper.ORDER.ASC) }
        orderDescButton.setOnClickListener { callOrderChange(LanraragiDB.DBHelper.ORDER.DESC) }
    }

    private fun initContentView() {
        binding.contentMain.errorLayout.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            intent.putExtra(INTENT_KEY_OPERATE, OPERATE_KEY_VALUE1)
            startActivity(intent)
        }
        binding.contentMain.searchView.apply {
            setOnAfterInputFinishListener { callQueryTextChange(it) }
            setOnClearTextListener { callQueryTextChange("") }
            setOnSearchDoneListener { callQueryTextChange(it) }
            setOnRelatedSelectedListener { callQueryTextChange(it) }
        }

        if (queryFromDetail != null) {
            binding.contentMain.isNew.visibility = View.GONE
            binding.contentMain.random.visibility = View.GONE
        }
        binding.contentMain.isNew.setOnClickListener { callOnNewChange((it as CheckBox).isChecked) }
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

    private fun callQueryTextChange(text: String) {
        val isShow = (text.isBlank() && queryFromDetail == null)
        forceRefreshButton?.isVisible = isShow
        intent.putExtra(INTENT_KEY_POS, 0)
        vm.setQueryText(text)
    }

    private fun callSortChange(sort: LanraragiDB.DBHelper.SORT) {
        intent.putExtra(INTENT_KEY_POS, 0)
        vm.setSort(sort)
    }

    private fun callOrderChange(order: LanraragiDB.DBHelper.ORDER) {
        intent.putExtra(INTENT_KEY_POS, 0)
        vm.setOrder(order)
    }

    private fun callOnNewChange(status: Boolean) {
        intent.putExtra(INTENT_KEY_POS, 0)
        vm.setNewState(status)
    }

    private fun callOnCategoryChange(category: Category?) {
        val isShow = (category == null && queryFromDetail == null)
        forceRefreshButton?.isVisible = isShow
        intent.putExtra(INTENT_KEY_POS, 0)
        vm.setCategory(if(vm.curCategory.value?.name == category?.name) null else category)
    }

    private fun onDataListChanged(list: List<Archive>) {
        setAppBarSubtitle(String.format(getString(R.string.main_toolbar_subtitle), list.size))
        mAdapter.submitList(list) {
            val historyPos = intent.getIntExtra(INTENT_KEY_POS, -1)
            if (historyPos > -1) {
                intent.putExtra(INTENT_KEY_POS, -1)
                mRecyclerView?.layoutManager?.scrollToPosition(historyPos)
            }
        }
    }

    private fun onQueryTextChanged(text: String) {
        binding.contentMain.searchView.setText(text)
    }

    private fun onSortChanged(sort: LanraragiDB.DBHelper.SORT) {
        val isTime = sort == LanraragiDB.DBHelper.SORT.TIME
        sortTimeButton.isChecked = isTime
        sortTitleButton.isChecked = !isTime
    }

    private fun onOrderChanged(order: LanraragiDB.DBHelper.ORDER) {
        val isAsc = order == LanraragiDB.DBHelper.ORDER.ASC
        orderAscButton.isChecked = isAsc
        orderDescButton.isChecked = !isAsc
    }

    private fun onNewChanged(status: Boolean) {
        binding.contentMain.isNew.isChecked = status
    }

    private fun onCategoriesChanged(list: List<Category>) {
        categoryLayout.removeAllViews()
        list.forEach { category ->
            val item = ItemTagBinding.inflate(layoutInflater, categoryLayout, true)
            item.textView.apply {
                text = category.name
                theme.getDrawable(R.drawable.bg_category_content)?.let { background = it }
                setOnClickListener { callOnCategoryChange(category) }
            }
        }
    }

    private fun onCategoryChanged(category: Category?) {
        for (i in 0 until categoryLayout.childCount) {
            val tv = categoryLayout.getChildAt(i).findViewById<TextView>(R.id.textView)
            val text = tv.text.toString()
            tv.setTextColor(Color.parseColor(if (text == category?.name) "#22a7f0" else "#ffffff"))
        }
    }

    override fun onTagSelected(header: String, content: String) {
        super.onTagSelected(header, content)
        val query = if (header.isBlank()) content else "$header:$content"
        callQueryTextChange(query)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            forceRefreshButton = it.findItem(R.id.refresh).apply {
                isVisible = queryFromDetail == null
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         when (item.itemId) {
            R.id.refresh -> {
                callOnCategoryChange(null)
                vm.refreshData(true)
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