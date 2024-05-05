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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Archive>)

    @Query("DELETE FROM Archive")
    suspend fun clearTable()

    @Query("SELECT * FROM Archive")
    suspend fun getAll(): List<Archive>

    @Query("SELECT * FROM Archive WHERE (tags LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%') " +
            "AND isnew IN (:isNew) ORDER BY title ASC")
    suspend fun queryByTextTitleAsc(query: String, isNew: List<Int>): List<Archive>

    @Query("SELECT * FROM Archive WHERE (tags LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%') " +
            "AND isnew IN (:isNew) ORDER BY title DESC")
    suspend fun queryByTextTitleDesc(query: String, isNew: List<Int>): List<Archive>

    @Query("SELECT * FROM Archive WHERE (tags LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%') " +
            "AND isnew IN (:isNew) ORDER BY data_added ASC")
    suspend fun queryByTextDateAsc(query: String, isNew: List<Int>): List<Archive>

    @Query("SELECT * FROM Archive WHERE (tags LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%') " +
            "AND isnew IN (:isNew) ORDER BY data_added DESC")
    suspend fun queryByTextDateDesc(query: String, isNew: List<Int>): List<Archive>

    @Query("SELECT * FROM Archive ORDER BY RANDOM() limit :count")
    suspend fun getRandomArchive(count: Int): List<Archive>

    @Query("SELECT * FROM Archive WHERE arcid = :arcId limit 1")
    suspend fun findByArcid(arcId: String): Archive?

    @Query("SELECT * FROM ARCHIVE WHERE arcid IN (:idList) AND isnew IN (:isNew) ORDER BY title ASC")
    suspend fun queryByIdListTitleAsc(idList: List<String>, isNew: List<Int>): List<Archive>

    @Query("SELECT * FROM ARCHIVE WHERE arcid IN (:idList) AND isnew IN (:isNew) ORDER BY title DESC")
    suspend fun queryByIdListTitleDesc(idList: List<String>, isNew: List<Int>): List<Archive>

    @Query("SELECT * FROM ARCHIVE WHERE arcid IN (:idList) AND isnew IN (:isNew) ORDER BY data_added ASC")
    suspend fun queryByIdListDateAsc(idList: List<String>, isNew: List<Int>): List<Archive>

    @Query("SELECT * FROM ARCHIVE WHERE arcid IN (:idList) AND isnew IN (:isNew) ORDER BY data_added DESC")
    suspend fun queryByIdListDateDesc(idList: List<String>, isNew: List<Int>): List<Archive>

    @Query("UPDATE ARCHIVE SET progress = :page WHERE arcid = :id")
    suspend fun updateReadingProgress(id: String, page: Int)

    @Query("UPDATE Archive SET tags = :tags WHERE arcid = :id")
    suspend fun updateTags(id: String, tags: String)
}