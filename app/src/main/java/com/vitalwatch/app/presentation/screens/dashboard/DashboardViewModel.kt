package com.vitalwatch.app.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalwatch.app.ble.BleManager
import com.vitalwatch.app.ble.BleState
import com.vitalwatch.app.domain.model.InferenceResult
import com.vitalwatch.app.domain.model.Reading
import com.vitalwatch.app.domain.model.SyncStatus
import com.vitalwatch.app.domain.repository.AuthRepository
import com.vitalwatch.app.domain.repository.ReadingRepository
import com.vitalwatch.app.domain.usecase.DashboardData
import com.vitalwatch.app.domain.usecase.GetDashboardUseCase
import com.vitalwatch.app.inference.OnnxInferenceEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class DashboardUiState(
    val userName: String = "User",
    val latestReading: Reading? = null,
    val recentReadings: List<Reading> = emptyList(),
    val pendingCount: Int = 0,
    val isLoading: Boolean = true,
    val showMeasurementSheet: Boolean = false,
    val inferenceResult: InferenceResult? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardUseCase: GetDashboardUseCase,
    private val readingRepository: ReadingRepository,
    private val authRepository: AuthRepository,
    val bleManager: BleManager,
    private val inferenceEngine: OnnxInferenceEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val bleState: StateFlow<BleState> = bleManager.state

    init {
        observeDashboard()
        observeBleResults()
    }

    private fun observeDashboard() {
        viewModelScope.launch {
            getDashboardUseCase().collect { data ->
                _uiState.update {
                    it.copy(
                        userName = data.userName,
                        latestReading = data.latestReading,
                        recentReadings = data.recentReadings,
                        pendingCount = data.pendingCount,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeBleResults() {
        viewModelScope.launch {
            bleManager.state.collect { state ->
                if (state is BleState.MeasurementComplete) {
                    val features = state.features
                    val currentBleConnected = bleManager.state.value
                    val signalQuality = 85 // default; could come from device status char

                    val result = inferenceEngine.predict(features, signalQuality)
                    if (result != null) {
                        _uiState.update { it.copy(inferenceResult = result) }
                        // Save to DB
                        val session = authRepository.getCurrentUser()
                        val reading = Reading(
                            id = UUID.randomUUID().toString(),
                            patientId = session?.patientId ?: "local",
                            timestamp = features.timestamp,
                            glucoseMgdl = result.glucoseMgdl,
                            systolicMmhg = result.systolicMmhg,
                            diastolicMmhg = result.diastolicMmhg,
                            heartRateBpm = result.heartRateBpm,
                            spo2Percent = 98f,
                            signalQuality = signalQuality,
                            hrvFeaturesJson = features.toString(),
                            syncStatus = SyncStatus.PENDING,
                            syncedAt = null,
                            createdAt = System.currentTimeMillis()
                        )
                        readingRepository.saveReading(reading)
                    }
                }
            }
        }
    }

    fun startMeasurement() {
        _uiState.update { it.copy(showMeasurementSheet = true) }
        if (bleManager.state.value is BleState.Connected) {
            bleManager.startMeasurement()
        } else {
            bleManager.startScan()
        }
    }

    fun dismissMeasurementSheet() {
        _uiState.update { it.copy(showMeasurementSheet = false, inferenceResult = null) }
    }
}
