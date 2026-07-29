package our.bunny.julie.domain.repository

import kotlinx.coroutines.flow.Flow
import our.bunny.julie.domain.model.PetHealthReport

interface ExportRepository {
    fun getPetHealthReport(petId: Long): Flow<PetHealthReport?>
}
