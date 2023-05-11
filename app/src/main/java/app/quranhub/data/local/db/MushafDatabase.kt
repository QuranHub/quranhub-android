package app.quranhub.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import app.quranhub.data.local.dao.AyaDao
import app.quranhub.data.local.dao.AyaQuranSubjectDao
import app.quranhub.data.local.dao.HizbQuarterDao
import app.quranhub.data.local.dao.JuzDao
import app.quranhub.data.local.dao.QuranSubjectCategoryDao
import app.quranhub.data.local.dao.QuranSubjectDao
import app.quranhub.data.local.dao.SuraDao
import app.quranhub.data.local.db.RoomAsset.databaseBuilder
import app.quranhub.data.local.entity.Aya
import app.quranhub.data.local.entity.AyaQuranSubject
import app.quranhub.data.local.entity.HizbQuarter
import app.quranhub.data.local.entity.Juz
import app.quranhub.data.local.entity.QuranSubject
import app.quranhub.data.local.entity.QuranSubjectCategory
import app.quranhub.data.local.entity.Sura

@Database(
    entities = [Sura::class, Aya::class, HizbQuarter::class, Juz::class, QuranSubjectCategory::class, QuranSubject::class, AyaQuranSubject::class],
    version = 2,
    exportSchema = false
)
abstract class MushafDatabase : RoomDatabase() {

    abstract val ayaDao: AyaDao

    abstract val hizbQuarterDao: HizbQuarterDao

    abstract val juzDao: JuzDao

    abstract val suraDao: SuraDao

    abstract val quranSubjectCategoryDao: QuranSubjectCategoryDao

    abstract val quranSubjectDao: QuranSubjectDao

    abstract val ayaQuranSubjectDao: AyaQuranSubjectDao

    companion object {

        const val DATABASE_NAME = "mushaf_metadata.db"

        const val ASSET_DB_VERSION = 2

        @Volatile
        private var instance: MushafDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): MushafDatabase {
            return instance ?: synchronized(MushafDatabase::class.java) {
                instance ?: databaseBuilder(
                    context.applicationContext,
                    MushafDatabase::class.java, DATABASE_NAME, ASSET_DB_VERSION
                )
                    .build().also { instance = it }
            }
        }
    }
}