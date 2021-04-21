package com.example.bluetoothsdkdemo.bean.parse;

import androidx.annotation.NonNull;


import com.example.bluetoothsdkdemo.util.TimeUtil;

import java.io.Serializable;

public class AdjustBean  implements Serializable {
    private byte adjustState;// 校准状态： 0-成功 1-失败 2-硬件错误 Calibration status: 0 - success 1 - failure 2 - hardware error
    private int time;// Unix时间戳 单位：秒 UNIX timestamp unit: seconds
    private byte adjustBeforeState;

    /**
     * 返回校准前状态 Return to pre calibration status
     * @return 返回校准前状态 0-成功 1-失败 2-硬件错误 Return to pre calibration status 0 - success 1 - failure 2 - hardware error
     */
    public byte getAdjustBeforeState() {
        return adjustBeforeState;
    }

    /**
     * 设置校准状态 Set calibration status
     * @param adjustState 校准状态 0-成功 1-失败 2-硬件错误 Calibration status 0 - success 1 - failure 2 - hardware error
     */
    public void setAdjustState(byte adjustState) {
        this.adjustState = adjustState;
    }

    /**
     * 返回Unix时间戳 单位：秒 Returns the UNIX timestamp in seconds
     * @return 返回Unix时间戳 单位：秒 Returns the UNIX timestamp in seconds
     */
    public int getTime() {
        return time;
    }

    /**
     * 设置Unix时间戳 Set UNIX timestamp
     * @param time Unix时间戳 UNIX timestamp
     */
    public void setTime(int time) {
        this.time = time;
    }

    /**
     * 返回校准状态 Return to calibration status
     * @return 返回校准状态 Return to calibration status
     */
    public byte getAdjustState() {
        return adjustState;
    }

    /**
     *  设置校准前状态 Set state before calibration
     * @param adjustBeforeState 0-成功 1-失败 2-硬件错误 0 - success 1 - failure 2 - hardware error
     */
    public void setAdjustBeforeState(byte adjustBeforeState) {
        this.adjustBeforeState = adjustBeforeState;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n")
                .append("校准状态：") // Calibration status
                .append(judgeState(adjustState))
                .append("\n")
                .append("时间：") // time
                .append(TimeUtil.unixTimestamp2Date(time))
                .append("\n")
                .append("校准前状态：") // adjustBeforeState
                .append(judgeState(adjustBeforeState));


        return builder.toString();
    }

    private String judgeState(byte b) {
        String str;
        if (b == 0x00) {
            str = "成功"; // success
        } else if (b == 0x01) {
            str = "失败"; // fail
        } else if (b == 0x02) {
            str = "硬件错误"; // Hardware error
        } else {
            str = "解析错误"; // Parsing error
        }
        return str;
    }
}
