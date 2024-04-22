package com.sena.lanraragi.database.archiveData

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

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
    val data_added: Long?,
    var isBookmark: Boolean = false
) : Serializable {
    override fun toString(): String {
        return "arcid: $arcid, title: $title, data_added: $data_added, isnew: $isnew, extension: $extension, pagecount: $pagecount, progress: $progress, tags: $tags, lastereadtime: $lastreadtime, isBookmark: $isBookmark"
    }

}






