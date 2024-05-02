package com.sena.lanraragi.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sena.lanraragi.AppConfig
import com.sena.lanraragi.database.archiveData.Archive
import com.sena.lanraragi.database.archiveData.ArchiveDao
import com.sena.lanraragi.database.bookmarkData.Bookmark
import com.sena.lanraragi.database.bookmarkData.BookmarkDao
import com.sena.lanraragi.database.category.Category
import com.sena.lanraragi.database.category.CategoryDao
import com.sena.lanraragi.database.statsData.Stats
import com.sena.lanraragi.database.statsData.StatsDao
import com.sena.lanraragi.utils.DebugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * FileName: LanraragiDB
 * Author: JiaoCan
 * Date: 2024/3/22
 * https://juejin.cn/post/7218743537495998524
 */

@Database(entities = [Archive::class, Stats::class, Category::class, Bookmark::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class LanraragiDB : RoomDatabase() {

    abstract fun archiveDao(): ArchiveDao
    abstract fun statsDao(): StatsDao
    abstract fun categoryDao(): CategoryDao
    abstract fun bookmarkDao(): BookmarkDao

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
                        MIGRATION_1_2,
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

        suspend fun updateArchiveList(archives: List<Archive>) = dbInvoke(Unit) {
            it.archiveDao().apply {
                val oldData = getAll()
                archives.forEach { a ->
                    val oldItem = oldData.firstOrNull { o -> o.arcid == a.arcid }
//                    a.isBookmark = oldItem?.isBookmark ?: false
                    if (!AppConfig.enableSyn) {
                        oldItem?.let { a.progress = oldItem.progress }
                    }
                }
                clearTable()
                insertAll(archives)
            }
        }


        suspend fun getRandomArchive(count: Int): List<Archive> = dbInvoke(emptyList()) {
            it.archiveDao().getRandomArchive(count)
        }

        suspend fun queryArchivesWithTag(query: String): List<Archive> = dbInvoke(emptyList()) {
            val dao = it.archiveDao()
            val order = AppConfig.order
            val sort = AppConfig.sort
            val isNew = if (AppConfig.isNew) listOf(1) else listOf(0, 1)

            when {
                order == ORDER.ASC && sort == SORT.TITLE -> dao.queryByTextTitleAsc(query, isNew)
                order ==ORDER.DESC && sort ==  SORT.TITLE -> dao.queryByTextTitleDesc(query, isNew)
                order == ORDER.ASC && sort ==  SORT.TIME -> dao.queryByTextDateAsc(query, isNew)
                order == ORDER.DESC && sort == SORT.TIME -> dao.queryByTextDateDesc(query, isNew)
                else -> emptyList()
            }
        }

        suspend fun queryArchiveById(id: String): Archive? = dbInvoke(null) {
            it.archiveDao().findByArcid(id)
        }

        suspend fun queryArchivesByIdList(idList: List<String>): List<Archive> = dbInvoke(emptyList()) {
            val order = AppConfig.order
            val sort = AppConfig.sort
            val dao = it.archiveDao()
            val isNew = if (AppConfig.isNew) listOf(1) else listOf(1, 0)

            when  {
                order == ORDER.ASC && sort == SORT.TITLE -> dao.queryByIdListTitleAsc(idList, isNew)
                order ==ORDER.DESC && sort ==  SORT.TITLE -> dao.queryByIdListTitleDesc(idList, isNew)
                order == ORDER.ASC && sort ==  SORT.TIME -> dao.queryByIdListDateAsc(idList, isNew)
                order == ORDER.DESC && sort == SORT.TIME -> dao.queryByIdListDateDesc(idList, isNew)
                else -> emptyList()
            }
        }

        suspend fun updateReadingProgress(id: String, page: Int) = dbInvoke(Unit) {
            it.archiveDao().updateReadingProgress(id, page)
            it.bookmarkDao().updateProgress(id, page)
        }

        suspend fun updateStatsTable(list: List<Stats>) = dbInvoke(Unit) {
            it.statsDao().clearTable()
            it.statsDao().insertAll(list)
        }

        suspend fun getRelatedTags(q: String): List<Stats> = dbInvoke(listOf()) {
            it.statsDao().queryRelatedTags(q)
        }

        suspend fun updateCategoryTable(list: List<Category>) = dbInvoke(Unit) {
            it.categoryDao().apply {
                clearTable()
                insertAll(list)
            }
        }

        suspend fun queryAllCategories() = dbInvoke(emptyList()) {
            it.categoryDao().getAll()
        }

        suspend fun queryCategoriesById(id: String) = dbInvoke(emptyList()) {
            it.categoryDao().queryCategoriesById(id)
        }

        suspend fun updateBookmark(id: String, isBookmark: Boolean) = dbInvoke(Unit) {
            if (isBookmark) {
                it.archiveDao().findByArcid(id)?.let { archive ->
                    val newBookmark = Bookmark(archive.arcid, AppConfig.serverHost, archive.title, archive.tags, archive.progress)
                    it.bookmarkDao().add(newBookmark)
                }
            } else {
                it.bookmarkDao().remove(id)
            }
        }

        suspend fun queryAllBookmark() = dbInvoke(emptyList()) {
            val serverHost = AppConfig.serverHost
            it.bookmarkDao().queryByServerHost(serverHost)
        }

        suspend fun isBookmarked(id: String) = dbInvoke(false) {
            val bookmark = it.bookmarkDao().queryById(id)
            bookmark != null
        }

        private suspend fun <T> dbInvoke(def: T, func: suspend (instance: LanraragiDB) -> T): T {
            val instance = INSTANCE
            if (instance == null) {
                DebugLog.e("DB 未初始化")
                return def
            }
            val result = withContext(Dispatchers.IO) {
                func.invoke(instance)
            }
            return result
        }
    }
}

