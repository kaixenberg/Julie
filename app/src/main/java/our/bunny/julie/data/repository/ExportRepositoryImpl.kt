package our.bunny.julie.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import our.bunny.julie.domain.model.PetHealthReport
import our.bunny.julie.domain.repository.ExportRepository
import our.bunny.julie.domain.repository.PetRepository
import our.bunny.julie.domain.repository.TrackerRepository
import javax.inject.Inject

class ExportRepositoryImpl @Inject constructor(
    private val petRepository: PetRepository,
    private val trackerRepository: TrackerRepository
) : ExportRepository {

    override fun getPetHealthReport(petId: Long): Flow<PetHealthReport?> {
        return combine(
            petRepository.getPetByIdStream(petId),
            trackerRepository.getTimelineEventsForPet(petId),
            trackerRepository.getMedicationsForPet(petId)
        ) { pet, events, medications ->
            if (pet != null) {
                PetHealthReport(
                    pet = pet,
                    timelineEvents = events,
                    medications = medications
                )
            } else {
                null
            }
        }
    }
}
