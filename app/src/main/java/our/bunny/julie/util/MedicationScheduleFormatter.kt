package our.bunny.julie.util

import our.bunny.julie.domain.model.MedicationSchedule
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek

object MedicationScheduleFormatter {
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    fun format(schedules: List<MedicationSchedule>): String {
        if (schedules.isEmpty()) return "No schedule"

        val timesByDays = schedules.groupBy { it.daysOfWeek }

        val parts = timesByDays.map { (days, scheds) ->
            val timesStr = scheds.map { it.timeOfDay.format(timeFormatter) }
                .sorted()
                .joinToString(", ")
            
            val daysStr = formatDays(days)
            "$timesStr • $daysStr"
        }

        return parts.joinToString(" | ")
    }

    private fun formatDays(days: Set<DayOfWeek>): String {
        if (days.size == 7) return "Every day"
        if (days.isEmpty()) return "No days"
        
        // Sort days logically starting from Monday
        val sortedDays = days.sortedBy { it.value }
        
        // Use short names
        val shortNames = sortedDays.map { 
            it.name.lowercase().replaceFirstChar { char -> char.uppercase() }.take(3) 
        }
        
        return shortNames.joinToString(", ")
    }
}
