package app.quranhub.data.local.db

import android.content.Context
import android.util.Log
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.concurrent.thread

/**
 * Utility class for Room used to create Room databases that can be pre-populated
 * from an existing SQLite database bundled as a file in the app assets (at `assets/databases`).
 * Use this class instead of [androidx.room.Room] when building your database.
 *
 * @author Abdallah Abdelazim ([abdallah.abdelazim@hotmail.com](mailto:abdallah.abdelazim@hotmail.com)).
 */
object RoomAsset {

    private val TAG = RoomAsset::class.java.simpleName

    /**
     * Creates a RoomDatabase.Builder for a pre-populated persistent database. Once a database is
     * built, you should keep a reference to it and re-use it.
     *
     * In the [Database] annotation on your database class, you must use `version = 2`.
     * Do not use the version in the [Database] annotation anymore. Instead, for migration,
     * increment the `version` param passed here.
     *
     * @param context The context for the database. This is usually the Application context.
     * @param klass   The abstract class which is annotated with [Database] and extends
     * [RoomDatabase].
     * @param name    The name of the database file (which should also be the name of the bundled
     * database file in the assets).
     * @param version A version number to allow for migration when the bundled assets database
     * is updated. Increment this number when updating the assets database file.
     *
     * If the database is already on the device & with a version number lower than the
     * passed number here, a migration will happen. Migration is done by deleting the
     * old database & recopying the one bundled with the assets again.
     * The version must be an integer greater than or equal 1.
     * @param <T>     The type of the database class.
     * @return A `RoomDatabaseBuilder<T>` which you can use to create the database.
    </T> */
    @JvmStatic
    fun <T : RoomDatabase> databaseBuilder(
        context: Context, klass: Class<T>, name: String, version: Int
    ): RoomDatabase.Builder<T> {
        require(name.trim().isNotEmpty()) {
            ("Cannot build a database with null or empty name."
                    + " If you are trying to create an in memory database, use Room"
                    + ".inMemoryDatabaseBuilder")
        }

        // copy pre-populated file from assets if necessary
        copyAssetDatabase(context, name, version)
        return databaseBuilder(context, klass, name)
            .addMigrations(object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    /* prevent creation of the database schema by Room */
                }
            })
    }

    /**
     * Initializes a database from a bundled SQLite database assets file in a background thread.
     * Call this at your app startup, such as in your splash screen.
     *
     * @param context
     * @param dbName    The name of the database file in assets (which also the name of the
     * database by Room).
     * @param dbVersion A version number to allow for migration when the bundled assets database
     * is updated. Increment this number when updating the assets database file.
     *
     * If the database is already on the device & with a version number lower than the
     * passed number here, a migration will happen. Migration is done by deleting the
     * old database & recopying the one bundled with the assets again.
     * The version must be an integer greater than or equal 1.
     */
    fun initializeDatabase(context: Context, dbName: String, dbVersion: Int) {
        thread {
            copyAssetDatabase(context.applicationContext, dbName, dbVersion)
        }
    }

    /**
     * Utility function that copies the SQLite database file from the assets to the databases directory
     * inside the app's internal storage area.
     *
     * @param context      This is usually the Application context.
     * @param databaseName The name of the database file in assets (which also the name of the
     * @param version      A version number to allow for migration when the bundled assets database
     * is updated. Increment this number when updating the assets database file.
     *
     * If the database is already on the device & with a version number lower than the
     * passed number here, a migration will happen. Migration is done by deleting the
     * old database & recopying the one bundled with the assets again.
     * The version must be an integer greater than or equal 1.
     */
    @Synchronized
    private fun copyAssetDatabase(
        context: Context, databaseName: String, version: Int
    ) {
        require(version >= 1) { "The version must be greater than or equal 1" }
        val sharedPref = context.getSharedPreferences(
            "room_asset_prefs", Context.MODE_PRIVATE
        )
        val oldVersion = sharedPref.getInt(databaseName, -1)

        // If the database already exists with the same version, return
        if (oldVersion == version) { // handle recopying if assets database is updated
            Log.d(TAG, "Database '$databaseName' already exists with the latest version")
            return
        }
        Log.d(TAG, "Copying database '$databaseName'...")
        val dbPath = context.getDatabasePath(databaseName)

        // delete old database file if exists
        dbPath.delete()

        // Make sure we have a path to the file
        dbPath.parentFile.mkdirs()

        // Try to copy database file
        try {
            val inputStream = context.assets.open("databases/$databaseName")
            val output: OutputStream = FileOutputStream(dbPath)
            val buffer = ByteArray(8192)
            var length: Int
            while (inputStream.read(buffer, 0, 8192).also { length = it } > 0) {
                output.write(buffer, 0, length)
            }
            output.flush()
            output.close()
            inputStream.close()
            sharedPref.edit().putInt(databaseName, version).apply()
        } catch (e: IOException) {
            Log.d(TAG, "Failed to open file", e)
            e.printStackTrace()
        }
    }
}