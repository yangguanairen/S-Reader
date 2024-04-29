package com.sena.lanraragi.database.statsData

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


/**
 * FileName: StatsDao
 * Author: JiaoCan
 * Date: 2024/4/28
 */


@Dao
interface StatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Stats>)

    @Query("DELETE FROM Stats")
    suspend fun clearTable()

    @Query("SELECT * FROM Stats WHERE splicingText LIKE '%' || :q || '%'")
    suspend fun queryRelatedTags(q: String): List<Stats>
}