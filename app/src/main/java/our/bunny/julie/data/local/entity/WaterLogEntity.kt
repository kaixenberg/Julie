package our.bunny.julie.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import our.bunny.julie.domain.model.WaterLog
import java.time.LocalDateTime

@Entity(
    tableName = "water_logs",
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
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val amount: Float,
    val unit: String,
    val time: LocalDateTime
) {
    fun toDomainModel(): WaterLog {
        return WaterLog(
            id = id,
            petId = petId,
            amount = amount,
            unit = unit,
            time = time
        )
    }

    companion object {
        fun fromDomainModel(model: WaterLog): WaterLogEntity {
            return WaterLogEntity(
                id = model.id,
                petId = model.petId,
                amount = model.amount,
                unit = model.unit,
                time = model.time
            )
        }
    }
}
