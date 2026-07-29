package our.bunny.julie.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import our.bunny.julie.domain.model.Pet
import java.time.LocalDate

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val species: String,
    val breed: String,
    val sex: String,
    val birthday: LocalDate?,
    val adoptionDate: LocalDate?,
    val weightUnit: String,
    val color: String,
    val notes: String,
    val microchipId: String,
    val photoUri: String?,
    val favoriteVet: String,
    val favoriteFood: String,
    val isArchived: Boolean = false
) {
    fun toDomainModel(): Pet {
        return Pet(
            id = id,
            name = name,
            species = species,
            breed = breed,
            sex = sex,
            birthday = birthday,
            adoptionDate = adoptionDate,
            weightUnit = weightUnit,
            color = color,
            notes = notes,
            microchipId = microchipId,
            photoUri = photoUri,
            favoriteVet = favoriteVet,
            favoriteFood = favoriteFood,
            isArchived = isArchived
        )
    }

    companion object {
        fun fromDomainModel(pet: Pet): PetEntity {
            return PetEntity(
                id = pet.id,
                name = pet.name,
                species = pet.species,
                breed = pet.breed,
                sex = pet.sex,
                birthday = pet.birthday,
                adoptionDate = pet.adoptionDate,
                weightUnit = pet.weightUnit,
                color = pet.color,
                notes = pet.notes,
                microchipId = pet.microchipId,
                photoUri = pet.photoUri,
                favoriteVet = pet.favoriteVet,
                favoriteFood = pet.favoriteFood,
                isArchived = pet.isArchived
            )
        }
    }
}
