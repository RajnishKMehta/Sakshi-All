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
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * The Room database that stores all media sync metadata for Sakshi Vault.
 */
@Database(entities = [MediaRecord::class], version = 2, exportSchema = false)
abstract class VaultDatabase : RoomDatabase() {

    abstract fun mediaRecordDao(): MediaRecordDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {

            override suspend fun migrate(connection: SQLiteConnection) {

                connection.execSQL("ALTER TABLE media_records ADD COLUMN scheduledDeletionTime INTEGER DEFAULT NULL")

            }

        }

        @Volatile
        private var INSTANCE: VaultDatabase? = null

        fun getDatabase(context: Context): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VaultDatabase::class.java,
                    "sakshi_vault_beta_database"
                )
                .setDriver(AndroidSQLiteDriver())
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
