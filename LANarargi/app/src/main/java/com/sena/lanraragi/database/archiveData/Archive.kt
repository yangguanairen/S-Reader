package com.sena.lanraragi.database.archiveData

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Archive")
data class Archive(

    @PrimaryKey
    val arcid: String,
    val isnew: Boolean?,
    val extension: String?,
    val pagecount: Int?,
    val progress: Int?,
    val tags: String?,
    val lastreadtime: Long?,
    val title: String?,
    val data_added: Long?
)
