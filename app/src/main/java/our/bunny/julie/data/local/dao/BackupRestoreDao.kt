package our.bunny.julie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import our.bunny.julie.data.local.entity.FeedingLogEntity
import our.bunny.julie.data.local.entity.MedicationEntity
import our.bunny.julie.data.local.entity.MedicationScheduleEntity
import our.bunny.julie.data.local.entity.PetEntity
import our.bunny.julie.data.local.entity.WaterLogEntity
import our.bunny.julie.data.local.entity.WeightEntryEntity

@Dao
interface BackupRestoreDao {

    @Query("SELECT * FROM pets")
    suspend fun getAllPets(): List<PetEntity>

    @Query("SELECT * FROM weight_entries")
    suspend fun getAllWeightEntries(): List<WeightEntryEntity>

    @Query("SELECT * FROM water_logs")
    suspend fun getAllWaterLogs(): List<WaterLogEntity>

    @Query("SELECT * FROM feeding_logs")
    suspend fun getAllFeedingLogs(): List<FeedingLogEntity>

    @Query("SELECT * FROM medications")
    suspend fun getAllMedications(): List<MedicationEntity>

    @Query("SELECT * FROM medication_schedules")
    suspend fun getAllMedicationSchedules(): List<MedicationScheduleEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPets(pets: List<PetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntries(entries: List<WeightEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLogs(logs: List<WaterLogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedingLogs(logs: List<FeedingLogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<MedicationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationSchedules(schedules: List<MedicationScheduleEntity>)


    @Transaction
    suspend fun restoreData(
        pets: List<PetEntity>,
        weightEntries: List<WeightEntryEntity>,
        waterLogs: List<WaterLogEntity>,
        feedingLogs: List<FeedingLogEntity>,
        medications: List<MedicationEntity>,
        medicationSchedules: List<MedicationScheduleEntity>
    ) {
        insertPets(pets)
        insertWeightEntries(weightEntries)
        insertWaterLogs(waterLogs)
        insertFeedingLogs(feedingLogs)
        insertMedications(medications)
        insertMedicationSchedules(medicationSchedules)
    }
}
