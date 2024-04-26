package com.sena.lanraragi.ui.setting

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.sena.lanraragi.BaseActivity
import com.sena.lanraragi.R
import com.sena.lanraragi.databinding.ActivityOpenLicenseBinding
import com.sena.lanraragi.databinding.ItemOpenLicenseBinding
import com.sena.lanraragi.utils.getOrNull
import java.io.BufferedReader
import java.io.InputStreamReader

class OpenLicenseActivity : BaseActivity() {

    private lateinit var binding: ActivityOpenLicenseBinding
    private val dataList: List<List<String>> = listOf(
        listOf("This software developed by the Android Open Source Project (AOSP).", "", "aospLicense.txt"),
        listOf("OkHttp", "https://github.com/square/okhttp", "okhhtpLicense.txt"),
        listOf("BaseRecyclerViewAdapterHelper4", "https://github.com/CymChad/BaseRecyclerViewAdapterHelper", "adapterLicense.txt"),
        listOf("Subsampling Scale Image View", "https://github.com/davemorrissey/subsampling-scale-image-view", "scaleLicense.txt"),
        listOf("PhotoView", "https://github.com/Baseflow/PhotoView", "photoLicense.txt"),
        listOf("XPopup", "https://github.com/junixapp/XPopup", "xpopupLicense.txt"),
        listOf("Coil", "https://github.com/coil-kt/coil", "coilLicense.txt")
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenLicenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        setNavigation(R.drawable.ic_arrow_back_24) {
            onBackPressedDispatcher.onBackPressed()
        }
        setAppBarText(getString(R.string.setting_source_licenses_title), null)
        val mAdapter = OpenLicenseAdapter()
        mAdapter.addOnItemChildClickListener(R.id.author) { a, _, p ->
            val url = a.getItem(p)?.getOrNull(2) ?: return@addOnItemChildClickListener
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            val chooser = Intent.createChooser(intent, "Open with")
            startActivity(chooser)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@OpenLicenseActivity)
            adapter = mAdapter
        }
        mAdapter.submitList(dataList)
    }



    class OpenLicenseAdapter : BaseQuickAdapter<List<String>, OpenLicenseAdapter.VH>() {

        override fun onBindViewHolder(holder: VH, position: Int, item: List<String>?) {
            item?.let { holder.bind(context, it) }
        }

        override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
            return VH(ItemOpenLicenseBinding.inflate(LayoutInflater.from(context), parent, false))
        }

        override fun getItemViewType(position: Int, list: List<List<String>>): Int {
            return position
        }

        class VH(private val binding: ItemOpenLicenseBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(context: Context, dataList: List<String>) {
                binding.title.text = dataList[0]
                if (dataList[1].isBlank()) {
                    binding.author.visibility = View.GONE
                } else {
                    binding.author.text = SpannableString(dataList[1]).apply {
                        setSpan(UnderlineSpan(),0, dataList[1].length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                val s = getOrNull { BufferedReader(InputStreamReader(context.assets.open(dataList[2]))).readText() }
                s?.let { binding.license.text = it }
            }
        }
    }
}