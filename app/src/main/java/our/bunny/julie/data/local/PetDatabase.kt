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

@Database(
    entities = [
        PetEntity::class,
        WeightEntryEntity::class,
        FeedingLogEntity::class,
        WaterLogEntity::class,
        MedicationEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PetDatabase : RoomDatabase() {
    abstract val petDao: PetDao
    abstract val trackerDao: TrackerDao

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
    }
}
