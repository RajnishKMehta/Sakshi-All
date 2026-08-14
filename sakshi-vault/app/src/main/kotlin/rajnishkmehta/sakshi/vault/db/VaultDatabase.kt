package rajnishkmehta.sakshi.vault.db

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

/**
 * The Room database that stores all media sync metadata for Sakshi Vault.
 */
@Database(entities = [MediaRecord::class], version = 1, exportSchema = false)
abstract class VaultDatabase : RoomDatabase() {

    abstract fun mediaRecordDao(): MediaRecordDao

    companion object {
        @Volatile
        private var INSTANCE: VaultDatabase? = null

        fun getDatabase(context: Context): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VaultDatabase::class.java,
                    "sakshi_vault_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
