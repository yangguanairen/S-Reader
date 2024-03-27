package com.sena.lanraragi.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioButton
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.databinding.ActivityMainBinding
import com.sena.lanraragi.BaseActivity
import com.sena.lanraragi.R
import com.sena.lanraragi.ui.detail.DetailActivity
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var vm: MainVM
    private lateinit var adapter: MainAdapter


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
        setSupportActionBar(binding.contentMain.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_24)
            title = "LANraragi"
        }

        binding.contentMain.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(binding.leftNav)
        }

        initRightNavigationView()
        initContentView()
    }

    private fun initRightNavigationView() {
        binding.rightNav.getHeaderView(0).apply {
            sortTimeButton = findViewById(R.id.sortTime)
            sortTitleButton = findViewById(R.id.sortTitle)
            orderAscButton = findViewById(R.id.orderAsc)
            orderDescButton = findViewById(R.id.orderDesc)
        }

        sortTimeButton.setOnClickListener {
            lifecycleScope.launch {
                vm.setSort(LanraragiDB.DBHelper.SORT.TIME)
            }
        }
        sortTitleButton.setOnClickListener {
            lifecycleScope.launch {
                vm.setSort(LanraragiDB.DBHelper.SORT.TITLE)
            }
        }
        orderAscButton.setOnClickListener {
            lifecycleScope.launch {
                vm.setOrder(LanraragiDB.DBHelper.ORDER.ASC)
            }
        }
        orderDescButton.setOnClickListener {
            lifecycleScope.launch {
                vm.setOrder(LanraragiDB.DBHelper.ORDER.DESC)
            }
        }
    }

    private fun initContentView() {

        binding.contentMain.searchView.setOnAfterInputFinishListener {
            if (it.isBlank()) return@setOnAfterInputFinishListener
            lifecycleScope.launch {
                vm.queryFromServer(it)
            }
        }
        binding.contentMain.searchView.setOnClearTextListener {
            lifecycleScope.launch {
                vm.queryFromDB("")
            }
        }
        binding.contentMain.searchView.setOnSearchDoneListener {
            lifecycleScope.launch {
                vm.queryFromServer(it)
            }
        }


        binding.contentMain.isNew.setOnClickListener {

        }
        binding.contentMain.random.setOnClickListener {

        }


        adapter = MainAdapter()
        binding.contentMain.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.contentMain.recyclerView.adapter = adapter
        adapter.setOnItemClickListener { a, v, p ->
            val itemData = adapter.getItem(p)
            if (itemData != null) {
                val arcId = itemData.arcid
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("arcId", arcId)
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
            }
        }
        vm.dataList.observe(this) {
            adapter.submitList(it)
        }
    }

    private fun initData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (!queryFromDetail.isNullOrBlank()) {
                    vm.queryFromServer(queryFromDetail!!)
                } else {
                    vm.initData()
                    vm.setOrder(LanraragiDB.DBHelper.ORDER.DESC)
                    vm.setSort(LanraragiDB.DBHelper.SORT.TIME)
                }

            }
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