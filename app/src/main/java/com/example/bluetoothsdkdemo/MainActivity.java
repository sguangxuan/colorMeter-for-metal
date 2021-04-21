package com.example.bluetoothsdkdemo;


import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.icu.math.BigDecimal;
import android.os.Build;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.os.SystemClock.sleep;

public class MainActivity extends BluetoothActivity {
    private ActivityMainBinding binding;
    private BluetoothClient mClient;
    private BluetoothInfoAdapter bluetoothInfoAdapter;
    private List<BluetoothBean> bluetoothBeanList = new ArrayList<>();
    private SearchRequest request;
    public String msg;
    public String deviceState;
    public String result;
    public int color;
    public int intentVis = View.VISIBLE;
    public int measureVis = View.VISIBLE;
    public int adjustVis = View.VISIBLE;
    private PlaybackStatus mPlaybackStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setActivity(this);
        acceptPermissions();
        initBluetoothInfoAdapter();
        mClient = new BluetoothClient(this);
        request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .build();

        mPlaybackStatus = new PlaybackStatus();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ON_FAIL);
        registerReceiver(mPlaybackStatus, filter);
        intentTestActivity();
    }

    @Override
    public void onBlackAdjust(AdjustBean bean) {
        super.onBlackAdjust(bean);
        msg = bean.toString();
        binding.tvContent.setText(msg);
    }

    @Override
    public void onWhiteAdjust(AdjustBean bean) {
        super.onWhiteAdjust(bean);
        msg = bean.toString();
        binding.tvContent.setText(msg);
    }

    @Override
    public void onMeasure(MeasureBean bean) {
        super.onMeasure(bean);
        getMeasureData();
        msg = bean.toString();
        binding.tvContent.setText(msg);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReadMeasureData(ReadMeasureDataBean bean) {
        super.onReadMeasureData(bean);
        Log.i("Lab", String.format("onReadMeasureData: %f， %f， %f", bean.getLabData()[0], bean.getLabData()[1], bean.getLabData()[2]));
        // 这里这里测到了Lab的值 bean.getLabData()[0], bean.getLabData()[1], bean.getLabData()[2]！！！！！！！!!!!!!!!!
        msg = bean.toString();
        binding.tvContent.setText(msg);

        String level = "";
        double value;
        value = (92.782 - 0.706 * bean.getLabData()[0] + 3.403 * bean.getLabData()[1] - 1.715 * bean.getLabData()[2]);
        BigDecimal tmp = new BigDecimal(value);
        value = tmp.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (value < 0){
            value = 0;
        }else if(value > 100){
            value = 100;
        }
        if (value <= 21){
            level = "第一阶段";
        }else if(value>21 && value <= 64){
            level = "第二阶段";
        }else if(value>64 && value <= 70){
            level = "第三阶段";
        }else if(value>70 && value <= 88){
            level = "第四阶段";
        }else if(value>88){
            level = "第五阶段";
        }
        Log.i("measure", "onReadMeasureData: value" + value + level);

        result = "α-FeOOH的含量(%)\n" + value + "\n\n腐蚀阶段处于: " + level;
        binding.tvResult.setText(result);

        int[] RGB = new int[3];
        double[] Lab = new double[3];
        Lab[0] = bean.getLabData()[0];Lab[1] = bean.getLabData()[1];Lab[2] = bean.getLabData()[2];
        RGB = XYZ2sRGB(Lab2XYZ(Lab));
        color = Color.rgb(RGB[0], RGB[1], RGB[2]);
        binding.tvColor.setBackgroundColor(color);
    }

    public static double[] Lab2XYZ(double[] Lab) {
        double[] XYZ = new double[3];
        double L, a, b;
        double fx, fy, fz;
        double Xn, Yn, Zn;
        Xn = 95.04;
        Yn = 100;
        Zn = 108.89;

        L = Lab[0];
        a = Lab[1];
        b = Lab[2];

        fy = (L + 16) / 116;
        fx = a / 500 + fy;
        fz = fy - b / 200;

        if (fx > 0.2069) {
            XYZ[0] = Xn * Math.pow(fx, 3);
        } else {
            XYZ[0] = Xn * (fx - 0.1379) * 0.1284;
        }

        if ((fy > 0.2069) || (L > 8)) {
            XYZ[1] = Yn * Math.pow(fy, 3);
        } else {
            XYZ[1] = Yn * (fy - 0.1379) * 0.1284;
        }

        if (fz > 0.2069) {
            XYZ[2] = Zn * Math.pow(fz, 3);
        } else {
            XYZ[2] = Zn * (fz - 0.1379) * 0.1284;
        }

        return XYZ;
    }

    public static int[] XYZ2sRGB(double[] XYZ) {
        int[] sRGB = new int[3];
        double X, Y, Z;
        double dr, dg, db;
        X = XYZ[0];
        Y = XYZ[1];
        Z = XYZ[2];

        dr = 0.032406 * X - 0.015371 * Y - 0.0049895 * Z;
        dg = -0.0096891 * X + 0.018757 * Y + 0.00041914 * Z;
        db = 0.00055708 * X - 0.0020401 * Y + 0.01057 * Z;

        if (dr <= 0.00313) {
            dr = dr * 12.92;
        } else {
            dr = Math.exp(Math.log(dr) / 2.4) * 1.055 - 0.055;
        }

        if (dg <= 0.00313) {
            dg = dg * 12.92;
        } else {
            dg = Math.exp(Math.log(dg) / 2.4) * 1.055 - 0.055;
        }

        if (db <= 0.00313) {
            db = db * 12.92;
        } else {
            db = Math.exp(Math.log(db) / 2.4) * 1.055 - 0.055;
        }

        dr = dr * 255;
        dg = dg * 255;
        db = db * 255;

        dr = Math.min(255, dr);
        dg = Math.min(255, dg);
        db = Math.min(255, db);

        sRGB[0] = (int) (dr + 0.5);
        sRGB[1] = (int) (dg + 0.5);
        sRGB[2] = (int) (db + 0.5);

        return sRGB;
    }

    @Override
    public void onReadLabMeasureData(ReadLabMeasureDataBean bean) {
        super.onReadLabMeasureData(bean);
        msg = bean.toString();
        binding.tvContent.setText(msg);
    }

    @Override
    public void onReadRgbMeasureData(ReadRgbMeasureDataBean bean) {
        super.onReadRgbMeasureData(bean);
        msg = bean.toString();
        binding.tvContent.setText(msg);
    }

    @Override
    public void onGetStandardCount(short count) {
        super.onGetStandardCount(count);
        msg = String.format("getStandardCount===>%d",count);
        binding.tvContent.setText(msg);
    }

    @Override
    public void onGetStandardDataForNumber(StandardSampleDataBean.StandardDataBean standardDataBean) {
        super.onGetStandardDataForNumber(standardDataBean);
        msg = standardDataBean.toString();
        binding.tvContent.setText(msg);
    }

    @Override
    public void onDeleteAllStandardData(byte state) { // state  0-删除成功(success) 1-删除失败(fail)
        super.onDeleteAllStandardData(state);
        msg = String.format("onDeleteAllStandardData===>state===>%d",state);
        binding.tvContent.setText(msg);
    }

    @Override
    public void onDeleteStandardDataForNumber(short standardNum, byte state) { // state  0-删除成功(success) 1-删除失败(fail) 2-删除失败，超出界限(out of range)
        super.onDeleteStandardDataForNumber(standardNum, state);
        msg = String.format("onDeleteStandardDataForNumber===>index===>%d===>state===>%d",standardNum,state);
        binding.tvContent.setText(msg);
    }

    @Override
    public void onPostStandardData(byte state) { // state 0-下载成功(success) 1-下载失败(fail)
        super.onPostStandardData(state);
        msg = String.format("onPostStandardData===>state===>%d",state);
        binding.tvContent.setText(msg);
    }

    @Override
    public void onGetDeviceInfo(DeviceInfoStruct deviceInfoStruct) {
        super.onGetDeviceInfo(deviceInfoStruct);
        msg = deviceInfoStruct.toString();
        binding.tvContent.setText(msg);
    }

    @Override
    public void onGetDevicePowerInfo(DeviceInfoBean.PowerInfo powerInfo) {
        super.onGetDevicePowerInfo(powerInfo);
        msg = String.format("onGetDevicePowerInfo===>%d",powerInfo.getElectricQuantity());
        binding.tvContent.setText(msg);
    }

    @Override
    public void onGetDeviceAdjustState(DeviceInfoBean.DeviceAdjustState deviceAdjustState) {
        super.onGetDeviceAdjustState(deviceAdjustState);
        msg = deviceAdjustState.toString();
        binding.tvContent.setText(msg);
    }

    @Override
    public void onSetDeviceDisplayParam(byte state) { // 0-设置成功(success) 1-设置失败(fail)
        super.onSetDeviceDisplayParam(state);
        msg = String.format("onSetDeviceDisplayParam===>state===>%d",state);
        binding.tvContent.setText(msg);
    }

    @Override
    public void onSetTolerance(byte state) { // 0-设置成功(success) 1-设置失败(fail)
        super.onSetTolerance(state);
        msg = String.format("onSetTolerance===>state===>%d",state);
        binding.tvContent.setText(msg);
    }

    @Override
    public void onSetDeviceTime(byte state) { // 0-设置成功(success) 1-设置失败(fail)
        super.onSetDeviceTime(state);
        msg = String.format("onSetDeviceTime===>state===>%d",state);
        binding.tvContent.setText(msg);
    }

    @Override
    public void onFail(String failType) { // failType -- 命令标识(cmd tag)
        super.onFail(failType);
        msg = String.format("onFail===>state===>"+failType);
        binding.tvContent.setText(msg);
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

    private void initBluetoothInfoAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        bluetoothInfoAdapter = new BluetoothInfoAdapter(R.layout.recyclerview_item_bluetooth_info);
        bluetoothInfoAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                BluetoothBean bluetoothBean = (BluetoothBean) adapter.getItem(position);
                mClient.stopSearch();
                connectBluetoothDevice(bluetoothBean.getMac());
            }
        });
    }

    private void showProgressDialog() {
        WaitDialog.show(MainActivity.this, getString(R.string.connect));
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
                Toast.makeText(MainActivity.this, getString(R.string.connect_success), Toast.LENGTH_SHORT);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                Log.i("deep", "onDisConnected");
            }
        });
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

    public void intentTestActivity(){
        Intent intent = new Intent(this, TestActivity.class);
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK){       //判断接口是否符合，符合接受传值
            //接受第二个页面的值，然后设置给某个控件
            deviceState = "连接成功";
            binding.tvDeviceState.setText(deviceState);
            intentVis = View.INVISIBLE;
            binding.btnIntent.setVisibility(intentVis);
            adjustVis = View.VISIBLE;
            binding.btnAdjust.setVisibility(adjustVis);
            measureVis = View.VISIBLE;
            binding.btnMeasure.setVisibility(measureVis);
            intentAdjustActivity();
        }else {
            deviceState = "设备情况未知，建议重新连接";
            binding.tvDeviceState.setText(deviceState);
            intentVis = View.VISIBLE;
            binding.btnIntent.setVisibility(intentVis);
            adjustVis = View.VISIBLE;
            binding.btnAdjust.setVisibility(adjustVis);
            measureVis = View.VISIBLE;
            binding.btnMeasure.setVisibility(measureVis);
        }
    }

    public void intentAdjustActivity(){
        Intent intent = new Intent(this, AdjustActivity.class);
        startActivity(intent);
    }

    public void search() {
        if (mClient.isBluetoothOpened()) {
            WaitDialog.show(MainActivity.this, "");
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

    public void blackAdjust() {
        BluetoothManager.getInstance().setOrder(Constant.BLACK_ADJUST);
    }

    public void whiteAdjust() {
        BluetoothManager.getInstance().setOrder(Constant.WHITE_ADJUST);
    }

    public void measure() {
        final int measureMode;
        measureMode = Constant.TYPE_MEASURE_MODE_SCI;
        BluetoothManager.getInstance().measureMode = measureMode;
        BluetoothManager.getInstance().setOrder(Constant.MEASURE);


        //sleep(3000);

//        CustomDialog.show(MainActivity.this, R.layout.dialog_measure, new CustomDialog.OnBindView() {
//            @Override
//            public void onBind(CustomDialog dialog, View v) {
//                Button btnSend = v.findViewById(R.id.btn_send);
//                final RadioButton rbSci = v.findViewById(R.id.rb_sci);
//                final RadioButton rbSce = v.findViewById(R.id.rb_sce);
//                final RadioButton rbSciSce = v.findViewById(R.id.rb_sci_sce);
//                rbSci.setChecked(true);
//                btnSend.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        final int measureMode;
//                        if (rbSci.isChecked()) {
//                            measureMode = Constant.TYPE_MEASURE_MODE_SCI;
//                        } else if (rbSce.isChecked()) {
//                            measureMode = Constant.TYPE_MEASURE_MODE_SCE;
//                        } else if (rbSciSce.isChecked()) {
//                            measureMode = Constant.TYPE_MEASURE_MODE_SCI_SCE;
//                        } else {
//                            measureMode = Constant.TYPE_MEASURE_MODE_SCI;
//                        }
//                        BluetoothManager.getInstance().measureMode = measureMode;
//                        BluetoothManager.getInstance().setOrder(Constant.MEASURE);
//                    }
//                });
//
//            }
//        });

    }

    public void getMeasureData() {
        final int measureMode;
        measureMode = Constant.TYPE_MEASURE_MODE_SCI_NEW;
        BluetoothManager.getInstance().measureMode = measureMode;
        BluetoothManager.getInstance().setOrder(Constant.READ_MEASURE_DATA);
//        CustomDialog.show(MainActivity.this, R.layout.dialog_read_measure_data, new CustomDialog.OnBindView() {
//            @Override
//            public void onBind(CustomDialog dialog, View v) {
//                Button btnSend = v.findViewById(R.id.btn_send);
//                final RadioButton rbSci = v.findViewById(R.id.rb_sci);
//                final RadioButton rbSce = v.findViewById(R.id.rb_sce);
//                rbSci.setChecked(true);
//                btnSend.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        final int measureMode;
//                        if (rbSci.isChecked()) {
//                            measureMode = Constant.TYPE_MEASURE_MODE_SCI_NEW;
//                        } else if (rbSce.isChecked()) {
//                            measureMode = Constant.TYPE_MEASURE_MODE_SCE_NEW;
//                        } else {
//                            measureMode = Constant.TYPE_MEASURE_MODE_SCI_NEW;
//                        }
//                        BluetoothManager.getInstance().measureMode = measureMode;
//                        BluetoothManager.getInstance().setOrder(Constant.READ_MEASURE_DATA);
//                    }
//                });
//            }
//        });

    }

    public void getLabMeasureData() {
        CustomDialog.show(MainActivity.this, R.layout.dialog_read_measure_data, new CustomDialog.OnBindView() {
            @Override
            public void onBind(CustomDialog dialog, View v) {
                Button btnSend = v.findViewById(R.id.btn_send);
                final RadioButton rbSci = v.findViewById(R.id.rb_sci);
                final RadioButton rbSce = v.findViewById(R.id.rb_sce);
                rbSci.setChecked(true);
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int measureMode, lightSource, angle;
                        if (rbSci.isChecked()) {
                            measureMode = Constant.TYPE_MEASURE_MODE_SCI;
                        } else if (rbSce.isChecked()) {
                            measureMode = Constant.TYPE_MEASURE_MODE_SCE;
                        } else {
                            measureMode = Constant.TYPE_MEASURE_MODE_SCI;
                        }
                        BluetoothManager.getInstance().measureMode = measureMode;
                        BluetoothManager.getInstance().setOrder(Constant.READ_LAB_MEASURE_DATA);
                    }
                });
            }
        });

    }

    public void getRgbMeasureData() {
        CustomDialog.show(MainActivity.this, R.layout.dialog_read_measure_data, new CustomDialog.OnBindView() {
            @Override
            public void onBind(CustomDialog dialog, View v) {
                Button btnSend = v.findViewById(R.id.btn_send);
                final RadioButton rbSci = v.findViewById(R.id.rb_sci);
                final RadioButton rbSce = v.findViewById(R.id.rb_sce);
                rbSci.setChecked(true);
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int measureMode;
                        if (rbSci.isChecked()) {
                            measureMode = Constant.TYPE_MEASURE_MODE_SCI;
                        } else if (rbSce.isChecked()) {
                            measureMode = Constant.TYPE_MEASURE_MODE_SCE;
                        } else {
                            measureMode = Constant.TYPE_MEASURE_MODE_SCI;
                        }
                        BluetoothManager.getInstance().measureMode = measureMode;
                        BluetoothManager.getInstance().setOrder(Constant.READ_RGB_MEASURE_DATA);
                    }
                });

            }
        });

    }

    public void getStandardDataCount() {
        BluetoothManager.getInstance().setOrder(Constant.GET_STANDARD_DATA_COUNT);
    }

    /**
     * 先获取标样数量再发送此命令可以获取全部数据
     * (First get the number of standard samples and then send this command to get all the data)
     */
    public void getStandardDataIndex() {
        BluetoothManager.getInstance().index = 0;
        BluetoothManager.getInstance().setOrder(Constant.GET_STANDARD_DATA_FOR_NUM);
    }

    public void deleteAllData() {
        BluetoothManager.getInstance().setOrder(Constant.DELETE_ALL_STANDARD_DATA);
    }

    public void deleteStandardDataIndex() {
        CustomDialog.show(MainActivity.this, R.layout.dialog_delete_standard_data_for_num, new CustomDialog.OnBindView() {
            @Override
            public void onBind(CustomDialog dialog, View v) {
                final EditText etStandardNum = v.findViewById(R.id.et_standard_num);
                etStandardNum.setText("0");
                Button btnSend = v.findViewById(R.id.btn_send);
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        short standardNum = Short.parseShort(etStandardNum.getText().toString().trim());
                        BluetoothManager.getInstance().index = standardNum;
                        BluetoothManager.getInstance().setOrder(Constant.DELETE_ALL_SAMPLE_FOR_STANDARD_NUM);
                    }
                });
            }
        });

    }

    public void getDeviceInfo() {
        BluetoothManager.getInstance().setOrder(Constant.GET_DEVICE_INFO);
    }

    public void getAdjustState() {
        BluetoothManager.getInstance().setOrder(Constant.GET_DEVICE_ADJUST_STATE);
    }

    public void syncTime() {
        BluetoothManager.getInstance().setOrder(Constant.SET_DEVICE_TIME);
    }

    public void getDevicePowerInfo() {
        BluetoothManager.getInstance().setOrder(Constant.GET_DEVICE_POWER_INFO);
    }

    public void postDataToDevice() {
        List<Float> dataSCI = new ArrayList<>();
        List<Float> dataSCE = new ArrayList<>();
        for (int i = 0; i < 43; i++) {
            dataSCI.add(2f);
            dataSCE.add(2f);
        }
        StandardSampleDataBean.StandardDataBean standardDataBean = new StandardSampleDataBean.StandardDataBean();
        standardDataBean.setState((byte) 0x11);
        standardDataBean.setAngle((byte) 0x83);
        standardDataBean.setLightSource((byte) 0x83);
        standardDataBean.setMeasureMode((byte) 0x00);
        standardDataBean.setDataMode((byte) 0x00);
        standardDataBean.setStartWave((short) 400);
        standardDataBean.setWaveLength((byte) 0x0a);
        standardDataBean.setWaveCount((byte) 31);
        standardDataBean.setName("123");
        standardDataBean.setL((float) 0.1);
        standardDataBean.setA((float) 0.1);
        standardDataBean.setB((float) 0.1);
        standardDataBean.setDataSCI(dataSCI);
        standardDataBean.setDataSCE(dataSCE);
        standardDataBean.setRecordCount(0);
        standardDataBean.setTime(TimeUtil.getUnixTime());
        BluetoothManager.getInstance().standardDataBean = standardDataBean;
        BluetoothManager.getInstance().setOrder(Constant.POST_STANDARD_DATA);
    }

    public void setDeviceDisplayParam() {
        CustomDialog.show(MainActivity.this, R.layout.dialog_set_device_display_param, new CustomDialog.OnBindView() {
            @Override
            public void onBind(CustomDialog dialog, View v) {
                Button btnSend = v.findViewById(R.id.btn_send);
                final NiceSpinner nsMeasureMode = v.findViewById(R.id.ns_measure_mode);
                final NiceSpinner nsLightSourece = v.findViewById(R.id.ns_light_source);
                final NiceSpinner nsSecondLightSource = v.findViewById(R.id.ns_second_light_source);
                final NiceSpinner nsAngle = v.findViewById(R.id.ns_angle);
                final NiceSpinner nsSecondAngle = v.findViewById(R.id.ns_second_angle);
                final NiceSpinner nsColorSpace = v.findViewById(R.id.ns_color_space);
                final NiceSpinner nsColorDiff = v.findViewById(R.id.ns_color_diff);
                //设置默认参数
                nsMeasureMode.setSelectedIndex(0);//SCI
                nsLightSourece.setSelectedIndex(4);//D65
                nsSecondLightSource.setSelectedIndex(4);//D65
                nsAngle.setSelectedIndex(1);//10°
                nsSecondAngle.setSelectedIndex(1);//10°
                nsColorSpace.setSelectedIndex(0);//lab(CIE)
                nsColorDiff.setSelectedIndex(0);//dE*ab
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int measureMode = getMeasureMode(nsMeasureMode.getSelectedIndex());
                        int lightSource = getLightSource(nsLightSourece.getSelectedIndex());
                        int secondLightSource = getLightSource(nsSecondLightSource.getSelectedIndex());
                        int angle = getAngle(nsAngle.getSelectedIndex());
                        int secondAngle = getAngle(nsSecondAngle.getSelectedIndex());
                        int colorSpace = getColorSpace(nsColorSpace.getSelectedIndex());
                        int colorDiff = getColorDiff(nsColorDiff.getSelectedIndex());
                        BluetoothManager.getInstance().displayParam = new DisplayParam(measureMode, lightSource, angle, secondLightSource,
                                secondAngle, colorSpace, colorDiff);
                        BluetoothManager.getInstance().setOrder(Constant.SET_DEVICE_DISPLAY_PARAM);
                    }
                });
            }
        });
    }

    public void setTolerance() {
        CustomDialog.show(MainActivity.this, R.layout.dialog_set_tolerance, new CustomDialog.OnBindView() {
            @Override
            public void onBind(CustomDialog dialog, View v) {
                Button btnSend = v.findViewById(R.id.btn_send);
                final EditText etDL = v.findViewById(R.id.et_dL);
                final EditText etDa = v.findViewById(R.id.et_da);
                final EditText etDb = v.findViewById(R.id.et_db);
                final EditText etDc = v.findViewById(R.id.et_dc);
                final EditText etDH = v.findViewById(R.id.et_dH);
                final EditText etDeab = v.findViewById(R.id.et_dEab);
                final EditText etDech = v.findViewById(R.id.et_dEch);
                final EditText etDecmc = v.findViewById(R.id.et_dEcmc);
                final EditText etDe94 = v.findViewById(R.id.et_dE94);
                final EditText etDe00 = v.findViewById(R.id.et_dE00);
                etDL.setText("0.2");
                etDa.setText("0.2");
                etDb.setText("0.2");
                etDc.setText("0.2");
                etDH.setText("0.2");
                etDeab.setText("0.2");
                etDech.setText("0.2");
                etDecmc.setText("0.2");
                etDe94.setText("0.2");
                etDe00.setText("0.2");
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        float dL = Float.parseFloat(etDL.getText().toString().trim());
                        float da = Float.parseFloat(etDa.getText().toString().trim());
                        float db = Float.parseFloat(etDb.getText().toString().trim());
                        float dc = Float.parseFloat(etDc.getText().toString().trim());
                        float dH = Float.parseFloat(etDH.getText().toString().trim());
                        float dEab = Float.parseFloat(etDeab.getText().toString().trim());
                        float dEch = Float.parseFloat(etDech.getText().toString().trim());
                        float dEcmc = Float.parseFloat(etDecmc.getText().toString().trim());
                        float dE94 = Float.parseFloat(etDe94.getText().toString());
                        float dE00 = Float.parseFloat(etDe00.getText().toString().trim());
                        ToleranceBean bean = new ToleranceBean();
                        bean.setL(dL);
                        bean.setA(da);
                        bean.setB(db);
                        bean.setC(dc);
                        bean.setH(dH);
                        bean.setDe00(dEab);
                        bean.setdE94(dEch);
                        bean.setdEab(dEab);
                        bean.setdEch(dEch);
                        bean.setdEcmc(dEcmc);
                        bean.setdE94(dE94);
                        bean.setDe00(dE00);
                        BluetoothManager.getInstance().toleranceBean = bean;
                        BluetoothManager.getInstance().setOrder(Constant.SET_TOLERANCE);
                    }
                });
            }
        });
    }

    private int getMeasureMode(int position) {
        int measureMode;
        if (position == 0) {
            measureMode = Constant.TYPE_MEASURE_MODE_SCI;
        } else if (position == 1) {
            measureMode = Constant.TYPE_MEASURE_MODE_SCE;
        } else if (position == 2) {
            measureMode = Constant.TYPE_MEASURE_MODE_SCI_SCE;
        } else {
            measureMode = Constant.TYPE_MEASURE_MODE_SCI;
        }
        return measureMode;
    }

    private int getLightSource(int position) {
        int lightSource = 0;
        if (position == 0) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_A;
        } else if (position == 1) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_C;
        } else if (position == 2) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_D50;
        } else if (position == 3) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_D55;
        } else if (position == 4) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_D65;
        } else if (position == 5) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_D75;
        } else if (position == 6) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_F1;
        } else if (position == 7) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_F2;
        } else if (position == 8) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_F3;
        } else if (position == 9) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_F4;
        } else if (position == 10) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_F5;
        } else if (position == 11) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_F6;
        } else if (position == 12) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_F7;
        } else if (position == 13) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_F8;
        } else if (position == 14) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_F9;
        } else if (position == 15) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_F10;
        } else if (position == 16) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_F11;
        } else if (position == 17) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_F12;
        } else if (position == 18) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_CWF;
        } else if (position == 19) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_U30;
        } else if (position == 20) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_DLF;
        } else if (position == 21) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_NBF;
        } else if (position == 22) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_TL83;
        } else if (position == 23) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_TL84;
        } else if (position == 24) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_U35;
        } else if (position == 25) {
            lightSource = Constant.TYPE_LIGHT_SOURCE_B;
        } else {
            lightSource = Constant.TYPE_LIGHT_SOURCE_D65;
        }
        return lightSource;
    }

    private int getAngle(int position) {
        int angle = 0;
        if (position == 0) {
            angle = Constant.TYPE_ANGLE_2;
        } else if (position == 1) {
            angle = Constant.TYPE_ANGLE_10;
        } else {
            angle = Constant.TYPE_ANGLE_2;
        }
        return angle;
    }

    private int getColorSpace(int position) {
        int colorSpace;
        if (position == 0) {
            colorSpace = Constant.TYPE_COLOR_SPACE_LAB_CIE;
        } else if (position == 1) {
            colorSpace = Constant.TYPE_COLOR_SPACE_LCH_CIE;
        } else if (position == 2) {
            colorSpace = Constant.TYPE_COLOR_SPACE_LAB_HUNTER;
        } else if (position == 3) {
            colorSpace = Constant.TYPE_COLOR_SPACE_LUV;
        } else if (position == 4) {
            colorSpace = Constant.TYPE_COLOR_SPACE_XYZ;
        } else if (position == 5) {
            colorSpace = Constant.TYPE_COLOR_SPACE_YXY;
        } else if (position == 6) {
            colorSpace = Constant.TYPE_COLOR_SPACE_RGB;
        } else if (position == 7) {
            colorSpace = Constant.TYPE_COLOR_SPACE_TSYP;
        } else if (position == 8) {
            colorSpace = Constant.TYPE_COLOR_SPACE_REFLECTIVITY;
        } else if (position == 9) {
            colorSpace = Constant.TYPE_COLOR_SPACE_COVERAGE_RATE;
        } else if (position == 10) {
            colorSpace = Constant.TYPE_COLOR_SPACE_DENSITY_A;
        } else if (position == 11) {
            colorSpace = Constant.TYPE_COLOR_SPACE_DENSITY_T;
        } else if (position == 12) {
            colorSpace = Constant.TYPE_COLOR_SPACE_DENSITY_E;
        } else if (position == 13) {
            colorSpace = Constant.TYPE_COLOR_SPACE_DENSITY_M;
        } else if (position == 14) {
            colorSpace = Constant.TYPE_COLOR_SPACE_MUNSELL;
        } else if (position == 15) {
            colorSpace = Constant.TYPE_COLOR_SPACE_TINT;
        } else if (position == 16) {
            colorSpace = Constant.TYPE_COLOR_SPACE_WHITENESS;
        } else if (position == 17) {
            colorSpace = Constant.TYPE_COLOR_SPACE_YELLOWNESS;
        } else if (position == 18) {
            colorSpace = Constant.TYPE_COLOR_SPACE_BLACKNESS;
        } else if (position == 19) {
            colorSpace = Constant.TYPE_COLOR_SPACE_COLORING_POWER;
        } else if (position == 20) {
            colorSpace = Constant.TYPE_COLOR_SPACE_COLOR_FASTNESS;
        } else {
            colorSpace = Constant.TYPE_COLOR_SPACE_LAB_CIE;
        }
        return colorSpace;
    }

    private int getColorDiff(int position) {
        int colorDiff;
        if (position == 0) {
            colorDiff = Constant.TYPE_COLOR_DIFFERENCE_DE_AB;
        } else if (position == 1) {
            colorDiff = Constant.TYPE_COLOR_DIFFERENCE_DE_CH;
        } else if (position == 2) {
            colorDiff = Constant.TYPE_COLOR_DIFFERENCE_DE_00;
        } else if (position == 3) {
            colorDiff = Constant.TYPE_COLOR_DIFFERENCE_DE_CMC;
        } else if (position == 4) {
            colorDiff = Constant.TYPE_COLOR_DIFFERENCE_DE_94;
        } else if (position == 5) {
            colorDiff = Constant.TYPE_COLOR_DIFFERENCE_DE_AB_HUNTER;
        } else if (position == 6) {
            colorDiff = Constant.TYPE_COLOR_DIFFERENCE_DE_UV;
        } else {
            colorDiff = Constant.TYPE_COLOR_DIFFERENCE_DE_AB;
        }
        return colorDiff;
    }

    private int getAutoOffTime(int position) {
        int autoOffTime;
        if (position == 0) {
            autoOffTime = Constant.TYPE_AUTO_OFF_TIME_10_MIN;
        } else if (position == 1) {
            autoOffTime = Constant.TYPE_AUTO_OFF_TIME_20_MIN;
        } else {
            autoOffTime = Constant.TYPE_AUTO_OFF_TIME_10_MIN;
        }
        return autoOffTime;
    }

    private int getBackLightTime(int position) {
        int backLightTime;
        if (position == 0) {
            backLightTime = Constant.TYPE_BACKLIGHT_TIME_1_SECOND;
        } else if (position == 1) {
            backLightTime = Constant.TYPE_BACKLIGHT_TIME_2_SECOND;
        } else {
            backLightTime = Constant.TYPE_BACKLIGHT_TIME_1_SECOND;
        }
        return backLightTime;
    }

    private class PlaybackStatus extends BroadcastReceiver {


        public PlaybackStatus() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                Log.i("deep", action);
            }
            switch (action) {
                case Constant.ON_FAIL:
                    String failType = intent.getStringExtra(Constant.ON_FAIL);
                    onFail(failType);
                    break;
            }
        }

        public void onFail(String failType) {
            Log.i("deep-error", "onReceive: onfail in mainActivity!");
            result="";
            binding.tvResult.setText(result);
            color=Color.rgb(255,255,255);
            binding.tvColor.setBackgroundColor(color);
            deviceState = "未连接设备，请重新连接";
            binding.tvDeviceState.setText(deviceState);
            intentVis = View.VISIBLE;
            binding.btnIntent.setVisibility(intentVis);
            adjustVis = View.INVISIBLE;
            binding.btnAdjust.setVisibility(adjustVis);
            measureVis = View.INVISIBLE;
            binding.btnMeasure.setVisibility(measureVis);
        }

    }

}