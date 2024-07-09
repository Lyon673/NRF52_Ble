package com.example.mnrfble

/**
 * 负责扫描作用的控制入口，是一个主要的activity，也是app的MAIN
 */
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dylanc.viewbinding.binding
import dagger.hilt.android.AndroidEntryPoint
import com.example.mnrfble.databinding.ActivityScanBinding
import com.example.mnrfble.scanner.ScanDevicesAdapter
import com.example.mnrfble.scanner.ScannerViewModel
import com.example.mnrfble.scanner.ScanningState
import com.example.mnrfble.scanner.model.BleScanResult

@AndroidEntryPoint
class ScanActivity : AppCompatActivity() {

    private val TAG = ScanActivity::class.java.simpleName

    private val mBinding by binding<ActivityScanBinding>()
    private val mScannerViewModel by viewModels<ScannerViewModel>()

    private lateinit var mAdapter: ScanDevicesAdapter

    private val REQUEST_PERMISSIONS_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root) //设计UI
        initAdapter()
        initObserve()
        initEvent()
        checkPermissions()
    }

    //处理后台数据用的
    private fun initAdapter() {
        mAdapter = ScanDevicesAdapter()
        mBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        mBinding.recyclerView.adapter = mAdapter
    }

    //管理日志
    private fun initObserve() {
        mScannerViewModel.getBleScanResult.observe(this) { state ->
            Log.i(TAG, "$state")
            when (state) {
                is ScanningState.Loading -> {
                    // 处理加载状态
                }
                is ScanningState.DevicesDiscovered -> {
                    mAdapter.setList(state.devices)
                }
                is ScanningState.Error -> {
                    // 处理错误状态
                }
            }
        }
    }


    //回调
    private fun initEvent() {
        mBinding.actionScan.setOnClickListener {
            mAdapter.setList(null)
            mScannerViewModel.scan()
        }
        mBinding.actionStopScan.setOnClickListener {
            mScannerViewModel.stopScan()
        }
        mAdapter.setOnItemClickListener { adapter, _, position ->
            mScannerViewModel.stopScan()
            val device = adapter.getItem(position) as BleScanResult
            DeviceDetailActivity.start(this@ScanActivity, device)
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = mutableListOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            val permissionsToRequest = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

            if (permissionsToRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), REQUEST_PERMISSIONS_CODE)
            } else {
                // 所有权限已经被授予，继续你的操作
                registerBluetoothScanner()
            }
        } else {
            // 对于旧版本，不需要请求新的蓝牙权限
            registerBluetoothScanner()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (allGranted) {
                // 所有权限都被授予
                registerBluetoothScanner()
            } else {
                // 权限被拒绝，处理此情况
                Toast.makeText(this, "必要的权限被拒绝", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerBluetoothScanner() {
        // 你的注册 Bluetooth Scanner 的代码
    }
}
