package com.sena.lanraragi.ui.reader

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import coil.load
import coil.size.ViewSizeResolver
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.BaseFragment
import com.sena.lanraragi.R
import com.sena.lanraragi.databinding.FragmentReaderPageBinding
import com.sena.lanraragi.utils.DebugLog
import com.sena.lanraragi.utils.NewHttpHelper
import com.sena.lanraragi.utils.ScaleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception


/**
 * FileName: ReaderPageFragment
 * Author: JiaoCan
 * Date: 2024/4/24
 */

const val ARG_URL_LIST = "arg_url_list"

class ReaderPageFragment : BaseFragment() {

    private var mOnAttachListener: OnClickListener? = null
    private var mOnDetachListener: OnClickListener? = null
    private var mOnTapListener: OnClickListener? = null
    private var mOnLongPressListener: OnLongClickListener? = null

    private val firstScaleType = AppConfig.scaleMethod
    private val urlList: ArrayList<String> = arrayListOf()

    private lateinit var binding: FragmentReaderPageBinding
    private var mainView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getStringArray(ARG_URL_LIST)?.let {
            urlList.addAll(it)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentReaderPageBinding.inflate(inflater, container, false)
        // 自适应宽高且不响应触摸事件
        if (firstScaleType == ScaleType.WEBTOON) {
            binding.rootLayout.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            binding.rootLayout.enableTouch = false
        }
        binding.rootLayout.apply {
            setOnClickListener { mOnTapListener?.onClick(this) }
            setOnLongPressListener { mOnLongPressListener?.onLongClick(this) == true }
        }

        return binding.root
    }

//    override fun lazyLoad() {
//        super.lazyLoad()
//        test()
//    }

    fun setUrl(url: String) {
        urlList.add(url)
        test()
    }

    fun testGetView(): View = binding.root


    private fun test() {
        val picUrl = urlList.firstOrNull()
        if (picUrl == null) {
            DebugLog.e("无可展示的图片链接")
            return
        }

        lifecycleScope.launch {
            val id = Regex("api/archives/[a-z0-9]+/page").find(picUrl)?.value?.split(Regex("/"))?.getOrNull(2)
            val fileName = Regex("path=.*").find(picUrl)?.value?.replace("path=", "")
            if (id.isNullOrBlank() || fileName.isNullOrBlank()) {
                return@launch
            }

            val dir = File(requireContext().externalCacheDir, "archive/$id")
            if (!dir.exists()) dir.mkdirs()
            // 文件名携带"/"符号会自动生成父级文件夹
            val finFileName = fileName.replace("/", "_")
            var isExists = dir.listFiles()?.any { it.name == finFileName } == true
            val path = dir.absolutePath + "/$finFileName"

            val result = withContext(Dispatchers.IO) {
                if (!isExists) {
                    kotlin.runCatching {
                        NewHttpHelper.downloadFile(picUrl, path)
                        true
                    }.getOrNull()
                }
            }
            displayImage(path)
        }
    }


