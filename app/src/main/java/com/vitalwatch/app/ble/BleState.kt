package com.vitalwatch.app.ble

import com.vitalwatch.app.domain.model.HRVFeatures

sealed class BleState {
    object Disconnected : BleState()
    object Scanning : BleState()
    data class Connecting(val deviceName: String) : BleState()
    data class Connected(val deviceName: String, val battery: Int) : BleState()
    data class MeasurementInProgress(val progress: Int) : BleState()
    data class MeasurementComplete(val features: HRVFeatures) : BleState()
    data class Error(val message: String) : BleState()
}
