package our.bunny.julie.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import our.bunny.julie.data.local.dao.PetDao
import our.bunny.julie.data.local.dao.TrackerDao
import our.bunny.julie.data.local.entity.FeedingLogEntity
import our.bunny.julie.data.local.entity.PetEntity
import our.bunny.julie.data.local.entity.WaterLogEntity
import our.bunny.julie.data.local.entity.WeightEntryEntity
import our.bunny.julie.data.local.entity.MedicationEntity
import our.bunny.julie.data.local.entity.MedicationScheduleEntity

import our.bunny.julie.data.local.dao.BackupRestoreDao

@Database(
    entities = [
        PetEntity::class,
        WeightEntryEntity::class,
        FeedingLogEntity::class,
        WaterLogEntity::class,
        MedicationEntity::class,
        MedicationScheduleEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PetDatabase : RoomDatabase() {
    abstract val petDao: PetDao
    abstract val trackerDao: TrackerDao
    abstract val backupRestoreDao: BackupRestoreDao

    companion object {
        const val DATABASE_NAME = "julie_db"

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Migrate weight_entries
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `weight_entries_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `petId` INTEGER NOT NULL, 
                        `date` TEXT NOT NULL, 
                        `weight` REAL NOT NULL, 
                        `notes` TEXT NOT NULL, 
                        FOREIGN KEY(`petId`) REFERENCES `pets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                
                database.execSQL("""
                    INSERT INTO `weight_entries_new` (`id`, `petId`, `date`, `weight`, `notes`)
                    SELECT `id`, `petId`, `date`, 
                    CASE WHEN `unit` = 'LBS' OR `unit` = 'lbs' THEN `weight` / 2.20462 ELSE `weight` END, 
                    `notes` FROM `weight_entries`
                """)
                
                database.execSQL("DROP TABLE `weight_entries`")
                database.execSQL("ALTER TABLE `weight_entries_new` RENAME TO `weight_entries`")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_weight_entries_petId` ON `weight_entries` (`petId`)")

                // Migrate water_logs
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `water_logs_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `petId` INTEGER NOT NULL, 
                        `amount` REAL NOT NULL, 
                        `time` TEXT NOT NULL, 
                        FOREIGN KEY(`petId`) REFERENCES `pets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                
                database.execSQL("""
                    INSERT INTO `water_logs_new` (`id`, `petId`, `amount`, `time`)
                    SELECT `id`, `petId`, 
                    CASE WHEN `unit` = 'OZ' OR `unit` = 'oz' THEN `amount` * 29.5735 ELSE `amount` END, 
                    `time` FROM `water_logs`
                """)
                
                database.execSQL("DROP TABLE `water_logs`")
                database.execSQL("ALTER TABLE `water_logs_new` RENAME TO `water_logs`")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_water_logs_petId` ON `water_logs` (`petId`)")
            }
        }

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // 1. Create medication_schedules table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `medication_schedules` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `medicationId` INTEGER NOT NULL, 
                        `timeOfDay` TEXT NOT NULL, 
                        `daysOfWeek` TEXT NOT NULL, 
                        FOREIGN KEY(`medicationId`) REFERENCES `medications`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_medication_schedules_medicationId` ON `medication_schedules` (`medicationId`)")

                // 2. Migrate existing schedules: all existing rows become "every day"
                // Best-effort mapping: "Twice a day" only had one time stored. Users must manually add the second dose.
                android.util.Log.w("PetDatabase", "Migration 5 to 6: 'Twice a day' medications were migrated using their single stored time. Users may need to manually add the second schedule time.")

                database.execSQL("""
                    INSERT INTO `medication_schedules` (`medicationId`, `timeOfDay`, `daysOfWeek`)
                    SELECT `id`, `timeOfDay`, 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY' FROM `medications`
                """)

                // 3. Rebuild medications table to drop frequency and timeOfDay
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `medications_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `petId` INTEGER NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `dosage` TEXT NOT NULL, 
                        `isActive` INTEGER NOT NULL, 
                        `notes` TEXT NOT NULL, 
                        FOREIGN KEY(`petId`) REFERENCES `pets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                
                database.execSQL("""
                    INSERT INTO `medications_new` (`id`, `petId`, `name`, `dosage`, `isActive`, `notes`)
                    SELECT `id`, `petId`, `name`, `dosage`, `isActive`, `notes` FROM `medications`
                """)
                
                database.execSQL("DROP TABLE `medications`")
                database.execSQL("ALTER TABLE `medications_new` RENAME TO `medications`")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_medications_petId` ON `medications` (`petId`)")
            }
        }

        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `medications_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `petId` INTEGER NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `dosage` TEXT NOT NULL, 
                        `medicationType` TEXT NOT NULL, 
                        `isActive` INTEGER NOT NULL, 
                        `notes` TEXT NOT NULL, 
                        FOREIGN KEY(`petId`) REFERENCES `pets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                
                database.execSQL("""
                    INSERT INTO `medications_new` (`id`, `petId`, `name`, `dosage`, `medicationType`, `isActive`, `notes`)
                    SELECT `id`, `petId`, `name`, `dosage`,
                    CASE 
                        WHEN `dosage` LIKE '%pill(s)%' THEN 'Pill(s)'
                        WHEN `dosage` LIKE '%capsule(s)%' THEN 'Capsule(s)'
                        WHEN `dosage` LIKE '%drops%' THEN 'Drops'
                        WHEN `dosage` LIKE '%ml%' OR `dosage` LIKE '%tsp%' OR `dosage` LIKE '%tbsp%' THEN 'Liquid'
                        WHEN `dosage` LIKE '%mg%' OR `dosage` LIKE '%g%' THEN 'Pill(s)'
                        ELSE 'Unspecified'
                    END,
                    `isActive`, `notes` FROM `medications`
                """)
                
                database.execSQL("DROP TABLE `medications`")
                database.execSQL("ALTER TABLE `medications_new` RENAME TO `medications`")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_medications_petId` ON `medications` (`petId`)")
            }
        }
    }
}
