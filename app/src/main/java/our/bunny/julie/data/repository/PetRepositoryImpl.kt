package our.bunny.julie.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import our.bunny.julie.data.local.dao.PetDao
import our.bunny.julie.data.local.entity.PetEntity
import our.bunny.julie.domain.model.Pet
import our.bunny.julie.domain.repository.PetRepository

class PetRepositoryImpl(
    private val dao: PetDao
) : PetRepository {
    override fun getAllPets(): Flow<List<Pet>> {
        return dao.getAllPets().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getPetByIdStream(id: Long): Flow<Pet?> {
        return dao.getPetByIdStream(id).map { it?.toDomainModel() }
    }

    override suspend fun getPetById(id: Long): Pet? {
        return dao.getPetById(id)?.toDomainModel()
    }

    override suspend fun insertPet(pet: Pet): Long {
        return dao.insertPet(PetEntity.fromDomainModel(pet))
    }

    override suspend fun updatePet(pet: Pet) {
        dao.updatePet(PetEntity.fromDomainModel(pet))
    }

    override suspend fun deletePet(pet: Pet) {
        dao.deletePet(PetEntity.fromDomainModel(pet))
    }
}
