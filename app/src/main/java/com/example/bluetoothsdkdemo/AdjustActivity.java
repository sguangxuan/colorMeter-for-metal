package com.example.bluetoothsdkdemo;

import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.example.bluetoothsdkdemo.adapter.BluetoothInfoAdapter;
import com.example.bluetoothsdkdemo.bean.BluetoothBean;
import com.example.bluetoothsdkdemo.bean.parse.AdjustBean;
import com.example.bluetoothsdkdemo.bean.parse.DeviceInfoBean;
import com.example.bluetoothsdkdemo.bean.parse.MeasureBean;
import com.example.bluetoothsdkdemo.bean.parse.ReadLabMeasureDataBean;
import com.example.bluetoothsdkdemo.bean.parse.ReadMeasureDataBean;
import com.example.bluetoothsdkdemo.bean.parse.ReadRgbMeasureDataBean;
import com.example.bluetoothsdkdemo.bean.parse.StandardSampleDataBean;
import com.example.bluetoothsdkdemo.bean.parse.struct.DeviceInfoStruct;
import com.example.bluetoothsdkdemo.ble.BluetoothManager;
import com.example.bluetoothsdkdemo.databinding.ActivityAdjustBinding;
import com.example.bluetoothsdkdemo.databinding.ActivityTestBinding;
import com.example.bluetoothsdkdemo.util.Constant;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.kongzue.dialog.v3.WaitDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdjustActivity extends BluetoothActivity {
    private ActivityAdjustBinding binding;
    private SearchRequest request;
    public String msg;
    public int backVis;
    public String whiteAdjustResult;
    public String blackAdjustResult;
    public int whiteAdjustColor;
    public int blackAdjustColor;
    private PlaybackStatus mPlaybackStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_adjust);
        binding.setActivity(this);

        backVis = View.INVISIBLE;
        binding.btnBack.setVisibility(backVis);
        whiteAdjustResult = "未校准";
        whiteAdjustColor = Color.rgb(254, 61, 61);
        binding.tvWhiteResult.setText(whiteAdjustResult);
        binding.tvWhiteResult.setTextColor(whiteAdjustColor);
        blackAdjustResult = "未校准";
        blackAdjustColor = Color.rgb(254, 61, 61);
        binding.tvBlackResult.setText(blackAdjustResult);
        binding.tvBlackResult.setTextColor(blackAdjustColor);

        mPlaybackStatus = new PlaybackStatus();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.BLACK_ADJUST);
        filter.addAction(Constant.WHITE_ADJUST);
        filter.addAction(Constant.ON_FAIL);
        registerReceiver(mPlaybackStatus, filter);
    }

    public void blackAdjust() {
        BluetoothManager.getInstance().setOrder(Constant.BLACK_ADJUST);
    }

    public void whiteAdjust() {
        BluetoothManager.getInstance().setOrder(Constant.WHITE_ADJUST);
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
            Serializable data = intent.getSerializableExtra("data");
            switch (action) {
                case Constant.BLACK_ADJUST:
                    onBlackAdjust((AdjustBean) data);
                    break;
                case Constant.WHITE_ADJUST:
                    onWhiteAdjust((AdjustBean) data);
                    break;
                case Constant.ON_FAIL:
                    String failType = intent.getStringExtra(Constant.ON_FAIL);
                    onFail(failType);
                    break;
            }
        }
    }

    public void onBlackAdjust(AdjustBean bean) {
        blackAdjustResult = "已完成";
        blackAdjustColor = Color.rgb(0, 0, 0);
        binding.tvBlackResult.setText(blackAdjustResult);
        binding.tvBlackResult.setTextColor(blackAdjustColor);

        if (blackAdjustResult == "已完成" && whiteAdjustResult == "已完成"){
            backVis = View.VISIBLE;
            binding.btnBack.setVisibility(backVis);
        }
    }

    public void onWhiteAdjust(AdjustBean bean) {
        backVis = View.INVISIBLE;
        binding.btnBack.setVisibility(backVis);
        whiteAdjustResult = "已完成";
        whiteAdjustColor = Color.rgb(0, 0, 0);
        binding.tvWhiteResult.setText(whiteAdjustResult);
        binding.tvWhiteResult.setTextColor(whiteAdjustColor);

        if (blackAdjustResult == "已完成" && whiteAdjustResult == "已完成"){
            backVis = View.VISIBLE;
            binding.btnBack.setVisibility(backVis);
        }
    }

    public void onFail(String failType) {

    }

    public void back(){
        finish();
    }

}