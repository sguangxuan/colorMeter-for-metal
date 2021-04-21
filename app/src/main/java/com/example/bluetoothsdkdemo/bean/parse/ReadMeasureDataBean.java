package com.example.bluetoothsdkdemo.bean.parse;

import androidx.annotation.NonNull;


import com.example.bluetoothsdkdemo.util.ByteUtil;

import java.io.Serializable;

public class ReadMeasureDataBean implements Serializable {
    private byte measureMode;//  0-SCI 1-SCE 2-SCI+SCE
    private byte dataMode;//  0-反射率 1-lab
    private short startWavelengths;// 开始波长 startWavelengths
    private byte intervalWavelengths;// 波长间隔 intervalWavelengths
    private byte wavelengthsCount;// 波长个数 wavelengthsCount
    private float[] data;
    private float[] labData;

    /**
     *
     *
     * @return  0-SCI 1-SCE 2-SCI+SCE
     */
    public int getMeasureMode() {
        return measureMode;
    }

    /**
     *
     *
     * @param measureMode 0-SCI 1-SCE 2-SCI+SCE
     */
    public void setMeasureMode(byte measureMode) {
        this.measureMode = measureMode;
    }

    /**
     *
     *
     * @return  0-reflectivity 1-lab
     */
    public int getDataMode() {
        return dataMode;
    }

    /**
     *
     *
     * @param dataMode 0-reflectivity 1-lab
     */
    public void setDataMode(byte dataMode) {
        this.dataMode = dataMode;
    }

    /**
     *
     *
     * @return 360代表360nm 400代表400nm 360 stands for 360nm and 400 stands for 400nm
     */
    public short getStartWavelengths() {
        return startWavelengths;
    }

    /**
     *
     *
     * @param startWavelengths 开始波长 360代表360nm 400代表400nm 360 stands for 360nm and 400 stands for 400nm
     */
    public void setStartWavelengths(short startWavelengths) {
        this.startWavelengths = startWavelengths;
    }

    /**
     *
     *
     * @return 10代表10nm 10 stands for 10nm
     */
    public byte getIntervalWavelengths() {
        return intervalWavelengths;
    }

    /**
     *
     *
     * @param intervalWavelengths  10代表10nm 10 stands for 10nm
     */
    public void setIntervalWavelengths(byte intervalWavelengths) {
        this.intervalWavelengths = intervalWavelengths;
    }

    /**
     *
     *
     * @return  31 represents 31 wavelengths
     */
    public byte getWavelengthsCount() {
        return wavelengthsCount;
    }

    /**
     *
     *
     * @param wavelengthsCount  31代表31个波长 31 represents 31 wavelengths
     */
    public void setWavelengthsCount(byte wavelengthsCount) {
        this.wavelengthsCount = wavelengthsCount;
    }

    /**
     * 返回光谱反射率数组 Returns the spectral reflectance array
     *
     * @return
     */
    public float[] getData() {
        return data;
    }

    /**
     * @param data
     */
    public void setData(float[] data) {
        this.data = data;
    }

    /**
     * 返回 Lab数组 Return lab array
     *
     * @return
     */
    public float[] getLabData() {
        return labData;
    }

    /**
     * 设置 Lab数组 Set lab array
     *
     * @param labData
     */
    public void setLabData(float[] labData) {
        this.labData = labData;
    }


    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("测量模式：")// measureMode
                .append(judgeMeasureMode(measureMode))
                .append("\n")
                .append("数据模式：")//  dataMode
                .append(judgeDataMode(dataMode))
                .append("\n")
                .append("开始波长：")// startWavelengths
                .append(startWavelengths)
                .append("\n")
                .append("波长间隔：")// intervalWavelengths
                .append(intervalWavelengths)
                .append("\n")
                .append("波长个数：")// wavelengthsCount
                .append(wavelengthsCount)
                .append("\n")
                .append("反射率：")// reflectivity
                .append(ByteUtil.printArrays(data));
        if (labData != null) {
            sb.append("\n")
                    .append("L*:")
                    .append(labData[0])
                    .append("\n")
                    .append("a*:")
                    .append(labData[1])
                    .append("\n")
                    .append("b*:")
                    .append(labData[2]);
        }

        return sb.toString();
    }

    private String judgeMeasureMode(byte b) {
        String str;
        if (b == 0x00) {
            str = "SCI";
        } else if (b == 0x01) {
            str = "SCE";
        } else if (b == 0x10) {
            str = "SCI";
        } else if (b == 0x11) {
            str = "SCE";
        } else {
            str = "解析错误";// Parsing error
        }
        return str;
    }

    private String judgeDataMode(byte b) {
        String str;
        if (b == 0x00) {
            str = "反射率";// reflectivity
        } else if (b == 0x01) {
            str = "lab";
        } else {
            str = "解析错误";// Parsing error
        }
        return str;
    }
}
