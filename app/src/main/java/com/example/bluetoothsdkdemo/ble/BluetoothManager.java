package com.example.bluetoothsdkdemo.ble;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
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
import com.example.bluetoothsdkdemo.encode.DataParse;
import com.example.bluetoothsdkdemo.encode.MachineCmd;
import com.example.bluetoothsdkdemo.util.ByteUtil;
import com.example.bluetoothsdkdemo.util.Constant;
import com.example.bluetoothsdkdemo.util.DateUtil;
import com.example.bluetoothsdkdemo.util.TimeUtil;

import org.apache.commons.lang3.ArrayUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BluetoothManager {
    private static final String TAG = "BluetoothManager";
    private static BluetoothManager bleManager;
    private Context context;
    //已连接设备实例(Connected device instance)
    public BleDevice connectDevice;
    public String serviceUUid = "0000FFE0-0000-1000-8000-00805F9B34FB";
    public String WriteChaUUid = "0000FFE1-0000-1000-8000-00805F9B34FB";
    private byte[] realByte;
    private String order;
    //连接设备后第一次初始化(Initialization for the first time after connecting the device)
    public boolean connect_init = false;
    public boolean getByteSuccess = false;
    public StandardSampleDataBean.StandardDataBean standardDataBean;
    private Handler myHandle;
    private long lastTime;
    public short count = 0;     //deviceData总量(Total device data)
    public short index = 0;     //表示data数据索引(Data index)
    public int measureMode = 0;
    public DisplayParam displayParam;
    public ToleranceBean toleranceBean;
    private Disposable disposable;
    private int time = 0;
    public boolean fromApp = false;
    private BleWriteCallback bleWriteCallback = new BleWriteCallback() {
        @Override
        public void onWriteSuccess(int current, int total, byte[] justWrite) {
            Log.d(TAG, "WriteSuccess：" + HexUtil.formatHexString(justWrite));
        }

        @Override
        public void onWriteFailure(BleException exception) {

        }
    };

    public void init(Context context) {
        this.context = context;
    }

    public BluetoothManager() {
        if (myHandle == null) {
            myHandle = new Handler();
        }
    }

    public static BluetoothManager getInstance() {
        if (bleManager == null) {
            bleManager = new BluetoothManager();
        }
        return bleManager;
    }

    public boolean isConnect() {
        return BleManager.getInstance().isConnected(connectDevice);
    }

    public void syncTime() {
        int systemTime = (int) (System.currentTimeMillis() / 1000);
        byte[] bytes = MachineCmd.setDeviceTimeCmd(systemTime);
        Log.d("syncTime", "st = " + systemTime + " " + ByteUtil.getInt(bytes, 2, ByteUtil.TYPE_LOW_FIRST) + "");
        BleManager.getInstance().write(connectDevice, serviceUUid,
                WriteChaUUid, MachineCmd.setDeviceTimeCmd(systemTime), bleWriteCallback);
    }

    //读取校准状态 (Read calibration status)
    private void readAdjustState() {
        byte[] bytes = new byte[10];
        bytes[0] = (byte) 0xbb;
        bytes[1] = (byte) 0x1e;
        bytes[2] = (byte) 0x00;
        bytes[3] = (byte) 0x00;
        bytes[4] = (byte) 0x00;
        bytes[5] = (byte) 0x00;
        bytes[6] = (byte) 0x00;
        bytes[7] = (byte) 0x00;
        bytes[8] = (byte) 0xff;
        bytes[9] = (byte) 0x00;
        BleManager.getInstance().write(connectDevice, serviceUUid,
                WriteChaUUid, bytes, bleWriteCallback);
    }

    public void measure() {
        BleManager.getInstance().write(connectDevice, serviceUUid,
                WriteChaUUid, MachineCmd.measureCmd(measureMode), bleWriteCallback);

    }

    public void deleteAll() {
        BleManager.getInstance().write(connectDevice, serviceUUid,
                WriteChaUUid, MachineCmd.deleteAllStandardDataCmd(), bleWriteCallback);
    }

    public void deleteDataIndex() {
        BleManager.getInstance().write(connectDevice, serviceUUid,
                WriteChaUUid, MachineCmd.deleteStandardDataForNumberCmd(index), bleWriteCallback);
    }

    public void getMeasureResult() {
        BleManager.getInstance().write(connectDevice, serviceUUid,
                WriteChaUUid, MachineCmd.readMeasureDataCmd(measureMode), bleWriteCallback);
    }

    public void getLabMeasureResult() {
        BleManager.getInstance().write(connectDevice, serviceUUid,
                WriteChaUUid, MachineCmd.readLabMeasureDataCmd(measureMode), bleWriteCallback);
    }

    public void getRgbMeasureResult() {
        BleManager.getInstance().write(connectDevice, serviceUUid,
                WriteChaUUid, MachineCmd.readRGBMeasureDataCmd(measureMode), bleWriteCallback);
    }

    public void getDeviceInfo() {
        BleManager.getInstance().write(connectDevice, serviceUUid,
                WriteChaUUid, HexUtil.hexStringToBytes("bb12010000000000ffbc"), bleWriteCallback);
    }

    public void getPowerInfo() {
        BleManager.getInstance().write(connectDevice, serviceUUid,
                WriteChaUUid, MachineCmd.getDevicePowerInfoCmd(), bleWriteCallback);
    }

    public void getStandardDataCount() {
        BleManager.getInstance().write(connectDevice, serviceUUid,
                WriteChaUUid, HexUtil.hexStringToBytes("bb16000000000000ff00"), bleWriteCallback);
    }

    public void getStandardDataIndex() {
        byte[] b = new byte[2];
        ByteUtil.putShort(b, index, 0);
        BleManager.getInstance().write(connectDevice, serviceUUid,
                WriteChaUUid, HexUtil.hexStringToBytes("bb1601" + HexUtil.formatHexString(b) + "000000ff00"), bleWriteCallback);
    }

    public void setDisplayParam() {
        if (displayParam != null) {
            BleManager.getInstance().write(connectDevice, serviceUUid,
                    WriteChaUUid, MachineCmd.setDeviceDisplayParamCmd(displayParam), bleWriteCallback);
        }

    }

    public void setTolerance() {
        if (toleranceBean != null) {
            BleManager.getInstance().write(connectDevice, serviceUUid,
                    WriteChaUUid, MachineCmd.setToleranceCmd(toleranceBean), bleWriteCallback);
        }
    }

    public void wakeUp() {
        BleManager.getInstance().write(connectDevice, serviceUUid,
                WriteChaUUid, HexUtil.hexStringToBytes("bbf0000000000000ff00"), bleWriteCallback);
    }

    public void downloadDataToDevice() {
        if (standardDataBean != null) {
            BleManager.getInstance().write(connectDevice, serviceUUid,
                    WriteChaUUid, MachineCmd.postStandardDataCmd(standardDataBean), true, bleWriteCallback);
        }
    }

    public void blackAdjust() {
        BleManager.getInstance().write(connectDevice, serviceUUid,
                WriteChaUUid, MachineCmd.blackAdjustCmd(), bleWriteCallback);
    }

    public void whiteAdjust() {
        BleManager.getInstance().write(connectDevice, serviceUUid,
                WriteChaUUid, MachineCmd.whiteAdjustCmd(), bleWriteCallback);
    }

    public void setOrder(String order, final boolean needWakeUp) {
        this.order = order;
        myHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bleManager.isConnect()) {
                    startOrder(needWakeUp);
                } else {
                    Log.i(TAG, "蓝牙断开连接");// Bluetooth disconnect
                }
            }
        }, 100);

    }

    public void setOrder(String order) {
        this.order = order;

        myHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bleManager.isConnect()) {
                    startOrder(true);
                } else {
                    Log.i(TAG, "蓝牙断开连接");// Bluetooth disconnect
                }
            }
        }, 100);
    }

    /**
     * 定时发送蓝牙指令(Send Bluetooth command regularly)
     */
    private void sendOrderByTime() {
        if (order.equals(Constant.MEASURE)
                || order.equals(Constant.BLACK_ADJUST)
                || order.equals(Constant.WHITE_ADJUST)
                || order.equals(Constant.POST_STANDARD_DATA)) {
            runWithRetry(4, 3);
        } else {
            runWithRetry(4, 1);
        }
    }

    private void runWithRetry(int count, int period) {
        Observable.intervalRange(0, count, 0, period, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (lastTime != 0) {
                            Log.d("runnableTime", "time = " + time + "--" + System.currentTimeMillis() + "diffTime = " + (System.currentTimeMillis() - lastTime));
                        }
                        Log.d("cjq", "order:" + order);
                        Log.d("cjq", "onNext#" + DateUtil.getCurrentDate() + "-" + Thread.currentThread().getId());
                        if (time < 4) {
                            postOrder();
                            time++;
                            lastTime = System.currentTimeMillis();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("onError：", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d("cjq", "onComplete#" + DateUtil.getCurrentDate() + "-" + Thread.currentThread().getId());
                        disposable.dispose();
                        getByteSuccess = false;
                        time = 0;
                        lastTime = 0;
                        Log.i(TAG, "BLUETOOTH_FAILED");
                        Intent intent = new Intent(Constant.ON_FAIL);
                        intent.putExtra(Constant.ON_FAIL, order);
                        context.sendBroadcast(intent);
                        if (fromApp) {
                            fromApp = false;
                        }
                    }
                });
    }

    private void stopSendOrder() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    private void startOrder(boolean needWakeUp) {
        /*if(needWakeUp) {
            //唤醒
            wakeUp();
        }*/
        getByteSuccess = false;
        sendOrderByTime();
    }

    private void postOrder() {
        getByteSuccess = false;
        realByte = new byte[]{};
        wakeUp();
        // 有限制次数的轮询器
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        switch (order) {
            case Constant.MEASURE: // 测量(measure)
                measure();
                break;
            case Constant.READ_MEASURE_DATA: // 读取测量结果(Read the measurement results)
                getMeasureResult();
                break;
            case Constant.READ_LAB_MEASURE_DATA: // 读取Lab测量结果(GetLabMeasureResult)
                getLabMeasureResult();
                break;
            case Constant.READ_RGB_MEASURE_DATA: // 读取RGB测量结果(GetRgbMeasureResult)
                getRgbMeasureResult();
                break;
            case Constant.GET_DEVICE_INFO: // 获取仪器信息(GetDeviceInfo)
                getDeviceInfo();
                break;
            case Constant.GET_STANDARD_DATA_COUNT: // 获取标样数据数量(GetStandardDataCount)
                getStandardDataCount();
                break;
            case Constant.GET_STANDARD_DATA_FOR_NUM: // 获取某一条标样数据(GetStandardDataIndex)
                getStandardDataIndex();
                break;
            case Constant.POST_STANDARD_DATA: // 下载数据到仪器(DownloadDataToDevice)
                downloadDataToDevice();
                break;
            case Constant.SET_DEVICE_TIME: // 同步时间(SyncTime)
                syncTime();
                break;
            case Constant.BLACK_ADJUST: // 黑校准(BlackAdjust)
                blackAdjust();
                break;
            case Constant.WHITE_ADJUST: // 白校准(WhiteAdjust)
                whiteAdjust();
                break;
            case Constant.GET_DEVICE_ADJUST_STATE: // 读取校准状态(ReadAdjustState)
                readAdjustState();
                break;
            case Constant.DELETE_ALL_STANDARD_DATA: // 删除所有数据(DeleteAllData)
                deleteAll();
                break;
            case Constant.DELETE_STANDARD_DATA_FOR_NUM: // 删除某一条标样(Delete a standard sample)
                deleteDataIndex();
                break;
            case Constant.GET_DEVICE_POWER_INFO: // 读取仪器电量(GetPowerInfo)
                getPowerInfo();
                break;
            case Constant.SET_DEVICE_DISPLAY_PARAM: // 设置仪器显示参数(SetDisplayParam)
                setDisplayParam();
                break;
            case Constant.SET_TOLERANCE: // 设置仪器容差(SetTolerance)
                setTolerance();
                break;
        }
    }

    /*连接成功后，设置通知(When the connection is successful, set the notification)*/
    public void setNotify() {
        BleManager.getInstance().stopNotify(BleManager.getInstance().getAllConnectedDevice().get(0), serviceUUid, WriteChaUUid);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BleManager.getInstance().notify(BleManager.getInstance().getAllConnectedDevice().get(0), serviceUUid,
                        WriteChaUUid, new BleNotifyCallback() {
                            @SuppressLint("DefaultLocale")
                            @Override
                            public void onCharacteristicChanged(byte[] data) {
                                Log.d("cjq", "onCharacteristicChanged-" + Thread.currentThread().getId() + "，" + HexUtil.formatHexString(data));
                                Log.d(TAG, HexUtil.formatHexString(data));
                                if (HexUtil.formatHexString(data).startsWith("bb1a")) {
                                    initStatus();
                                    if (data[2] == 0x00) {
                                        Log.i(TAG, "setDisplayParams===>success");
                                    } else if (data[2] == 0x01) {
                                        Log.i(TAG, "setDisplayParams===>fail");
                                    } else {
                                        Log.i(TAG, "setDisplayParams===>Parsing error");
                                    }
                                    Intent intent = new Intent(Constant.SET_DEVICE_DISPLAY_PARAM);
                                    intent.putExtra("state", data[2]);
                                    context.sendBroadcast(intent);
                                } else if (HexUtil.formatHexString(data).startsWith("bb1b")) {
                                    initStatus();
                                    if (data[2] == 0x00) {
                                        Log.i(TAG, "setTolerance===>success");
                                    } else if (data[2] == 0x01) {
                                        Log.i(TAG, "setTolerance===>fail");
                                    } else {
                                        Log.i(TAG, "setTolerance===>Parsing error");
                                    }
                                    Intent intent = new Intent(Constant.SET_TOLERANCE);
                                    intent.putExtra("state", data[2]);
                                    context.sendBroadcast(intent);
                                } else if (HexUtil.formatHexString(data).startsWith("bb14")) {
                                    initStatus();
                                    if (data[2] == 0x00) {
                                        Log.i(TAG, "syncTime===>success");
                                    } else if (data[2] == 0x01) {
                                        Log.i(TAG, "syncTime===>fail");
                                    } else {
                                        Log.i(TAG, "syncTime===>err");
                                    }
                                    Intent intent = new Intent(Constant.SET_TOLERANCE);
                                    intent.putExtra("state", data[2]);
                                    context.sendBroadcast(intent);
                                } else if (HexUtil.formatHexString(data).startsWith("bb10")) {
                                    initStatus();
                                    AdjustBean adjustBean = DataParse.parseAdjust(data);
                                    Log.i(TAG, "blackAdjust===>" + adjustBean.toString());
                                    Intent intent = new Intent(Constant.BLACK_ADJUST);
                                    intent.putExtra("data", adjustBean);
                                    context.sendBroadcast(intent);
                                } else if (HexUtil.formatHexString(data).startsWith("bb11")) {
                                    initStatus();
                                    AdjustBean adjustBean = DataParse.parseAdjust(data);
                                    Log.i(TAG, "whiteAdjust===>" + adjustBean.toString());
                                    Intent intent = new Intent(Constant.WHITE_ADJUST);
                                    intent.putExtra("data", adjustBean);
                                    context.sendBroadcast(intent);
                                } else if (HexUtil.formatHexString(data).startsWith("bb01")) {
                                    initStatus();
                                    MeasureBean bean = DataParse.parseMeasure(data);
                                    Log.i(TAG, "measure===>" + bean.toString());
                                    Intent intent = new Intent(Constant.MEASURE);
                                    intent.putExtra("data", bean);
                                    context.sendBroadcast(intent);
                                } else if (HexUtil.formatHexString(data).startsWith("bb03")) {
                                    initStatus();
                                    ReadLabMeasureDataBean bean = DataParse.parseReadLabMeasureData(data);
                                    Log.i(TAG, "readLabMeasureData===>" + bean.toString());
                                    Intent intent = new Intent(Constant.READ_LAB_MEASURE_DATA);
                                    intent.putExtra("data", bean);
                                    context.sendBroadcast(intent);
                                } else if (HexUtil.formatHexString(data).startsWith("bb04")) {
                                    initStatus();
                                    ReadRgbMeasureDataBean bean = DataParse.parseReadRgbMeasureData(data);
                                    Log.i(TAG, "readGgbMeasureData===>" + bean.toString());
                                    Intent intent = new Intent(Constant.READ_RGB_MEASURE_DATA);
                                    intent.putExtra("data", bean);
                                    context.sendBroadcast(intent);
                                } else if (HexUtil.formatHexString(data).startsWith("bb1d")) {
                                    initStatus();
                                    DeviceInfoBean.PowerInfo powerInfo = DataParse.parseDevicePowerInfo(data);
                                    Log.i(TAG, "readPowerInfo===>" + powerInfo.getElectricQuantity());
                                    Intent intent = new Intent(Constant.GET_DEVICE_POWER_INFO);
                                    intent.putExtra("data", powerInfo);
                                    context.sendBroadcast(intent);
                                } else if (HexUtil.formatHexString(data).startsWith("bb1e")) {
                                    initStatus();
                                    int blackTime = ByteUtil.getInt(data, 8, ByteUtil.TYPE_LOW_FIRST);
                                    int whiteTime = ByteUtil.getInt(data, 3, ByteUtil.TYPE_LOW_FIRST);
                                    DeviceInfoBean.DeviceAdjustState adjustState = DataParse.parseDeviceAdjustState(data);
                                    Log.i(TAG, "blackAdjust===>" + TimeUtil.unixTimestamp2Date(blackTime));
                                    Log.i(TAG, "whiteAdjust===>" + TimeUtil.unixTimestamp2Date(whiteTime));
                                    Intent intent = new Intent(Constant.GET_DEVICE_ADJUST_STATE);
                                    intent.putExtra("data", adjustState);
                                    context.sendBroadcast(intent);
                                } else if (HexUtil.formatHexString(data).startsWith("bb1600")) {
                                    initStatus();
                                    count = 0;
                                    index = 0;
                                    BleManager.getInstance().setOperateTimeout(2000);
                                    count = ByteUtil.getShort(data, 3);
                                    Log.i(TAG, "getStandardCount===>" + count);
                                    Intent intent = new Intent(Constant.GET_STANDARD_DATA_COUNT);
                                    intent.putExtra("standard_count", count);
                                    context.sendBroadcast(intent);
                                } else if (HexUtil.formatHexString(data).startsWith("bb1602")) {
                                    initStatus();
                                    if (data[5] == 0x00) {
                                        Log.i(TAG, "deleteAllData===>删除成功(delete success)");
                                    } else if (data[5] == 0x01) {
                                        Log.i(TAG, "deleteAllData===>删除失败(delete fail)");
                                    } else if (data[5] == 0x02) {
                                        Log.i(TAG, "deleteAllData===>删除失败，超过界限(Beyond the limit)");
                                    } else {
                                        Log.i(TAG, "deleteAllData===>Parsing error");
                                    }
                                    index = 0;
                                    short standardNum = ByteUtil.getShort(data,3);
                                    Intent intent = new Intent(Constant.DELETE_STANDARD_DATA_FOR_NUM);
                                    intent.putExtra("state", data[5]);
                                    intent.putExtra("standard_num", standardNum);
                                } else if (HexUtil.formatHexString(data).startsWith("bb1605")) {
                                    if (data.length == 20 && HexUtil.formatHexString(data).substring(16, 18).equals("ff")) {
                                        initStatus();
                                        BleManager.getInstance().setOperateTimeout(2000);
                                    }
                                } else if (HexUtil.formatHexString(data).startsWith("bb160a")) {
                                    initStatus();
                                    if (data[3] == 0x00) {
                                        Log.i(TAG, "postDeviceData===>下载成功(Download successful)");
                                    } else if (data[3] == 0x01) {
                                        Log.i(TAG, "postDeviceData===>下载失败(Download failed)");
                                    } else if (data[3] == 0x02) {
                                        Log.i(TAG, "postDeviceData===>下载失败：仪器存储已满(Download failed: instrument storage is full)");
                                    }
                                    Intent intent = new Intent(Constant.POST_STANDARD_DATA);
                                    intent.putExtra("state", data[3]);
                                    context.sendBroadcast(intent);
                                } else if (HexUtil.formatHexString(data).startsWith("bb1604")) {
                                    //删除所有数据回调 Delete all data callbacks
                                    initStatus();
                                    if (data[3] == 0x00) {
                                        Log.i(TAG, "deleteAllData===>success");
                                    } else if (data[3] == 0x01) {
                                        Log.i(TAG, "deleteAllData===>fail");
                                    } else {
                                        Log.i(TAG, "deleteAllData===>Parsing error");
                                    }
                                    Intent intent = new Intent(Constant.DELETE_ALL_STANDARD_DATA);
                                    intent.putExtra("state", data[3]);
                                    context.sendBroadcast(intent);
                                } else {
                                    switch (order) {
                                        case Constant.READ_MEASURE_DATA:
                                            readNewMeasureResult(data);
                                            break;
                                        case Constant.GET_DEVICE_INFO:
                                            if (realByte.length < 200) {
                                                realByte = ArrayUtils.addAll(realByte, data);
                                            }
                                            Log.d(TAG, realByte.length + "");
                                            if (realByte.length == 200) {
                                                byte[] end = ArrayUtils.subarray(realByte,
                                                        198, 199);
                                                if (HexUtil.formatHexString(end).equals("ff")) {
                                                    initStatus();
                                                    DeviceInfoStruct deviceInfoStruct = DataParse.parseDeviceInfo(realByte);
                                                    Intent intent = new Intent(Constant.GET_DEVICE_INFO);
                                                    intent.putExtra("data",deviceInfoStruct);
                                                    context.sendBroadcast(intent);
                                                    Log.i(TAG, "getDeviceInfo===>" + deviceInfoStruct.toString());
                                                }
                                            }
                                            break;
                                        case Constant.GET_STANDARD_DATA_FOR_NUM:
                                            if (realByte.length < 250) {
                                                realByte = ArrayUtils.addAll(realByte, data);
                                            }
                                            if (realByte.length == 250) {
                                                byte[] end = ArrayUtils.subarray(realByte, 248, 249);
                                                HexUtil.formatHexString(realByte);
                                                if (HexUtil.formatHexString(end).equals("ff")) {
                                                    initStatus();
                                                    StandardSampleDataBean.StandardDataBean standardDataBean = DataParse.parseGetStandardForNum(realByte);
                                                    Log.i(TAG, "getStandardData===>index===>" + index + "===>" + standardDataBean.toString());
                                                    Intent intent = new Intent(Constant.GET_STANDARD_DATA_FOR_NUM);
                                                    intent.putExtra("data", standardDataBean);
                                                    context.sendBroadcast(intent);
                                                    if (index < count - 1) {
                                                        index++;
                                                        setOrder(Constant.GET_STANDARD_DATA_FOR_NUM);
                                                    }
                                                } else {
                                                    postOrder();
                                                }
                                            }
                                            break;
                                    }
                                }
                            }

                            @Override
                            public void onNotifySuccess() {
                                Log.d(TAG, "onNotifySuccess-" + Thread.currentThread().getId());
                            }

                            @Override
                            public void onNotifyFailure(BleException exception) {
                                Log.d(TAG, "onNotifyFailure-" + Thread.currentThread().getId());
                            }
                        });
            }
        }, 1000);
    }

    private void initStatus() {
        getByteSuccess = true;
        time = 0;
        stopSendOrder();
    }

    //200位获取测量结果方法 Method of obtaining measurement results with 200 bits
    private void readNewMeasureResult(byte[] data) {
        if (realByte.length < 200) {
            realByte = ArrayUtils.addAll(realByte, data);
        }
        //来自仪器，默认展示test数据 From the instrument, the default display test data
        if (realByte.length == 200) {
            byte[] end = ArrayUtils.subarray(realByte,
                    198, 199);
            if (HexUtil.formatHexString(realByte).startsWith("bb02") && HexUtil.formatHexString(end).equals("ff")) {
                initStatus();
                ReadMeasureDataBean dataBean = DataParse.parseReadMeasureData(realByte);
                Intent intent = new Intent(Constant.READ_MEASURE_DATA);
                intent.putExtra("data",dataBean);
                context.sendBroadcast(intent);
                Log.i(TAG, "readMeasureData===>" + dataBean.toString());
                //恢复初始值 reset
                fromApp = false;
            }
        }
    }
}
