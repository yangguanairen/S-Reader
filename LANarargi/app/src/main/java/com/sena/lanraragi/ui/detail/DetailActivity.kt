package com.sena.lanraragi.ui.detail

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.sena.lanraragi.BaseActivity
import com.sena.lanraragi.BaseFragment
import com.sena.lanraragi.R
import com.sena.lanraragi.databinding.ActivityDetailBinding

class DetailActivity : BaseActivity() {

    private lateinit var binding: ActivityDetailBinding

    private var arcId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arcId = intent.getStringExtra("arcId")

       initView()
    }

    private fun initView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
            title = "详细"
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }


        val fragments = arrayListOf(
            IntroduceFragment.newInstance(arcId).apply { setOnResumeListener {
                binding.toolbar.title = it[0]
                binding.toolbar.subtitle = null
            }},
            PreviewFragment.newInstance(arcId).apply { setOnResumeListener {
                binding.toolbar.title = it[0]
                binding.toolbar.subtitle = it[1] + "页"
            }}
        )
        val tabTitles = arrayListOf("详细", "预览图")
        binding.viewPager.currentItem = 1
        binding.viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            override fun getCount(): Int {
                return tabTitles.size
            }

            override fun getItem(position: Int): Fragment {

                return fragments[position]
            }

            override fun getPageTitle(position: Int): CharSequence {
                return tabTitles[position]
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {

            }
        }
        binding.tableLayout.setupWithViewPager(binding.viewPager)
    }
}