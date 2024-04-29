package com.sena.lanraragi.database.category

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


/**
 * FileName: CategoryDao
 * Author: JiaoCan
 * Date: 2024/4/28
 */

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Category>)

    @Query("DELETE FROM Category")
    suspend fun clearTable()

    @Query("SELECT * FROM Category")
    suspend fun getAll(): List<Category>
}

