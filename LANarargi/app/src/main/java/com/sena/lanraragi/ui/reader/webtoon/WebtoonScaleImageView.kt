package com.sena.lanraragi.ui.reader.webtoon

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

/**
 * FileName: WebtoonScaleImageView
 * Author: JiaoCan
 * Date: 2024/4/21
 */

class WebtoonScaleImageView(context: Context): SubsamplingScaleImageView(context) {

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }
}