package com.sena.lanraragi.database.bookmarkData

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


/**
 * FileName: BookmarkDao
 * Author: JiaoCan
 * Date: 2024/5/1
 */

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(bookmark: Bookmark)

    @Query("DELETE FROM Bookmark WHERE id = :id")
    fun remove(id: String)

    @Query("SELECT * FROM Bookmark WHERE serverHost = :apiHost")
    fun queryByServerHost(apiHost: String): List<Bookmark>

    @Query("SELECT * FROM Bookmark WHERE id = :id")
    fun queryById(id: String): Bookmark?

    @Query("UPDATE Bookmark SET progress = :progress WHERE id = :id")
    fun updateProgress(id: String, progress: Int)
}

