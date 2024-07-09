package com.example.mnrfble.airPocket.callback

import android.bluetooth.BluetoothDevice
import android.util.Log
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data
import com.example.mnrfble.util.HexUtil


class ResultCallback : ProfileReadResponse() {
    //通知返回的数据
    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        Log.d("UART", "Result: DATA: ${HexUtil.hexStringToString(data.toString())} ")
    }
}