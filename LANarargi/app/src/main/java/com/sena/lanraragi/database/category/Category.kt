package com.sena.lanraragi.database.category

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson


@Entity(tableName = "Category")
data class Category(
    @PrimaryKey
    val id: String,
    val name: String,
    val archives: List<String>,
    val lastUsed: Int,
    val pinned: Int,
    val search: String
) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
