package com.example.bluetoothsdkdemo.adapter;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.bluetoothsdkdemo.R;
import com.example.bluetoothsdkdemo.bean.BluetoothBean;

import java.util.List;

public class BluetoothInfoAdapter extends BaseQuickAdapter<BluetoothBean, BaseViewHolder> {
    public BluetoothInfoAdapter(int layoutResId, @Nullable List<BluetoothBean> data) {
        super(layoutResId, data);
    }
    public BluetoothInfoAdapter(int layoutResId) {
        super(layoutResId);
    }
    @Override
    protected void convert(@NonNull BaseViewHolder helper, BluetoothBean item) {
        helper.setText(R.id.tv_bluetooth_name,item.getName())
                .setText(R.id.tv_bluetooth_mac,item.getMac())
                .setText(R.id.tv_bluetooth_pre_parse,item.getPreParse())
                .setText(R.id.tv_bluetooth_post_parse,item.getPostParse())
                .setText(R.id.tv_bluetooth_rssi,item.getRssi());
    }
}
