package our.bunny.julie.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import our.bunny.julie.data.local.dao.TrackerDao
import our.bunny.julie.data.local.entity.FeedingLogEntity
import our.bunny.julie.data.local.entity.WaterLogEntity
import our.bunny.julie.data.local.entity.WeightEntryEntity
import our.bunny.julie.data.local.entity.MedicationEntity
import our.bunny.julie.domain.model.FeedingLog
import our.bunny.julie.domain.model.WaterLog
import our.bunny.julie.domain.model.WeightEntry
import our.bunny.julie.domain.model.Medication
import our.bunny.julie.data.local.entity.MedicationScheduleEntity
import our.bunny.julie.domain.repository.TrackerRepository
import androidx.room.withTransaction

class TrackerRepositoryImpl(
    private val dao: TrackerDao
) : TrackerRepository {

    override fun getWeightEntriesForPet(petId: Long): Flow<List<WeightEntry>> {
        return dao.getWeightEntriesForPet(petId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getLatestWeightEntry(petId: Long): Flow<WeightEntry?> {
        return dao.getLatestWeightEntry(petId).map { it?.toDomainModel() }
    }

    override suspend fun insertWeightEntry(entry: WeightEntry): Long {
        return dao.insertWeightEntry(WeightEntryEntity.fromDomainModel(entry))
    }

    override suspend fun deleteWeightEntry(entry: WeightEntry) {
        dao.deleteWeightEntry(WeightEntryEntity.fromDomainModel(entry))
    }

    override fun getFeedingLogsForPet(petId: Long): Flow<List<FeedingLog>> {
        return dao.getFeedingLogsForPet(petId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getLatestFeedingLog(petId: Long): Flow<FeedingLog?> {
        return dao.getLatestFeedingLog(petId).map { it?.toDomainModel() }
    }

    override suspend fun insertFeedingLog(log: FeedingLog): Long {
        return dao.insertFeedingLog(FeedingLogEntity.fromDomainModel(log))
    }

    override suspend fun deleteFeedingLog(log: FeedingLog) {
        dao.deleteFeedingLog(FeedingLogEntity.fromDomainModel(log))
    }

    override fun getWaterLogsForPet(petId: Long): Flow<List<WaterLog>> {
        return dao.getWaterLogsForPet(petId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getTodayWaterTotal(petId: Long): Flow<Float?> {
        return dao.getTodayWaterTotal(petId)
    }

    override suspend fun insertWaterLog(log: WaterLog): Long {
        return dao.insertWaterLog(WaterLogEntity.fromDomainModel(log))
    }

    override suspend fun deleteWaterLog(log: WaterLog) {
        dao.deleteWaterLog(WaterLogEntity.fromDomainModel(log))
    }

    override fun getMedicationsForPet(petId: Long): Flow<List<Medication>> {
        return dao.getMedicationsForPet(petId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun insertMedication(medication: Medication): Long {
        val medicationId = if (medication.id == 0L) {
            dao.insertMedication(MedicationEntity.fromDomainModel(medication))
        } else {
            dao.insertMedication(MedicationEntity.fromDomainModel(medication))
            medication.id
        }
        dao.deleteMedicationSchedules(medicationId)
        if (medication.schedules.isNotEmpty()) {
            dao.insertMedicationSchedules(medication.schedules.map { MedicationScheduleEntity.fromDomainModel(medicationId, it) })
        }
        return medicationId
    }

    override suspend fun deleteMedication(medication: Medication) {
        dao.deleteMedication(MedicationEntity.fromDomainModel(medication))
    }

    override fun getTimelineEventsForPet(petId: Long): Flow<List<our.bunny.julie.domain.model.TimelineEvent>> {
        return kotlinx.coroutines.flow.combine(
            getWeightEntriesForPet(petId),
            getFeedingLogsForPet(petId),
            getWaterLogsForPet(petId)
        ) { weightEntries, feedingLogs, waterLogs ->
            val events = mutableListOf<our.bunny.julie.domain.model.TimelineEvent>()
            events.addAll(weightEntries.map { our.bunny.julie.domain.model.TimelineEvent.WeightEvent(it) })
            events.addAll(feedingLogs.map { our.bunny.julie.domain.model.TimelineEvent.FeedingEvent(it) })
            events.addAll(waterLogs.map { our.bunny.julie.domain.model.TimelineEvent.WaterEvent(it) })
            events.sortedByDescending { it.timestamp }
        }
    }
}
