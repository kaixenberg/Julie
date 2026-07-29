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
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PetDatabase : RoomDatabase() {
    abstract val petDao: PetDao
    abstract val trackerDao: TrackerDao

    companion object {
        const val DATABASE_NAME = "julie_db"
    }
}
