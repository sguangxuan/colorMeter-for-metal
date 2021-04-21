package com.example.bluetoothsdkdemo;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.example.bluetoothsdkdemo.adapter.BluetoothInfoAdapter;
import com.example.bluetoothsdkdemo.bean.BluetoothBean;
import com.example.bluetoothsdkdemo.bean.DisplayParam;
import com.example.bluetoothsdkdemo.bean.ToleranceBean;
import com.example.bluetoothsdkdemo.bean.parse.AdjustBean;
import com.example.bluetoothsdkdemo.bean.parse.DeviceInfoBean;
import com.example.bluetoothsdkdemo.bean.parse.MeasureBean;
import com.example.bluetoothsdkdemo.bean.parse.ReadLabMeasureDataBean;
import com.example.bluetoothsdkdemo.bean.parse.ReadMeasureDataBean;
import com.example.bluetoothsdkdemo.bean.parse.ReadRgbMeasureDataBean;
import com.example.bluetoothsdkdemo.bean.parse.StandardSampleDataBean;
import com.example.bluetoothsdkdemo.bean.parse.struct.DeviceInfoStruct;
import com.example.bluetoothsdkdemo.ble.BluetoothManager;
import com.example.bluetoothsdkdemo.databinding.ActivityMainBinding;
import com.example.bluetoothsdkdemo.databinding.ActivityTestBinding;
import com.example.bluetoothsdkdemo.util.Constant;
import com.example.bluetoothsdkdemo.util.TimeUtil;
import com.haoge.easyandroid.easy.EasyPermissions;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.kongzue.dialog.v3.CustomDialog;
import com.kongzue.dialog.v3.WaitDialog;

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestActivity extends BluetoothActivity {
    private ActivityTestBinding binding;
    private BluetoothClient mClient;
    private BluetoothInfoAdapter bluetoothInfoAdapter;
    private List<BluetoothBean> bluetoothBeanList = new ArrayList<>();
    private SearchRequest request;
    public String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_test);
        binding.setActivity(this);
        acceptPermissions();
        initBluetoothInfoAdapter();
        mClient = new BluetoothClient(this);
        request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .build();
    }

    /**
     * 申请权限(acceptPermissions)
     */
    private void acceptPermissions() {
        EasyPermissions.create(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ).request(this);
    }

    public void connect() {
        if (mClient.isBluetoothOpened()) {
            WaitDialog.show(TestActivity.this, "");
            WaitDialog.dismiss(9000);
            mClient.search(request, new SearchResponse() {
                @Override
                public void onSearchStarted() {

                }

                @Override
                public void onDeviceFounded(SearchResult device) {
                    Beacon beacon = new Beacon(device.scanRecord);
                    setBluetoothBeanList(device, beacon);
                }

                @Override
                public void onSearchStopped() {

                }

                @Override
                public void onSearchCanceled() {

                }
            });
        } else {
            mClient.openBluetooth();
        }

    }

    private void setBluetoothBeanList(SearchResult device, Beacon beacon) {
        BluetoothBean bluetoothBean = new BluetoothBean();
        bluetoothBean.setName(device.getName());
        bluetoothBean.setMac(device.getAddress());
        bluetoothBean.setRssi(String.valueOf(device.rssi));
        bluetoothBean.setPreParse(Arrays.toString(beacon.mBytes));
        boolean isRepeat = false;
        if (!device.getName().equals("NULL") && device.getName().contains("CM")) {
            for (int i = 0; i < bluetoothBeanList.size(); i++) {
                if (bluetoothBeanList.get(i).getMac().equals(device.getName())) {
                    isRepeat = true;
                }
            }
            if (!isRepeat) {
                bluetoothBeanList.add(bluetoothBean);
            }
        }
        bluetoothInfoAdapter.setNewData(removeDuplicate(bluetoothBeanList));
        bluetoothInfoAdapter.notifyDataSetChanged();
    }

    /**
     * 移除重复的设备名(Remove duplicate device name)
     *
     * @param list
     * @return
     */
    private List<BluetoothBean> removeDuplicate(List<BluetoothBean> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = list.size() - 1; j > i; j--) {
                if (list.get(j).getMac().equals(list.get(i).getMac())) {
                    list.remove(j);
                }
            }
        }
        return list;
    }

    private void initBluetoothInfoAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TestActivity.this);
        bluetoothInfoAdapter = new BluetoothInfoAdapter(R.layout.recyclerview_item_bluetooth_info);
        bluetoothInfoAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                BluetoothBean bluetoothBean = (BluetoothBean) adapter.getItem(position);
                mClient.stopSearch();
                connectBluetoothDevice(bluetoothBean.getMac());

                Intent intent = new Intent();
                intent.putExtra("result","success");     //把获取的值传到第一个页面
                setResult(RESULT_OK,intent);

                finish();
            }
        });
        binding.rvBluetoothInfo.setLayoutManager(linearLayoutManager);
        binding.rvBluetoothInfo.setAdapter(bluetoothInfoAdapter);
    }

    private void connectBluetoothDevice(String mac) {
        Log.i("deep", mac);
        BleManager.getInstance().connect(mac, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                showProgressDialog();
                Log.i("deep", "onStartConnect");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                WaitDialog.dismiss();
                Log.i("deep", "onConnectFail");
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                WaitDialog.dismiss();
                initDevice(bleDevice);
                Log.i("deep", "onConnectSuccess");
                Toast.makeText(TestActivity.this, getString(R.string.connect_success), Toast.LENGTH_SHORT);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                Log.i("deep", "onDisConnected");
            }
        });
    }

    private void showProgressDialog() {
        WaitDialog.show(TestActivity.this, getString(R.string.connect));
    }

    /**
     * 初始化设备(Initialize device)
     */
    private void initDevice(BleDevice bleDevice) {
        BluetoothManager.getInstance().connectDevice = bleDevice;
        Log.d("cjq", "onConnectSuccess");
        BluetoothManager.getInstance().setNotify();
        BluetoothManager.getInstance().connect_init = true;
    }

}