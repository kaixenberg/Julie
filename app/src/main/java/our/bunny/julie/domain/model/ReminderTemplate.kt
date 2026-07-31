package our.bunny.julie.domain.model

import java.time.LocalTime

sealed class ReminderTemplate {
    abstract val isEnabled: Boolean
    abstract val quietHoursEnabled: Boolean
}

data class WeightReminderTemplate(
    override val isEnabled: Boolean,
    override val quietHoursEnabled: Boolean,
    val intervalDays: Int
) : ReminderTemplate()

data class FeedingReminderTemplate(
    override val isEnabled: Boolean,
    override val quietHoursEnabled: Boolean,
    val scheduledTimes: List<LocalTime>
) : ReminderTemplate()

data class WaterReminderTemplate(
    override val isEnabled: Boolean,
    override val quietHoursEnabled: Boolean,
    val intervalHours: Int
) : ReminderTemplate()
