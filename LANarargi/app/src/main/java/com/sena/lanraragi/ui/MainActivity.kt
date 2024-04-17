package com.sena.lanraragi.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.recyclerview.widget.LinearLayoutManager
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.databinding.ActivityMainBinding
import com.sena.lanraragi.BaseActivity
import com.sena.lanraragi.R
import com.sena.lanraragi.ui.detail.DetailActivity
import com.sena.lanraragi.ui.setting.SettingActivity

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var vm: MainVM
    private lateinit var adapter: MainAdapter


    private lateinit var settingLayout: LinearLayout

    private lateinit var sortTimeButton: RadioButton
    private lateinit var sortTitleButton: RadioButton
    private lateinit var orderAscButton: RadioButton
    private lateinit var orderDescButton: RadioButton

    private var queryFromDetail: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vm = MainVM()

        queryFromDetail = intent.getStringExtra("query")

        initView()
        initViewModel()
        initData()


    }


    private fun initView() {

        setAppBarText("LANraragi", null)
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
        }
        settingLayout.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRightNavigationView() {
        binding.rightNav.getHeaderView(0).apply {
            sortTimeButton = findViewById(R.id.sortTime)
            sortTitleButton = findViewById(R.id.sortTitle)
            orderAscButton = findViewById(R.id.orderAsc)
            orderDescButton = findViewById(R.id.orderDesc)
        }

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

        binding.contentMain.searchView.setOnAfterInputFinishListener {
            if (it.isBlank()) return@setOnAfterInputFinishListener
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

        }
        binding.contentMain.random.setOnClickListener {

        }


        adapter = MainAdapter()
        binding.contentMain.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.contentMain.recyclerView.adapter = adapter
        adapter.setOnItemClickListener { _, _, p ->
            val itemData = adapter.getItem(p)
            if (itemData != null) {
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("archive", itemData)
                startActivity(intent)
            }

        }
    }

    private fun initViewModel() {
        vm.serverArchiveCount.observe(this) { n: Int? ->
            binding.contentMain.toolbar.subtitle = "${n ?: 0}个档案"
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
            adapter.submitList(it)
        }
        vm.queryText.observe(this) {
//            binding.contentMain.searchView.setText(it)
        }
    }

    private fun initData() {
        if (queryFromDetail != null) {
            binding.contentMain.searchView.setText(queryFromDetail!!)
        } else {
            vm.initData(LanraragiDB.DBHelper.SORT.TIME, LanraragiDB.DBHelper.ORDER.DESC)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                true
            }

            R.id.filter -> {
                binding.drawerLayout.openDrawer(binding.rightNav)
                true
            }
            R.id.more -> {
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }
}