package com.vitalwatch.app.presentation.screens.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalwatch.app.ble.BleManager
import com.vitalwatch.app.ble.BleState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel @Inject constructor(
    val bleManager: BleManager
) : ViewModel() {

    val bleState: StateFlow<BleState> = bleManager.state

    fun startScan() {
        viewModelScope.launch { bleManager.startScan() }
    }

    fun stopScan() {
        bleManager.stopScan()
    }

    fun disconnect() {
        bleManager.disconnect()
    }

    fun isBluetoothEnabled(): Boolean = bleManager.isBluetoothEnabled()
}
