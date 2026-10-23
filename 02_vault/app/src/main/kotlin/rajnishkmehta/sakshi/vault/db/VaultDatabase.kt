/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.db

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver

/**
 * The Room database that stores all media synchronization metadata and deletion schedules for the Sakshi Vault.
 *
 * Uses Room 3 ([androidx.room3]) with the `AndroidSQLiteDriver`.
 */
@Database(entities = [MediaRecord::class], version = 1, exportSchema = false)
abstract class VaultDatabase : RoomDatabase() {

    /** Provides access to [MediaRecord] database operations. */
    abstract fun mediaRecordDao(): MediaRecordDao

    companion object {
        @Volatile
        private var INSTANCE: VaultDatabase? = null

        /**
         * Retrieves the singleton instance of [VaultDatabase], creating it if necessary.
         *
         * @param context Application context used to build the database.
         * @return The singleton [VaultDatabase] instance.
         */
        fun getDatabase(context: Context): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VaultDatabase::class.java,
                    "sakshi_vault_beta_database"
                )
                .setDriver(AndroidSQLiteDriver())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
