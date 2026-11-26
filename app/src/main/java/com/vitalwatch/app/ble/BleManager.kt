package com.vitalwatch.app.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@SuppressLint("MissingPermission")
class BleManager @Inject constructor(
    private val context: Context,
    private val bluetoothManager: BluetoothManager
) {
    private val tag = "BleManager"

    private val _state = MutableStateFlow<BleState>(BleState.Disconnected)
    val state: StateFlow<BleState> = _state.asStateFlow()

    private var gattClient: VitalWatchGatt? = null
    private var scanCallback: ScanCallback? = null

    private val scanner get() = bluetoothManager.adapter?.bluetoothLeScanner

    fun startScan() {
        if (_state.value is BleState.Scanning) return
        _state.value = BleState.Scanning

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(VitalWatchUUIDs.SERVICE_UUID))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                Log.d(tag, "Found device: ${result.device.name}")
                stopScan()
                connectToDevice(result.device)
            }
            override fun onScanFailed(errorCode: Int) {
                _state.value = BleState.Error("Scan failed: $errorCode")
            }
        }
        scanner?.startScan(listOf(filter), settings, scanCallback)
    }

    fun stopScan() {
        scanCallback?.let { scanner?.stopScan(it) }
        scanCallback = null
    }

    fun connectToDevice(device: BluetoothDevice) {
        gattClient?.disconnect()
        gattClient = VitalWatchGatt(
            context = context,
            device = device,
            stateFlow = _state,
            onMeasurementComplete = { features ->
                _state.value = BleState.MeasurementComplete(features)
            }
        )
        gattClient?.connect()
    }

    fun startMeasurement() {
        if (_state.value !is BleState.Connected) {
            _state.value = BleState.Error("Device not connected")
            return
        }
        gattClient?.startMeasurement()
    }

    fun disconnect() {
        stopScan()
        gattClient?.disconnect()
        gattClient = null
        _state.value = BleState.Disconnected
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothManager.adapter?.isEnabled == true
    }
}
