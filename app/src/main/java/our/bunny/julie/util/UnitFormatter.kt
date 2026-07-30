package our.bunny.julie.util

enum class WeightUnit(val displayString: String) {
    KG("kg"),
    LBS("lbs")
}

enum class WaterUnit(val displayString: String) {
    ML("ml"),
    OZ("oz")
}

object UnitFormatter {
    // 1 kg = 2.20462 lbs
    private const val LBS_PER_KG = 2.20462f
    
    // 1 oz = 29.5735 ml
    private const val ML_PER_OZ = 29.5735f

    // Format weight from canonical KG to the requested display unit
    fun formatWeight(weightInKg: Float, unit: WeightUnit): String {
        val converted = when (unit) {
            WeightUnit.KG -> weightInKg
            WeightUnit.LBS -> weightInKg * LBS_PER_KG
        }
        return String.format("%.2f %s", converted, unit.displayString)
    }
    
    // Return numerical weight in display unit (useful for charts)
    fun getWeightInDisplayUnit(weightInKg: Float, unit: WeightUnit): Float {
        return when (unit) {
            WeightUnit.KG -> weightInKg
            WeightUnit.LBS -> weightInKg * LBS_PER_KG
        }
    }

    // Convert user input (in display unit) back to canonical KG for DB storage
    fun parseWeightToCanonical(inputWeight: Float, currentUnit: WeightUnit): Float {
        return when (currentUnit) {
            WeightUnit.KG -> inputWeight
            WeightUnit.LBS -> inputWeight / LBS_PER_KG
        }
    }

    // Format water from canonical ML to the requested display unit
    fun formatWater(waterInMl: Float, unit: WaterUnit): String {
        val converted = when (unit) {
            WaterUnit.ML -> waterInMl
            WaterUnit.OZ -> waterInMl / ML_PER_OZ
        }
        return String.format("%.2f %s", converted, unit.displayString)
    }
    
    // Return numerical water in display unit (useful for charts)
    fun getWaterInDisplayUnit(waterInMl: Float, unit: WaterUnit): Float {
        return when (unit) {
            WaterUnit.ML -> waterInMl
            WaterUnit.OZ -> waterInMl / ML_PER_OZ
        }
    }

    // Convert user input (in display unit) back to canonical ML for DB storage
    fun parseWaterToCanonical(inputWater: Float, currentUnit: WaterUnit): Float {
        return when (currentUnit) {
            WaterUnit.ML -> inputWater
            WaterUnit.OZ -> inputWater * ML_PER_OZ
        }
    }
}
