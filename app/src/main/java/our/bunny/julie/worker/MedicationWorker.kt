package our.bunny.julie.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import our.bunny.julie.util.NotificationHelper

@HiltWorker
class MedicationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val petRepository: our.bunny.julie.domain.repository.PetRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val medicationId = inputData.getLong("medicationId", -1L)
        val petId = inputData.getLong("petId", -1L)
        val medicationName = inputData.getString("medicationName") ?: return Result.failure()
        val dosage = inputData.getString("dosage") ?: return Result.failure()
        
        val pet = petRepository.getPetById(petId)
        val petName = pet?.name ?: "Pet"
        val speciesEmoji = pet?.species?.let { our.bunny.julie.ui.screens.pet.PetData.getEmojiForSpecies(it) } ?: "🐾"
        
        NotificationHelper.showMedicationReminder(context, medicationId, medicationName, dosage, petId, petName, speciesEmoji)

        return Result.success()
    }
}
