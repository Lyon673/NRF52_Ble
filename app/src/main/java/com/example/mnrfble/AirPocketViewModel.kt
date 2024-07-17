package com.example.mnrfble

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.common.logger.BleLoggerAndLauncher
import no.nordicsemi.android.common.logger.DefaultBleLogger
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BondState
import com.example.mnrfble.repository.AirPocketRepository
import com.example.mnrfble.airPocket.BleDevicesCommand
import com.example.mnrfble.util.ByteUtils
import com.example.mnrfble.util.HexUtil
import java.util.UUID
import javax.inject.Inject
import com.example.mnrfble.databinding.ActivityDeviceDetailBinding


@SuppressLint("MissingPermission")
@HiltViewModel
class AirPocketViewModel @Inject constructor(
    @ApplicationContext context: Context, private val airPocketRepository: AirPocketRepository
) : AndroidViewModel(context as Application) {

    private val tag = "BleViewModel"


    //-------------------Android-BLE-Library---------------------------

    //设备连接状态
    val mConnectState = airPocketRepository.mConnectState


    fun connect(device: BluetoothDevice) {
        val exceptionHandler = CoroutineExceptionHandler { _, _ -> }
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            // This method may throw an exception if the connection fails,
            // Bluetooth is disabled, etc.
            // The exception will be caught by the exception handler and will be ignored.
            airPocketRepository.connect(device)
        }
    }

    fun disConnect() {
        airPocketRepository.disConnect()
    }

    fun writeCommand(text:String) {
        airPocketRepository.writeCommand(text)
    }

    fun passBinding(binding:ActivityDeviceDetailBinding){
        airPocketRepository.passBinding(binding)
    }



    override fun onCleared() {
        super.onCleared()
        airPocketRepository.release()
    }


}