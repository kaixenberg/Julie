package our.bunny.julie.domain.model

data class Medication(
    val id: Long = 0,
    val petId: Long,
    val name: String,
    val dosage: String,
    val frequency: String,
    val timeOfDay: String, // e.g. "08:00"
    val isActive: Boolean,
    val notes: String
)
