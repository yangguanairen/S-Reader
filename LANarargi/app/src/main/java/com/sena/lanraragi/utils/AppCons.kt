package com.sena.lanraragi.utils


/**
 * FileName: AppCons
 * Author: JiaoCan
 * Date: 2024/4/19
 */


const val INTENT_KEY_ARCHIVE = "intent_key_archive"
const val INTENT_KEY_ARCID = "intent_key_arcid"
const val INTENT_KEY_POS = "intent_key_pos"
const val INTENT_KEY_QUERY = "intent_key_query"
const val INTENT_KEY_OPERATE = "intent_key_operate"
const val OPERATE_KEY_VALUE1 = "openHostPopup"


const val COVER_SHARE_ANIMATION = "cover_share_animation"


enum class ScaleType {
    FIT_PAGE,
    FIT_WIDTH,
    FIT_HEIGHT,
    WEBTOON
}

enum class TouchZone {
    Left,
    Right,
    Center
}

enum class PosSource {
    ViewPager,
    Webtoon,
    Seekbar,
    PreviewFragment,
    Other
}



