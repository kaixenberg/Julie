package our.bunny.julie.domain.repository

import kotlinx.coroutines.flow.Flow
import our.bunny.julie.domain.model.FeedingLog
import our.bunny.julie.domain.model.WaterLog
import our.bunny.julie.domain.model.WeightEntry

import our.bunny.julie.domain.model.Medication

interface TrackerRepository {
    fun getWeightEntriesForPet(petId: Long): Flow<List<WeightEntry>>
    fun getLatestWeightEntry(petId: Long): Flow<WeightEntry?>
    suspend fun insertWeightEntry(entry: WeightEntry): Long
    suspend fun deleteWeightEntry(entry: WeightEntry)

    fun getFeedingLogsForPet(petId: Long): Flow<List<FeedingLog>>
    fun getLatestFeedingLog(petId: Long): Flow<FeedingLog?>
    suspend fun insertFeedingLog(log: FeedingLog): Long
    suspend fun deleteFeedingLog(log: FeedingLog)

    fun getWaterLogsForPet(petId: Long): Flow<List<WaterLog>>
    fun getTodayWaterTotal(petId: Long): Flow<Float?>
    suspend fun insertWaterLog(log: WaterLog): Long
    suspend fun deleteWaterLog(log: WaterLog)

    fun getMedicationsForPet(petId: Long): Flow<List<Medication>>
    suspend fun getAllMedications(): List<Medication>
    suspend fun insertMedication(medication: Medication): Long
    suspend fun deleteMedication(medication: Medication)

    fun getTimelineEventsForPet(petId: Long): Flow<List<our.bunny.julie.domain.model.TimelineEvent>>
}
