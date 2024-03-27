package com.sena.lanraragi.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.database.archiveData.ArchiveDao
import com.sena.lanraragi.utils.DebugLog


/**
 * FileName: LanraragiDB
 * Author: JiaoCan
 * Date: 2024/3/22
 * https://juejin.cn/post/7218743537495998524
 */

@Database(entities = [Archive::class], version = 1, exportSchema = false)
abstract class LanraragiDB : RoomDatabase() {

    abstract fun archiveDao(): ArchiveDao


    companion object DBHelper {
        @Volatile
        private var INSTANCE: LanraragiDB? = null

        enum class ORDER {
            ASC,
            DESC
        }

        enum class SORT(val s: String) {
            TITLE("title"),
            TIME("data_added")
        }

        fun init(context: Context) {
            synchronized(this) {
                val instance = Room.databaseBuilder(context, LanraragiDB::class.java, "lanraragi_db")
                    .fallbackToDestructiveMigration()
                    // .allowMainThreadQueries()
                    .addCallback(object : Callback () {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            DebugLog.d("Room: onCreate() db_name = ${db.path}")
                        }
                    })
                    .build()
                INSTANCE = instance
            }
        }

        suspend fun updateArchiveList(archives: List<Archive>) {
            if (INSTANCE == null) {
                DebugLog.e("DB 未初始化")
                return
            }
            INSTANCE!!.archiveDao().apply {
                clearTable()
                insertAll(archives)
            }
        }

        suspend fun filterArchiveList(
            sort: SORT = SORT.TITLE, order: ORDER = ORDER.ASC,
            start: Int = 0, limit: Int = 20
        ): List<Archive> {
            if (INSTANCE == null) {
                DebugLog.e("DB 未初始化")
                return emptyList()
            }
            val dao = INSTANCE!!.archiveDao()

            return when {
                order == ORDER.ASC && sort == SORT.TITLE -> dao.getAllOrderByTitleAsc()
                order ==ORDER.DESC && sort ==  SORT.TITLE -> dao.getAllOrderByTitleDesc()
                order == ORDER.ASC && sort ==  SORT.TIME -> dao.getAllOrderByTimeAsc()
                order == ORDER.DESC && sort == SORT.TIME -> dao.getAllOrderByTimeDesc()
                else -> emptyList()
            }
        }

        suspend fun findArchiveByArcid(arcId: String): Archive?  {
            if (INSTANCE == null) {
                DebugLog.e("DB 未初始化")
                return null
            }
            val dao = INSTANCE!!.archiveDao()

            return dao.findByArcid(arcId)
        }
    }






    override fun clearAllTables() {
        TODO("Not yet implemented")
    }

    override fun createInvalidationTracker(): InvalidationTracker {
        TODO("Not yet implemented")
    }

    override fun createOpenHelper(config: DatabaseConfiguration): SupportSQLiteOpenHelper {
        TODO("Not yet implemented")
    }


}

