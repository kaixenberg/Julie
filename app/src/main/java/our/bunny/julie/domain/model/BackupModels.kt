package our.bunny.julie.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val schemaVersion: Int,
    val timestamp: String, // ISO-8601 string of when backup was created
    val pets: List<PetBackup>,
    val weightLogs: List<WeightBackup>,
    val waterLogs: List<WaterBackup>,
    val feedingLogs: List<FeedingBackup>,
    val medications: List<MedicationBackup>,
    val medicationSchedules: List<MedicationScheduleBackup>
)

@Serializable
data class PetBackup(
    val id: Long,
    val name: String,
    val species: String,
    val breed: String,
    val sex: String,
    val birthday: String?, // String representation of LocalDate
    val adoptionDate: String?, // String representation of LocalDate
    val weightUnit: String,
    val color: String,
    val notes: String,
    val microchipId: String,
    val photoUri: String?,
    val favoriteVet: String,
    val favoriteFood: String,
    val isArchived: Boolean
)

@Serializable
data class WeightBackup(
    val id: Long,
    val petId: Long,
    val date: String, // String
    val weight: Float,
    val notes: String
)

@Serializable
data class WaterBackup(
    val id: Long,
    val petId: Long,
    val amount: Float,
    val time: String // String
)

@Serializable
data class FeedingBackup(
    val id: Long,
    val petId: Long,
    val food: String,
    val quantity: String,
    val unit: String,
    val time: String, // String
    val calories: Int?,
    val notes: String,
    val type: String
)

@Serializable
data class MedicationBackup(
    val id: Long,
    val petId: Long,
    val name: String,
    val dosage: String,
    val medicationType: String,
    val isActive: Boolean,
    val notes: String
)

@Serializable
data class MedicationScheduleBackup(
    val id: Long,
    val medicationId: Long,
    val timeOfDay: String, // "HH:MM"
    val daysOfWeek: String // comma separated names
)
