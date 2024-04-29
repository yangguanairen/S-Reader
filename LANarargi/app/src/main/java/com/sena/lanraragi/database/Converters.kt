package com.sena.lanraragi.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


/**
 * FileName: Converters
 * Author: JiaoCan
 * Date: 2024/4/28
 */

class Converters {

    @TypeConverter
    fun fromString(s: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(s, type)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return Gson().toJson(list)
    }

}

