package our.bunny.julie.ui.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import our.bunny.julie.domain.model.TimelineEvent
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onNavigateUp: () -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Timeline") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.events.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No events recorded yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.events) { event ->
                    TimelineEventItem(event)
                }
            }
        }
    }
}

@Composable
fun TimelineEventItem(event: TimelineEvent) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        // Icon
        val iconAndColor: Pair<ImageVector, Color> = when (event) {
            is TimelineEvent.WeightEvent -> Pair(Icons.Default.Star, Color(0xFF4CAF50))
            is TimelineEvent.FeedingEvent -> Pair(Icons.Default.CheckCircle, Color(0xFFFF9800))
            is TimelineEvent.WaterEvent -> Pair(Icons.Default.Info, Color(0xFF2196F3))
        }
        val icon = iconAndColor.first
        val color = iconAndColor.second

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            val title = when (event) {
                is TimelineEvent.WeightEvent -> "Weight Logged: ${event.entry.weight} ${event.entry.unit}"
                is TimelineEvent.FeedingEvent -> "Meal Logged: ${event.log.food}"
                is TimelineEvent.WaterEvent -> "Water Logged: ${event.log.amount} ${event.log.unit}"
            }

            val subtitle = when (event) {
                is TimelineEvent.WeightEvent -> event.entry.notes
                is TimelineEvent.FeedingEvent -> "${event.log.quantity} ${event.log.unit} • ${event.log.type}" + if (event.log.notes.isNotBlank()) "\n${event.log.notes}" else ""
                is TimelineEvent.WaterEvent -> ""
            }

            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = event.timestamp.format(formatter), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
