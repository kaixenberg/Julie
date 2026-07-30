package our.bunny.julie.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalTime
import our.bunny.julie.domain.model.MedicationSchedule

@Entity(
    tableName = "medication_schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index("medicationId")]
)
data class MedicationScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val timeOfDay: String, // "HH:mm"
    val daysOfWeek: String // comma separated names of java.time.DayOfWeek
) {
    fun toDomainModel(): MedicationSchedule {
        val days = if (daysOfWeek.isBlank()) {
            emptySet()
        } else {
            daysOfWeek.split(",").map { DayOfWeek.valueOf(it) }.toSet()
        }
        return MedicationSchedule(
            id = id,
            timeOfDay = LocalTime.parse(timeOfDay),
            daysOfWeek = days
        )
    }

    companion object {
        fun fromDomainModel(medicationId: Long, model: MedicationSchedule): MedicationScheduleEntity {
            return MedicationScheduleEntity(
                id = model.id,
                medicationId = medicationId,
                timeOfDay = model.timeOfDay.toString(), // format HH:mm
                daysOfWeek = model.daysOfWeek.joinToString(",") { it.name }
            )
        }
    }
}
