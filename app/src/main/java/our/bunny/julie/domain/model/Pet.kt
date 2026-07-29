package our.bunny.julie.domain.model

import java.time.LocalDate

data class Pet(
    val id: Long = 0,
    val name: String,
    val species: String,
    val breed: String,
    val sex: String,
    val birthday: LocalDate?,
    val adoptionDate: LocalDate?,
    val weightUnit: String, // e.g., "kg", "lbs"
    val color: String,
    val notes: String,
    val microchipId: String,
    val photoUri: String?,
    val favoriteVet: String,
    val favoriteFood: String,
    val isArchived: Boolean = false
)
