package our.bunny.julie.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import our.bunny.julie.data.local.entity.FeedingLogEntity
import our.bunny.julie.data.local.entity.WaterLogEntity
import our.bunny.julie.data.local.entity.WeightEntryEntity
import our.bunny.julie.data.local.entity.MedicationEntity
import our.bunny.julie.data.local.entity.MedicationScheduleEntity
import our.bunny.julie.data.local.entity.MedicationWithSchedules
import androidx.room.Transaction

@Dao
interface TrackerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntry(entry: WeightEntryEntity): Long

    @Query("SELECT * FROM weight_entries WHERE petId = :petId ORDER BY date DESC")
    fun getWeightEntriesForPet(petId: Long): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries WHERE petId = :petId ORDER BY date DESC LIMIT 1")
    fun getLatestWeightEntry(petId: Long): Flow<WeightEntryEntity?>

    @Delete
    suspend fun deleteWeightEntry(entry: WeightEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedingLog(log: FeedingLogEntity): Long

    @Query("SELECT * FROM feeding_logs WHERE petId = :petId ORDER BY time DESC")
    fun getFeedingLogsForPet(petId: Long): Flow<List<FeedingLogEntity>>

    @Query("SELECT * FROM feeding_logs WHERE petId = :petId ORDER BY time DESC LIMIT 1")
    fun getLatestFeedingLog(petId: Long): Flow<FeedingLogEntity?>

    @Delete
    suspend fun deleteFeedingLog(log: FeedingLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLogEntity): Long

    @Query("SELECT * FROM water_logs WHERE petId = :petId ORDER BY time DESC")
    fun getWaterLogsForPet(petId: Long): Flow<List<WaterLogEntity>>

    @Query("SELECT SUM(amount) FROM water_logs WHERE petId = :petId AND date(time) = date('now')")
    fun getTodayWaterTotal(petId: Long): Flow<Float?>

    @Delete
    suspend fun deleteWaterLog(log: WaterLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationSchedules(schedules: List<MedicationScheduleEntity>)

    @Query("DELETE FROM medication_schedules WHERE medicationId = :medicationId")
    suspend fun deleteMedicationSchedules(medicationId: Long)

    @Transaction
    @Query("SELECT * FROM medications WHERE petId = :petId")
    fun getMedicationsForPet(petId: Long): Flow<List<MedicationWithSchedules>>

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)
}
