package com.sena.lanraragi.database.archiveData

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.concurrent.Flow


/**
 * FileName: ArchiveDao
 * Author: JiaoCan
 * Date: 2024/3/22
 */


@Dao
interface ArchiveDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<Archive>)

    @Query("DELETE FROM Archive")
    suspend fun clearTable()

    @Query("SELECT * FROM Archive")
    suspend fun getAll(): List<Archive>


    @Query("SELECT * FROM ARCHIVE ORDER BY title ASC")
    suspend fun getAllOrderByTitleAsc(): List<Archive>

    @Query("SELECT * FROM ARCHIVE ORDER BY title DESC")
    suspend fun getAllOrderByTitleDesc(): List<Archive>

    @Query("SELECT * FROM ARCHIVE ORDER BY data_added ASC")
    suspend fun getAllOrderByTimeAsc(): List<Archive>

    @Query("SELECT * FROM ARCHIVE ORDER BY data_added DESC")
    suspend fun getAllOrderByTimeDesc(): List<Archive>


    @Query("SELECT * FROM Archive WHERE arcid = :arcId limit 1")
    suspend fun findByArcid(arcId: String): Archive

}