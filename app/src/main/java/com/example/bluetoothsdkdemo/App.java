package com.example.bluetoothsdkdemo;

import android.app.Application;

import com.clj.fastble.BleManager;
import com.example.bluetoothsdkdemo.ble.BluetoothManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 蓝牙模块初始化(Bluetooth module initialization)
        BluetoothManager.getInstance().init(this);
        BleManager.getInstance().init(this);
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(3, 5000)
                .setSplitWriteNum(20)
                .setConnectOverTime(30000)
                .setOperateTimeout(40000);
    }
}
