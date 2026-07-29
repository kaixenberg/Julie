package our.bunny.julie.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import our.bunny.julie.domain.model.FeedingLog
import java.time.LocalDateTime

@Entity(
    tableName = "feeding_logs",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index("petId")]
)
data class FeedingLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val food: String,
    val quantity: String,
    val unit: String = "grams",
    val time: LocalDateTime,
    val calories: Int?,
    val notes: String,
    val type: String
) {
    fun toDomainModel(): FeedingLog {
        return FeedingLog(
            id = id,
            petId = petId,
            food = food,
            quantity = quantity,
            unit = unit,
            time = time,
            calories = calories,
            notes = notes,
            type = type
        )
    }

    companion object {
        fun fromDomainModel(model: FeedingLog): FeedingLogEntity {
            return FeedingLogEntity(
                id = model.id,
                petId = model.petId,
                food = model.food,
                quantity = model.quantity,
                unit = model.unit,
                time = model.time,
                calories = model.calories,
                notes = model.notes,
                type = model.type
            )
        }
    }
}
