package our.bunny.julie.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import our.bunny.julie.domain.model.Medication

data class MedicationWithSchedules(
    @Embedded val medication: MedicationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "medicationId"
    )
    val schedules: List<MedicationScheduleEntity>
) {
    fun toDomainModel(): Medication {
        return Medication(
            id = medication.id,
            petId = medication.petId,
            name = medication.name,
            dosage = medication.dosage,
            medicationType = medication.medicationType,
            isActive = medication.isActive,
            notes = medication.notes,
            schedules = schedules.map { it.toDomainModel() }
        )
    }
}
