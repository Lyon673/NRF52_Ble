package com.example.mnrfble.airPocket.callback

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data
import com.example.mnrfble.util.HexUtil
import com.dylanc.viewbinding.binding
import com.example.mnrfble.databinding.ActivityDeviceDetailBinding


class ResultCallback : ProfileReadResponse() {
    var data:String?=null
    //通知返回的数据
    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        this.data=HexUtil.hexStringToString(data.toString())
        Log.d("UART", "Result: DATA: ${HexUtil.hexStringToString(data.toString())} ")
    }
}