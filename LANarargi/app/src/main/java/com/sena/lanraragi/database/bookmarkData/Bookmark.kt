package com.sena.lanraragi.database.bookmarkData

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Bookmark")
data class Bookmark(
    @PrimaryKey
    val id: String,
    val serverHost: String,
    val title: String?,
    val tags: String?,
    val progress: Int?
)
