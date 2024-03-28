package com.sena.lanraragi.ui.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayoutMediator
import com.sena.lanraragi.BaseActivity
import com.sena.lanraragi.R
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.databinding.ActivityDetailBinding
import com.sena.lanraragi.utils.getOrNull

class DetailActivity : BaseActivity() {

    private lateinit var binding: ActivityDetailBinding

    private var mArchive: Archive? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mArchive = getOrNull { intent.getSerializableExtra("archive") as Archive }

        mArchive?.let { initView(it) }
    }

    private fun initView(archive: Archive) {
        setNavigation(R.drawable.ic_arrow_back_24) { finish() }

        val fragments = arrayListOf(
            IntroduceFragment.newInstance(archive),
            PreviewFragment.newInstance(archive)
        )

        val tabTitles = arrayListOf("详细", "预览图")
        binding.viewPager.currentItem = 1
        binding.viewPager.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
            override fun getItemCount(): Int {
                return fragments.size
            }

            override fun createFragment(position: Int): Fragment {
                return fragments[position]
            }

        }
        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> setAppBarText(tabTitles[position], null)
                    1 -> {
                        val subtitle = if (archive.pagecount == null) null else "${archive.pagecount}页"
                        setAppBarText(tabTitles[position], subtitle)
                    }
                }
            }
        })
        // tabLayout和viewPager2联动
        // https://www.jianshu.com/p/0cde01392eb0
        TabLayoutMediator(binding.tableLayout, binding.viewPager) {
            tab, position -> tab.text = tabTitles[position]
        }.attach()
    }
}