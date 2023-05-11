package app.quranhub.data.local.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import app.quranhub.data.local.dao.AyaDao;
import app.quranhub.data.local.dao.AyaQuranSubjectDao;
import app.quranhub.data.local.dao.HizbQuarterDao;
import app.quranhub.data.local.dao.JuzDao;
import app.quranhub.data.local.dao.QuranSubjectCategoryDao;
import app.quranhub.data.local.dao.QuranSubjectDao;
import app.quranhub.data.local.dao.SuraDao;
import app.quranhub.data.local.entity.Aya;
import app.quranhub.data.local.entity.AyaQuranSubject;
import app.quranhub.data.local.entity.HizbQuarter;
import app.quranhub.data.local.entity.Juz;
import app.quranhub.data.local.entity.QuranSubject;
import app.quranhub.data.local.entity.QuranSubjectCategory;
import app.quranhub.data.local.entity.Sura;

@Database(entities = {Sura.class, Aya.class, HizbQuarter.class, Juz.class, QuranSubjectCategory.class
        , QuranSubject.class, AyaQuranSubject.class}, version = 2, exportSchema = false)
public abstract class MushafDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "mushaf_metadata.db";
    public static final int ASSET_DB_VERSION = 2;

    private static volatile MushafDatabase instance;

    public static MushafDatabase getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (MushafDatabase.class) {
                if (instance == null) {
                    instance = RoomAsset.databaseBuilder(context.getApplicationContext(),
                                    MushafDatabase.class, DATABASE_NAME, ASSET_DB_VERSION)
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract AyaDao getAyaDao();

    public abstract HizbQuarterDao getHizbQuarterDao();

    public abstract JuzDao getJuzDao();

    public abstract SuraDao getSuraDao();

    public abstract QuranSubjectCategoryDao getQuranSubjectCategoryDao();

    public abstract QuranSubjectDao getQuranSubjectDao();

    public abstract AyaQuranSubjectDao getAyaQuranSubjectDao();

}
