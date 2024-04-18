package com.sena.lanraragi.database.archiveData

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


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

    @Query("SELECT * FROM Archive WHERE tags LIKE '%' || :query || '%' ORDER BY title ASC")
    suspend fun queryArchivesWithTagByTitleAsc(query: String): List<Archive>

    @Query("SELECT * FROM Archive WHERE tags LIKE '%' || :query || '%' ORDER BY title DESC")
    suspend fun queryArchivesWithTagByTitleDesc(query: String): List<Archive>

    @Query("SELECT * FROM Archive WHERE tags LIKE '%' || :query || '%' ORDER BY data_added ASC")
    suspend fun queryArchivesWithTagByAddTimeAsc(query: String): List<Archive>

    @Query("SELECT * FROM Archive WHERE tags LIKE '%' || :query || '%' ORDER BY data_added DESC")
    suspend fun queryArchivesWithTagByAddTimeDesc(query: String): List<Archive>

    @Query("SELECT * FROM Archive WHERE tags LIKE '%' || :query || '%' AND isnew = 1 ORDER BY title ASC")
    suspend fun queryArchivesWithTagByTitleAscNew(query: String): List<Archive>

    @Query("SELECT * FROM Archive WHERE tags LIKE '%' || :query || '%' AND isnew = 1 ORDER BY title DESC")
    suspend fun queryArchivesWithTagByTitleDescNew(query: String): List<Archive>

    @Query("SELECT * FROM Archive WHERE tags LIKE '%' || :query || '%' AND isnew = 1 ORDER BY data_added ASC")
    suspend fun queryArchivesWithTagByAddTimeAscNew(query: String): List<Archive>

    @Query("SELECT * FROM Archive WHERE tags LIKE '%' || :query || '%' AND isnew = 1 ORDER BY data_added DESC")
    suspend fun queryArchivesWithTagByAddTimeDescNew(query: String): List<Archive>

    @Query("SELECT * FROM Archive ORDER BY RANDOM() limit :count")
    suspend fun getRandomArchive(count: Int): List<Archive>


    @Query("SELECT * FROM Archive WHERE arcid = :arcId limit 1")
    suspend fun findByArcid(arcId: String): Archive





}