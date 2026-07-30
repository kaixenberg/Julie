package our.bunny.julie.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class MedicationSchedule(
    val id: Long = 0,
    val timeOfDay: LocalTime,
    val daysOfWeek: Set<DayOfWeek>
)
