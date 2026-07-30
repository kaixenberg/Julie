package our.bunny.julie.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import our.bunny.julie.domain.model.Medication

@Entity(
    tableName = "medications",
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
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val name: String,
    val dosage: String,
    val medicationType: String,
    val isActive: Boolean,
    val notes: String
) {
    companion object {
        fun fromDomainModel(model: Medication): MedicationEntity {
            return MedicationEntity(
                id = model.id,
                petId = model.petId,
                name = model.name,
                dosage = model.dosage,
                medicationType = model.medicationType,
                isActive = model.isActive,
                notes = model.notes
            )
        }
    }
}
