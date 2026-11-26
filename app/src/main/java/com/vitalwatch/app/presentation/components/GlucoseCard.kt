package com.vitalwatch.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitalwatch.app.domain.model.GlucoseStatus
import com.vitalwatch.app.presentation.theme.*

@Composable
fun GlucoseCard(
    glucoseMgdl: Float,
    glucoseStatus: GlucoseStatus,
    heartRateBpm: Float,
    systolicMmhg: Float,
    diastolicMmhg: Float,
    spo2Percent: Float,
    modifier: Modifier = Modifier
) {
    val statusColor = when (glucoseStatus) {
        GlucoseStatus.LOW -> GlucoseLow
        GlucoseStatus.NORMAL -> GlucoseNormal
        GlucoseStatus.HIGH -> GlucoseHigh
        GlucoseStatus.CRITICAL -> GlucoseCritical
    }
    val statusLabel = when (glucoseStatus) {
        GlucoseStatus.LOW -> "Low"
        GlucoseStatus.NORMAL -> "Normal"
        GlucoseStatus.HIGH -> "High"
        GlucoseStatus.CRITICAL -> "Critical"
    }

    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsescale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            statusColor.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Blood Glucose",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = glucoseMgdl.toInt().toString(),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.scale(scale)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "mg/dL",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    StatusBadge(label = statusLabel, color = statusColor)
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    VitalChip("♥", "${heartRateBpm.toInt()} bpm")
                    VitalChip("BP", "${systolicMmhg.toInt()}/${diastolicMmhg.toInt()}")
                    VitalChip("SpO₂", "${spo2Percent.toInt()}%")
                }
            }
        }
    }
}

@Composable
private fun VitalChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}
