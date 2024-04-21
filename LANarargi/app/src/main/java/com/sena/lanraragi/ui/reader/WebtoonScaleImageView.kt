package com.sena.lanraragi.ui.reader

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView


/**
 * FileName: WebtoonScaleImageView
 * Author: JiaoCan
 * Date: 2024/4/21
 */

class WebtoonScaleImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet, defStyle: Int = 0
) : SubsamplingScaleImageView(context, attrs) {


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }
}

