package app.quranhub.data.local.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import app.quranhub.data.Constants;
import app.quranhub.data.local.dao.BookDao;
import app.quranhub.data.local.dao.BookmarkDao;
import app.quranhub.data.local.dao.NoteDao;
import app.quranhub.data.local.dao.QuranAudioDao;
import app.quranhub.data.local.dao.RecitationDao;
import app.quranhub.data.local.dao.ReciterDao;
import app.quranhub.data.local.dao.ReciterRecitationDao;
import app.quranhub.data.local.dao.TranslationBookDao;
import app.quranhub.data.local.entity.AyaBookmark;
import app.quranhub.data.local.entity.AyaRecorder;
import app.quranhub.data.local.entity.Book;
import app.quranhub.data.local.entity.BookmarkType;
import app.quranhub.data.local.entity.Note;
import app.quranhub.data.local.entity.QuranAudio;
import app.quranhub.data.local.entity.Reciter;
import app.quranhub.data.local.entity.ReciterRecitation;
import app.quranhub.data.local.entity.TranslationBook;
import app.quranhub.data.local.prefs.AppPreferencesManager;


@Database(entities = {AyaBookmark.class, BookmarkType.class, Book.class, TranslationBook.class,
        Note.class, app.quranhub.data.local.entity.Recitation.class, Reciter.class, ReciterRecitation.class,
        QuranAudio.class, AyaRecorder.class}, version = 3, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {

    private static final String TAG = UserDatabase.class.getSimpleName();

    public static final String DATABASE_NAME = "user.db";

    private static volatile UserDatabase instance;

    public static UserDatabase getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (UserDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    UserDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);

                                    if (!AppPreferencesManager.isDbInitialized(context)) {
                                        initData(db, context);
                                    }
                                }

                                @Override
                                public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
                                    super.onDestructiveMigration(db);

                                    AppPreferencesManager.persistDbInitialized(context, false);
                                }
                            })
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract BookmarkDao getBookmarkDao();

    public abstract BookDao getBookDao();

    public abstract TranslationBookDao getTranslationBookDao();

    public abstract NoteDao getNoteDao();

    public abstract RecitationDao getRecitationDao();

    public abstract ReciterDao getReciterDao();

    public abstract ReciterRecitationDao getReciterRecitationDao();

    public abstract QuranAudioDao getQuranAudioDao();

    private static void initData(@NonNull SupportSQLiteDatabase db, @NonNull Context context) {
        initBookmarkTypes(db);
        initAvailableRecitations(db);

        AppPreferencesManager.persistDbInitialized(context, true);
    }

    private static void initBookmarkTypes(@NonNull SupportSQLiteDatabase db) {

        String favoriteType = "INSERT INTO BookmarkType(typeId, bookmarkTypeName, colorIndex)\n" +
                "VALUES(1, \"Favorite\", 0)";
        String memorizeType = "INSERT INTO BookmarkType(typeId, bookmarkTypeName, colorIndex)\n" +
                "VALUES(2, \"Reciting\", 0)";
        String recitingType = "INSERT INTO BookmarkType(typeId, bookmarkTypeName, colorIndex)\n" +
                "VALUES(3, \"Note\", 0)";
        String noteType = "INSERT INTO BookmarkType(typeId, bookmarkTypeName, colorIndex)\n" +
                "VALUES(4, \"Memorize\", 0)";
        db.execSQL(favoriteType);
        db.execSQL(recitingType);
        db.execSQL(noteType);
        db.execSQL(memorizeType);
    }

    private static void initAvailableRecitations(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("INSERT INTO Recitation VALUES (" + Constants.Recitation.HAFS_ID + ", \"حفص عن عاصم\")");
        db.execSQL("INSERT INTO Recitation VALUES (" + Constants.Recitation.WARSH_ID + ", \"ورش عن نافع\")");
    }
}
