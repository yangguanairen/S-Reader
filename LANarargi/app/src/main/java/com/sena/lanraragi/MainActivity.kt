package com.sena.lanraragi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.sena.lanraragi.database.LanraragiDB
import com.sena.lanraragi.testpaging.Injection
import com.sena.lanraragi.databinding.ActivityMainBinding
import com.sena.lanraragi.testpaging.ArchiveAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var vm: MainVM
    private lateinit var adapter: ArchiveAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vm = viewModels<MainVM>(
            factoryProducer = {
                Injection.provideViewModelFactory(this)
            }
        ).value
        adapter = ArchiveAdapter(this)
        LanraragiDB.DBHelper.init(this)

        initView()
        initScope()
        initData()

    }


    private fun initView() {
        setSupportActionBar(binding.contentMain.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_24)
            title = "LANraragi"
        }
        binding.contentMain.toolbar.inflateMenu(R.menu.menu_main)
        binding.contentMain.toolbar.setNavigationOnClickListener {
//            binding.drawerLayout.openDrawer(binding.rightNav)
        }



        binding.contentMain.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.contentMain.recyclerView.adapter = adapter
    }

    private fun initScope() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.items.collectLatest {
                    adapter.submitData(it)
                }
            }
        }

        vm.filterOrder.observe(this) {
            binding.rightNav
            adapter.refresh()
        }
        vm.filterSort.observe(this) {
            adapter.refresh()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collect {
                    println("加载更多")
                }
            }
        }
    }

    private fun initData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                vm.initData()
//                adapter.refresh()
            }
        }
    }


    override fun onResume() {
        super.onResume()
//        https://sugoi.gitbook.io/lanraragi/v/dev/api-documentation/archive-api
//        https://developer.android.google.cn/codelabs/android-paging-basics?hl=zh_cn#4
//        ArchiveListVM().insertAll(this)
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