package our.bunny.julie.domain.model

import java.time.LocalDateTime

data class FeedingLog(
    val id: Long = 0,
    val petId: Long,
    val food: String,
    val quantity: String,
    val unit: String,
    val time: LocalDateTime,
    val calories: Int?,
    val notes: String,
    val type: String // Breakfast, Lunch, Dinner, Snack, Custom
)
