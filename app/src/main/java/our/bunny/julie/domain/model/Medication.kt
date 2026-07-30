package our.bunny.julie.domain.model

data class Medication(
    val id: Long = 0,
    val petId: Long,
    val name: String,
    val dosage: String,
    val medicationType: String,
    val isActive: Boolean,
    val notes: String,
    val schedules: List<MedicationSchedule>
)
