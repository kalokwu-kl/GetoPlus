/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.android.geto.data.room.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.android.geto.data.room.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object RoomModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `AppSettingEntity_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `enabled` INTEGER NOT NULL,
                    `settingType` TEXT NOT NULL,
                    `componentName` TEXT NOT NULL,
                    `label` TEXT NOT NULL,
                    `key` TEXT NOT NULL,
                    `valueOnLaunch` TEXT,
                    `valueOnRevert` TEXT
                )
                """.trimIndent(),
            )
            db.execSQL(
                "INSERT INTO `AppSettingEntity_new` " +
                    "(`id`, `enabled`, `settingType`, `componentName`, `label`, `key`, `valueOnLaunch`, `valueOnRevert`) " +
                    "SELECT `id`, `enabled`, `settingType`, `componentName`, `label`, `key`, `valueOnLaunch`, `valueOnRevert` FROM `AppSettingEntity`",
            )
            db.execSQL("DROP TABLE `AppSettingEntity`")
            db.execSQL("ALTER TABLE `AppSettingEntity_new` RENAME TO `AppSettingEntity`")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `AppSettingEntity_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `enabled` INTEGER NOT NULL,
                    `settingType` TEXT NOT NULL,
                    `componentName` TEXT NOT NULL,
                    `label` TEXT NOT NULL,
                    `key` TEXT NOT NULL,
                    `valueOnLaunch` TEXT NOT NULL,
                    `valueOnRevert` TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                "INSERT INTO `AppSettingEntity_new` " +
                    "(`id`, `enabled`, `settingType`, `componentName`, `label`, `key`, `valueOnLaunch`, `valueOnRevert`) " +
                    "SELECT `id`, `enabled`, `settingType`, `componentName`, `label`, `key`, " +
                    "COALESCE(`valueOnLaunch`, ''), COALESCE(`valueOnRevert`, '') FROM `AppSettingEntity`",
            )
            db.execSQL("DROP TABLE `AppSettingEntity`")
            db.execSQL("ALTER TABLE `AppSettingEntity_new` RENAME TO `AppSettingEntity`")
        }
    }

    @Singleton
    @Provides
    fun appDatabase(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME,
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
}
