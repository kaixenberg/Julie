package our.bunny.julie.domain.repository

import kotlinx.coroutines.flow.Flow
import our.bunny.julie.domain.model.Pet

interface PetRepository {
    fun getAllPets(): Flow<List<Pet>>
    fun getPetByIdStream(id: Long): Flow<Pet?>
    suspend fun getPetById(id: Long): Pet?
    suspend fun insertPet(pet: Pet)
    suspend fun updatePet(pet: Pet)
    suspend fun deletePet(pet: Pet)
}
