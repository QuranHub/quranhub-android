package app.quranhub.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import app.quranhub.data.Constants
import app.quranhub.data.local.dao.BookDao
import app.quranhub.data.local.dao.BookmarkDao
import app.quranhub.data.local.dao.NoteDao
import app.quranhub.data.local.dao.QuranAudioDao
import app.quranhub.data.local.dao.RecitationDao
import app.quranhub.data.local.dao.ReciterDao
import app.quranhub.data.local.dao.ReciterRecitationDao
import app.quranhub.data.local.dao.TranslationBookDao
import app.quranhub.data.local.entity.AyaBookmark
import app.quranhub.data.local.entity.AyaRecorder
import app.quranhub.data.local.entity.Book
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.data.local.entity.Note
import app.quranhub.data.local.entity.QuranAudio
import app.quranhub.data.local.entity.Recitation
import app.quranhub.data.local.entity.Reciter
import app.quranhub.data.local.entity.ReciterRecitation
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.data.local.prefs.AppPreferencesManager.isDbInitialized
import app.quranhub.data.local.prefs.AppPreferencesManager.persistDbInitialized

@Database(
    entities = [AyaBookmark::class, BookmarkType::class, Book::class, TranslationBook::class, Note::class, Recitation::class, Reciter::class, ReciterRecitation::class, QuranAudio::class, AyaRecorder::class],
    version = 3,
    exportSchema = false
)
abstract class UserDatabase : RoomDatabase() {

    abstract val bookmarkDao: BookmarkDao

    abstract val bookDao: BookDao

    abstract val translationBookDao: TranslationBookDao

    abstract val noteDao: NoteDao

    abstract val recitationDao: RecitationDao

    abstract val reciterDao: ReciterDao

    abstract val reciterRecitationDao: ReciterRecitationDao

    abstract val quranAudioDao: QuranAudioDao

    companion object {
        private val TAG = UserDatabase::class.java.simpleName

        private const val DATABASE_NAME = "user.db"

        @Volatile
        private var instance: UserDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): UserDatabase {
            return instance ?: synchronized(UserDatabase::class.java) {
                instance ?: databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java, DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            if (!isDbInitialized(context)) {
                                initData(db, context)
                            }
                        }

                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            persistDbInitialized(context, false)
                        }
                    })
                    .build().also { instance = it }
            }
        }

        private fun initData(db: SupportSQLiteDatabase, context: Context) {
            initBookmarkTypes(db)
            initAvailableRecitations(db)
            persistDbInitialized(context, true)
        }

        private fun initBookmarkTypes(db: SupportSQLiteDatabase) {
            val favoriteType = """
                INSERT INTO BookmarkType(typeId, bookmarkTypeName, colorIndex)
                VALUES(1, "Favorite", 0)
                """.trimIndent()
            val memorizeType = """
                INSERT INTO BookmarkType(typeId, bookmarkTypeName, colorIndex)
                VALUES(2, "Reciting", 0)
                """.trimIndent()
            val recitingType = """
                INSERT INTO BookmarkType(typeId, bookmarkTypeName, colorIndex)
                VALUES(3, "Note", 0)
                """.trimIndent()
            val noteType = """
                INSERT INTO BookmarkType(typeId, bookmarkTypeName, colorIndex)
                VALUES(4, "Memorize", 0)
                """.trimIndent()
            db.execSQL(favoriteType)
            db.execSQL(recitingType)
            db.execSQL(noteType)
            db.execSQL(memorizeType)
        }

        private fun initAvailableRecitations(db: SupportSQLiteDatabase) {
            db.execSQL("INSERT INTO Recitation VALUES (" + Constants.Recitation.HAFS_ID + ", \"حفص عن عاصم\")")
            db.execSQL("INSERT INTO Recitation VALUES (" + Constants.Recitation.WARSH_ID + ", \"ورش عن نافع\")")
        }
    }
}