    private fun displayImage(filePath: String) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        when (options.outMimeType.lowercase()) {
            "image/png", "image/jpeg", "image/jpg", "image/webp" -> showCustomPic(filePath)
            "image/gif" -> showGif(filePath)
            else -> showErrorView()
        }
    }

    private fun showGif(filePath: String) {
        mainView = PhotoView(context).also {
            initView(it)

            val imageLoader = ImageLoader.Builder(requireContext())
                .components {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(coil.decode.GifDecoder.Factory())
                    }
                }
                .build()

            it.load(filePath, imageLoader = imageLoader) {
                crossfade(true)
                listener(
                    onSuccess = { _, _ ->
                        binding.progressBar.visibility = View.GONE
                        view?.run {
                            setOnClickListener(null)
                            setOnLongClickListener(null)
                        }
                        updateScaleType(it, firstScaleType)
                    },
                    onError = { _, _ ->
                        showErrorView()
                        it.visibility = View.GONE
                    }
                )
                error(R.drawable.bg_error)
                placeholder(R.drawable.bg_placeholder)
                size(ViewSizeResolver(it))
            }
        }
    }

    private fun showCustomPic(filePath: String) {
        mainView = (if (firstScaleType == ScaleType.WEBTOON) {
            SubsamplingScaleImageView(context)
        } else {
            WebtoonScaleImageView(requireContext())
        }).also {
            initView(it)
            it.setOnImageEventListener(object : SubsamplingScaleImageView.DefaultOnImageEventListener() {
                override fun onReady() {
                    super.onReady()
                    binding.progressBar.visibility = View.GONE
                    view?.run {
                        setOnClickListener(null)
                        setOnLongClickListener(null)
                        updateScaleType(it, firstScaleType)
                    }
                }
                override fun onImageLoadError(e: Exception?) {
                    super.onImageLoadError(e)
                    showErrorView()
                    it.visibility = View.GONE
                }
            })
            it.setImage(ImageSource.uri(filePath))
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView(view: View) {
        view.layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            if (firstScaleType == ScaleType.WEBTOON) {
                RelativeLayout.LayoutParams.WRAP_CONTENT
            } else {
                RelativeLayout.LayoutParams.MATCH_PARENT
            }
        )
        if (firstScaleType != ScaleType.WEBTOON) {
            if (view is SubsamplingScaleImageView) {
                val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        mOnTapListener?.onClick(view)
                        return true
                    }
                })
                view.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
            } else if (view is PhotoView) {
                view.setOnViewTapListener { _, _, _ -> mOnTapListener?.onClick(view) }
            }
        }
        view.setOnLongClickListener { mOnLongPressListener?.onLongClick(view) == true }
        binding.rootLayout.addView(view)
    }


    private fun showErrorView() {
        binding.errorView.apply {
            setOnClickListener { mOnTapListener?.onClick(this) }
            setOnLongPressListener { mOnLongPressListener?.onLongClick(this) == true }
            visibility = View.VISIBLE
        }
    }





    fun onScaleTypeChange(scaleType: ScaleType) {
        updateScaleType(mainView, scaleType)
    }

    private fun updateScaleType(imageView: View?, scaleType: ScaleType) {
        when (imageView) {
            is SubsamplingScaleImageView -> {
                imageView.setMinimumScaleType(when (scaleType) {
                    ScaleType.FIT_WIDTH -> SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
                    ScaleType.FIT_HEIGHT -> SubsamplingScaleImageView.SCALE_TYPE_START
                    ScaleType.FIT_PAGE -> SubsamplingScaleImageView.SCALE_TYPE_CUSTOM
                    ScaleType.WEBTOON -> SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
                })
            }
            is PhotoView -> {
                imageView.scaleType = when (firstScaleType) {
                    ScaleType.FIT_WIDTH -> ImageView.ScaleType.FIT_CENTER
                    ScaleType.FIT_HEIGHT -> ImageView.ScaleType.FIT_START
                    ScaleType.FIT_PAGE -> ImageView.ScaleType.CENTER
                    ScaleType.WEBTOON -> ImageView.ScaleType.FIT_CENTER
                }
            }
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateScaleType(mainView, AppConfig.scaleMethod)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mOnAttachListener?.onClick(null)
    }

    override fun onDetach() {
        super.onDetach()
        mOnDetachListener?.onClick(null)
    }


    fun setOnAttachListener(func: () -> Unit) {
        mOnAttachListener = OnClickListener { func.invoke() }
    }

    fun setOnDetachListener(func: () -> Unit) {
        mOnDetachListener = OnClickListener { func.invoke() }
    }

    fun setOnTapListener(func: () -> Unit) {
        mOnTapListener = OnClickListener { func.invoke() }
    }

    fun setOnLongPressListener(func: () -> Boolean) {
        mOnLongPressListener = OnLongClickListener { func.invoke() }
    }

    fun newInstance(urlList: List<String>) = ReaderPageFragment().apply {
        arguments = Bundle().apply {
            putStringArray(ARG_URL_LIST, urlList.toTypedArray())
        }
    }

}

