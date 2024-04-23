package com.sena.lanraragi.database

import android.content.Context
import androidx.room.Database
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.database.archiveData.ArchiveDao
import com.sena.lanraragi.utils.DebugLog


/**
 * FileName: LanraragiDB
 * Author: JiaoCan
 * Date: 2024/3/22
 * https://juejin.cn/post/7218743537495998524
 */

@Database(entities = [Archive::class], version = 2, exportSchema = false)
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
                    .addMigrations(
                        MIGRATION_1_2
                    )
                    .build()
                INSTANCE = instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Archive ADD COLUMN 'isBookmark' INTEGER NOT NULL DEFAULT 0")
            }
        }

        suspend fun updateArchiveList(archives: List<Archive>) {
            if (INSTANCE == null) {
                DebugLog.e("DB 未初始化")
                return
            }
            INSTANCE!!.archiveDao().apply {
                val oldData = getAll()
                archives.forEach {
                    it.isBookmark = oldData.firstOrNull { o -> o.arcid == it.arcid }?.isBookmark ?: false
                }
                clearTable()
                insertAll(archives)
            }
        }


        suspend fun getRandomArchive(count: Int): List<Archive> {
            if (INSTANCE == null) {
                DebugLog.e("DB 未初始化")
                return emptyList()
            }
            val dao = INSTANCE!!.archiveDao()

            return dao.getRandomArchive(count)
        }

        suspend fun queryArchivesWithTag(query: String): List<Archive> {
            if (INSTANCE == null) {
                DebugLog.e("DB 未初始化")
                return emptyList()
            }
            val dao = INSTANCE!!.archiveDao()
            val order = AppConfig.order
            val sort = AppConfig.sort
            val isNew = AppConfig.isNew

            if (isNew) {
                return when {
                    order == ORDER.ASC && sort == SORT.TITLE -> dao.queryArchivesWithTagByTitleAscNew(query)
                    order ==ORDER.DESC && sort ==  SORT.TITLE -> dao.queryArchivesWithTagByTitleDescNew(query)
                    order == ORDER.ASC && sort ==  SORT.TIME -> dao.queryArchivesWithTagByAddTimeAscNew(query)
                    order == ORDER.DESC && sort == SORT.TIME -> dao.queryArchivesWithTagByAddTimeDescNew(query)
                    else -> emptyList()
                }
            }

            return when {
                order == ORDER.ASC && sort == SORT.TITLE -> dao.queryArchivesWithTagByTitleAsc(query)
                order ==ORDER.DESC && sort ==  SORT.TITLE -> dao.queryArchivesWithTagByTitleDesc(query)
                order == ORDER.ASC && sort ==  SORT.TIME -> dao.queryArchivesWithTagByAddTimeAsc(query)
                order == ORDER.DESC && sort == SORT.TIME -> dao.queryArchivesWithTagByAddTimeDesc(query)
                else -> emptyList()
            }
        }

        suspend fun queryArchiveById(id: String): Archive? {
            val instance = INSTANCE
            if (instance == null) {
                DebugLog.e("DB 未初始化")
                return null
            }
            val result = instance.archiveDao().findByArcid(id)
            return result
        }


        suspend fun updateArchiveBookmark(id: String, status: Boolean) {
            if (INSTANCE == null) {
                DebugLog.e("DB 未初始化")
                return
            }
            INSTANCE!!.archiveDao().updateBookmarkByArcid(id, status)
        }

        suspend fun getBookmarkedArchives(): List<Archive> {
            val instance = INSTANCE
            if (instance == null) {
                DebugLog.e("DB 未初始化")
                return emptyList()
            }
            return instance.archiveDao().queryBookmarkedArchives()
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

