package our.bunny.julie.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import our.bunny.julie.domain.model.PetHealthReport
import our.bunny.julie.domain.model.TimelineEvent
import our.bunny.julie.util.UnitFormatter
import our.bunny.julie.util.WaterUnit
import our.bunny.julie.util.WeightUnit
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter

object PdfGenerator {
    
    suspend fun generateReport(context: Context, report: PetHealthReport, weightUnit: WeightUnit, waterUnit: WaterUnit): File = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint()
        var yPosition = 50f
        val margin = 50f

        // Title
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        canvas.drawText("Health Report: ${report.pet.name}", margin, yPosition, paint)
        
        yPosition += 30f
        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("${report.pet.species} • ${report.pet.breed}", margin, yPosition, paint)
        
        yPosition += 20f
        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        canvas.drawText("Generated on: ${java.time.LocalDate.now().format(dateFormatter)}", margin, yPosition, paint)

        yPosition += 40f
        paint.strokeWidth = 2f
        canvas.drawLine(margin, yPosition, 545f, yPosition, paint)

        // Medications Section
        yPosition += 40f
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Active Medications", margin, yPosition, paint)

        yPosition += 30f
        paint.textSize = 12f
        paint.isFakeBoldText = false
        if (report.medications.isEmpty()) {
            canvas.drawText("No active medications.", margin, yPosition, paint)
            yPosition += 20f
        } else {
            for (med in report.medications) {
                if (yPosition > 800f) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 50f
                }
                val status = if (med.isActive) "Active" else "Paused"
                canvas.drawText("• ${med.name} - ${med.dosage} (${med.frequency} at ${med.timeOfDay}) [$status]", margin + 10f, yPosition, paint)
                yPosition += 20f
                if (med.notes.isNotBlank()) {
                    canvas.drawText("  Notes: ${med.notes}", margin + 20f, yPosition, paint)
                    yPosition += 20f
                }
            }
        }

        // Charts Section
        yPosition += 40f
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Health Trends", margin, yPosition, paint)
        yPosition += 20f

        val weightEvents = report.timelineEvents.filterIsInstance<TimelineEvent.WeightEvent>().sortedBy { it.timestamp }
        val waterEvents = report.timelineEvents.filterIsInstance<TimelineEvent.WaterEvent>()

        if (weightEvents.isNotEmpty()) {
            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("Weight Trend", margin, yPosition, paint)
            yPosition += 10f

            val chartHeight = 80f
            val chartWidth = 495f
            val maxW = weightEvents.maxOf { it.entry.weight }
            val minW = weightEvents.minOf { it.entry.weight }
            val range = (maxW - minW).coerceAtLeast(1f)
            val stepX = chartWidth / (weightEvents.size - 1).coerceAtLeast(1).toFloat()

            paint.color = Color.parseColor("#4CAF50") // Green
            paint.strokeWidth = 2f
            paint.style = Paint.Style.STROKE
            
            val path = android.graphics.Path()
            weightEvents.forEachIndexed { index, event ->
                val px = margin + (index * stepX)
                val py = yPosition + chartHeight - (((event.entry.weight - minW) / range) * chartHeight)
                if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
                
                paint.style = Paint.Style.FILL
                canvas.drawCircle(px, py, 3f, paint)
                paint.style = Paint.Style.STROKE
            }
            canvas.drawPath(path, paint)
            paint.style = Paint.Style.FILL
            paint.color = Color.BLACK
            
            yPosition += chartHeight + 30f
        }

        if (waterEvents.isNotEmpty()) {
            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("Water Consumption (Last 7 Days)", margin, yPosition, paint)
            yPosition += 10f

            val chartHeight = 80f
            val chartWidth = 495f

            val now = java.time.LocalDateTime.now()
            val last7Days = (0..6).map { now.minusDays(it.toLong()).toLocalDate() }.reversed()
            val grouped = waterEvents.groupBy { it.timestamp.toLocalDate() }
            val dailyTotals = last7Days.map { date ->
                grouped[date]?.sumOf { it.log.amount.toDouble() }?.toFloat() ?: 0f
            }

            val maxAmount = dailyTotals.maxOrNull()?.coerceAtLeast(1f) ?: 1f
            val barWidth = chartWidth / (dailyTotals.size * 2f)
            val spacing = chartWidth / dailyTotals.size

            paint.color = Color.parseColor("#2196F3") // Blue
            
            dailyTotals.forEachIndexed { index, total ->
                val barH = (total / maxAmount) * chartHeight
                val px = margin + (index * spacing) + (spacing / 2f) - (barWidth / 2f)
                val py = yPosition + chartHeight - barH
                
                canvas.drawRect(px, py, px + barWidth, py + barH, paint)
            }
            paint.color = Color.BLACK
            
            yPosition += chartHeight + 30f
        }

        if (yPosition > 750f) {
            document.finishPage(page)
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPosition = 50f
        } else {
            paint.strokeWidth = 1f
            canvas.drawLine(margin, yPosition, 545f, yPosition, paint)
            yPosition += 20f
        }

        // Timeline Section
        yPosition += 40f
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Health Timeline", margin, yPosition, paint)

        yPosition += 30f
        paint.textSize = 12f
        paint.isFakeBoldText = false
        
        val timeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")

        if (report.timelineEvents.isEmpty()) {
            canvas.drawText("No health records found.", margin, yPosition, paint)
        } else {
            for (event in report.timelineEvents) {
                if (yPosition > 800f) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 50f
                }

                val timeStr = event.timestamp.format(timeFormatter)
                when (event) {
                    is TimelineEvent.WeightEvent -> {
                        val formattedWeight = UnitFormatter.formatWeight(event.entry.weight, weightUnit)
                        canvas.drawText("[$timeStr] Weight: $formattedWeight", margin + 10f, yPosition, paint)
                        yPosition += 15f
                        if (event.entry.notes.isNotBlank()) {
                            canvas.drawText("  Notes: ${event.entry.notes}", margin + 20f, yPosition, paint)
                            yPosition += 15f
                        }
                    }
                    is TimelineEvent.FeedingEvent -> {
                        canvas.drawText("[$timeStr] Feeding: ${event.log.food} (${event.log.quantity} ${event.log.unit} • ${event.log.type})", margin + 10f, yPosition, paint)
                        yPosition += 15f
                        if (event.log.notes.isNotBlank()) {
                            canvas.drawText("  Notes: ${event.log.notes}", margin + 20f, yPosition, paint)
                            yPosition += 15f
                        }
                    }
                    is TimelineEvent.WaterEvent -> {
                        val formattedWater = UnitFormatter.formatWater(event.log.amount, waterUnit)
                        canvas.drawText("[$timeStr] Water: $formattedWater", margin + 10f, yPosition, paint)
                        yPosition += 15f
                    }
                }
                yPosition += 5f
            }
        }

        document.finishPage(page)

        val file = File(context.cacheDir, "Julie_Health_Report_${report.pet.name}.pdf")
        FileOutputStream(file).use {
            document.writeTo(it)
        }
        document.close()

        file
    }
}
