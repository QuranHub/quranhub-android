package app.quranhub.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.quranhub.data.local.dao.TranslationDao
import app.quranhub.data.local.entity.Translation

@Database(entities = [Translation::class], version = 2, exportSchema = false)
abstract class TranslationDatabase : RoomDatabase() {

    abstract val translationDao: TranslationDao

    companion object {

        @JvmStatic
        fun newInstance(context: Context, databaseName: String): TranslationDatabase {
            return databaseBuilder(
                context.applicationContext,
                TranslationDatabase::class.java, databaseName
            )
                .addMigrations(MIGRATION_1_2).build()
        }

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                /* prevent creation of schema */
            }
        }
    }
}