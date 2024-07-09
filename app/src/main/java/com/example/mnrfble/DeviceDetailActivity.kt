package com.example.mnrfble

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dylanc.viewbinding.binding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.mnrfble.databinding.ActivityDeviceDetailBinding
import com.example.mnrfble.scanner.model.BleScanResult

@AndroidEntryPoint
class DeviceDetailActivity : AppCompatActivity() {
    private val TAG = DeviceDetailActivity::class.java.simpleName

    private val mBinding by binding<ActivityDeviceDetailBinding>()

    private val mAirPocketViewModel by viewModels<AirPocketViewModel>()

    private lateinit var mBleDevice: BleScanResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        initObserve()
        initEvent()
    }

    private fun initObserve() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mAirPocketViewModel.mConnectState.collect {
                    Log.e("TTTT", "连接状态1 -----  $it")
                }
            }
        }
    }

    private fun initEvent() {
        //Android-Ble-Library
        mBinding.button1.setOnClickListener {
            mAirPocketViewModel.connect(mBleDevice.device)
        }
        mBinding.button2.setOnClickListener {
            mAirPocketViewModel.disConnect()
        }
    }

    private fun initData() {
        mBleDevice = intent.getParcelableExtra(KEY_BLE_DEVICE)!!
    }

    companion object {
        private const val KEY_BLE_DEVICE = "key_ble_device"

        fun start(context: Context, bleScanResult: BleScanResult) {
            val intent = Intent(context, DeviceDetailActivity::class.java)
            intent.putExtra(KEY_BLE_DEVICE, bleScanResult)
            context.startActivity(intent)
        }
    }
}