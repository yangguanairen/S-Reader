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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Category)

    @Query("DELETE FROM Category")
    suspend fun clearTable()

    @Query("SELECT * FROM Category")
    suspend fun getAll(): List<Category>

    @Query("SELECT * FROM Category WHERE archives LIKE '%' || :arcId || '%'")
    suspend fun queryCategoriesByArcId(arcId: String): List<Category>

    @Query("SELECT * FROM Category WHERE id = :id LIMIT 1")
    suspend fun queryCategory(id: String): Category?

    @Query("UPDATE Category SET archives = :list WHERE id = :id")
    suspend fun updateArchives(id: String, list: List<String>)

    @Query("DELETE FROM Category WHERE id = :id")
    suspend fun deleteCategory(id: String)
}

