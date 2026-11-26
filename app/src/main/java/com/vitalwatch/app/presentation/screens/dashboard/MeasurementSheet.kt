package com.vitalwatch.app.presentation.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitalwatch.app.ble.BleState
import com.vitalwatch.app.domain.model.InferenceResult
import com.vitalwatch.app.presentation.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementSheet(
    bleState: BleState,
    inferenceResult: InferenceResult?,
    onDismiss: () -> Unit
) {
    var autoDismissCountdown by remember { mutableIntStateOf(5) }

    LaunchedEffect(inferenceResult) {
        if (inferenceResult != null) {
            while (autoDismissCountdown > 0) {
                delay(1000)
                autoDismissCountdown--
            }
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Measurement",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = bleState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "bleStateContent"
            ) { state ->
                when (state) {
                    is BleState.Disconnected -> BleStatusContent(
                        icon = Icons.Default.BluetoothDisabled,
                        title = "Disconnected",
                        subtitle = "Tap 'Start Measurement' to scan for your VitalWatch"
                    )
                    is BleState.Scanning -> BleStatusContent(
                        icon = Icons.Default.Bluetooth,
                        title = "Scanning...",
                        subtitle = "Looking for VitalWatch device",
                        showProgress = true
                    )
                    is BleState.Connecting -> BleStatusContent(
                        icon = Icons.Default.Bluetooth,
                        title = "Connecting...",
                        subtitle = "Connecting to ${state.deviceName}",
                        showProgress = true
                    )
                    is BleState.Connected -> BleStatusContent(
                        icon = Icons.Default.BluetoothConnected,
                        title = "Connected: ${state.deviceName}",
                        subtitle = "Battery: ${state.battery}% · Starting measurement..."
                    )
                    is BleState.MeasurementInProgress -> MeasurementProgressContent(progress = state.progress)
                    is BleState.MeasurementComplete -> {
                        if (inferenceResult != null) {
                            InferenceResultContent(result = inferenceResult, countdown = autoDismissCountdown)
                        } else {
                            BleStatusContent(icon = Icons.Default.CheckCircle, title = "Done!", subtitle = "Processing results...")
                        }
                    }
                    is BleState.Error -> BleStatusContent(
                        icon = Icons.Default.Error,
                        title = "Error",
                        subtitle = state.message,
                        isError = true
                    )
                }
            }
        }
    }
}

@Composable
private fun BleStatusContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    showProgress: Boolean = false,
    isError: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (showProgress) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
        }
    }
}

@Composable
private fun MeasurementProgressContent(progress: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Measuring...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(24.dp))
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.size(120.dp),
                strokeWidth = 8.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text("$progress%", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Keep your wrist still", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InferenceResultContent(result: InferenceResult, countdown: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(48.dp), tint = GlucoseNormal)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Results Ready", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ResultChip("Glucose", "${result.glucoseMgdl.toInt()} mg/dL")
            ResultChip("HR", "${result.heartRateBpm.toInt()} bpm")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ResultChip("Systolic", "${result.systolicMmhg.toInt()} mmHg")
            ResultChip("Diastolic", "${result.diastolicMmhg.toInt()} mmHg")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Auto-closing in ${countdown}s",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ResultChip(label: String, value: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}
