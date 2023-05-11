package app.quranhub.data.local.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class for Room used to create Room databases that can be pre-populated
 * from an existing SQLite database bundled as a file in the app assets (at {@code assets/databases}).
 * Use this class instead of {@link androidx.room.Room} when building your database.
 *
 * @author Abdallah Abdelazim (<a href="mailto:abdallah.abdelazim@hotmail.com">abdallah.abdelazim@hotmail.com</a>).
 */
public class RoomAsset {

    private static final String TAG = RoomAsset.class.getSimpleName();

    /**
     * Creates a RoomDatabase.Builder for a pre-populated persistent database. Once a database is
     * built, you should keep a reference to it and re-use it.
     * <p>In the {@link Database} annotation on your database class, you must use {@code version = 2}.
     * Do not use the version in the {@link Database} annotation anymore. Instead, for migration,
     * increment the {@code version} param passed here.
     *
     * @param context The context for the database. This is usually the Application context.
     * @param klass   The abstract class which is annotated with {@link Database} and extends
     *                {@link RoomDatabase}.
     * @param name    The name of the database file (which should also be the name of the bundled
     *                database file in the assets).
     * @param version A version number to allow for migration when the bundled assets database
     *                is updated. Increment this number when updating the assets database file.
     *                <p>If the database is already on the device & with a version number lower than the
     *                passed number here, a migration will happen. Migration is done by deleting the
     *                old database & recopying the one bundled with the assets again.
     *                The version must be an integer greater than or equal 1.
     * @param <T>     The type of the database class.
     * @return A {@code RoomDatabaseBuilder<T>} which you can use to create the database.
     */
    @NonNull
    public static <T extends RoomDatabase> RoomDatabase.Builder<T> databaseBuilder(
            @NonNull Context context, @NonNull Class<T> klass, @NonNull String name, int version) {
        //noinspection ConstantConditions
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("Cannot build a database with null or empty name."
                    + " If you are trying to create an in memory database, use Room"
                    + ".inMemoryDatabaseBuilder");
        }

        // copy pre-populated file from assets if necessary
        copyAssetDatabase(context, name, version);

        return Room.databaseBuilder(context, klass, name)
                .addMigrations(new Migration(1, 2) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        /* prevent creation of the database schema by Room */
                    }
                });
    }

    /**
     * Initializes a database from a bundled SQLite database assets file in a background thread.
     * Call this at your app startup, such as in your splash screen.
     *
     * @param context
     * @param dbName    The name of the database file in assets (which also the name of the
     *                  database by Room).
     * @param dbVersion A version number to allow for migration when the bundled assets database
     *                  is updated. Increment this number when updating the assets database file.
     *                  <p>If the database is already on the device & with a version number lower than the
     *                  passed number here, a migration will happen. Migration is done by deleting the
     *                  old database & recopying the one bundled with the assets again.
     *                  The version must be an integer greater than or equal 1.
     */
    public static void initializeDatabase(@NonNull Context context, @NonNull String dbName, int dbVersion) {

        new Thread(() ->
                copyAssetDatabase(context.getApplicationContext(), dbName, dbVersion)).start();
    }

    /**
     * Utility function that copies the SQLite database file from the assets to the databases directory
     * inside the app's internal storage area.
     *
     * @param context      This is usually the Application context.
     * @param databaseName The name of the database file in assets (which also the name of the
     * @param version      A version number to allow for migration when the bundled assets database
     *                     is updated. Increment this number when updating the assets database file.
     *                     <p>If the database is already on the device & with a version number lower than the
     *                     passed number here, a migration will happen. Migration is done by deleting the
     *                     old database & recopying the one bundled with the assets again.
     *                     The version must be an integer greater than or equal 1.
     */
    private static synchronized void copyAssetDatabase(@NonNull Context context
            , @NonNull String databaseName, int version) {

        if (version < 1) {
            throw new IllegalArgumentException("The version must be greater than or equal 1");
        }

        SharedPreferences sharedPref = context.getSharedPreferences("room_asset_prefs"
                , Context.MODE_PRIVATE);
        int oldVersion = sharedPref.getInt(databaseName, -1);

        // If the database already exists with the same version, return
        if (oldVersion == version) { // handle recopying if assets database is updated
            Log.d(TAG, "Database '" + databaseName + "' already exists with the latest version");
            return;
        }

        Log.d(TAG, "Copying database '" + databaseName + "'...");

        final File dbPath = context.getDatabasePath(databaseName);

        // delete old database file if exists
        dbPath.delete();

        // Make sure we have a path to the file
        dbPath.getParentFile().mkdirs();

        // Try to copy database file
        try {
            final InputStream inputStream = context.getAssets().open("databases/" + databaseName);
            final OutputStream output = new FileOutputStream(dbPath);

            byte[] buffer = new byte[8192];
            int length;

            while ((length = inputStream.read(buffer, 0, 8192)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();
            inputStream.close();

            sharedPref.edit().putInt(databaseName, version).apply();
        } catch (IOException e) {
            Log.d(TAG, "Failed to open file", e);
            e.printStackTrace();
        }
    }

}
