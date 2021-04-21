package com.example.bluetoothsdkdemo.bean.parse.struct;

public class SampleStruct {
    private byte state;
    private byte lightSourceOrAngle;
    private byte measureMode;
    private byte dataMode;
    private byte startWavelengths;// 开始波长 startWavelengths
    private byte intervalWavelengths;// 波长间隔 intervalWavelengths
    private byte WavelengthsCount;// 波长个数 WavelengthsCount
    private String name;
    private float[] labArrarys;
    private float[] sci;
    private float[] sce;

    private int time;

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public byte getLightSourceOrAngle() {
        return lightSourceOrAngle;
    }

    public void setLightSourceOrAngle(byte lightSourceOrAngle) {
        this.lightSourceOrAngle = lightSourceOrAngle;
    }

    public byte getMeasureMode() {
        return measureMode;
    }

    public void setMeasureMode(byte measureMode) {
        this.measureMode = measureMode;
    }

    public byte getDataMode() {
        return dataMode;
    }

    public void setDataMode(byte dataMode) {
        this.dataMode = dataMode;
    }

    public byte getStartWavelengths() {
        return startWavelengths;
    }

    public void setStartWavelengths(byte startWavelengths) {
        this.startWavelengths = startWavelengths;
    }

    public byte getWavelengthsCount() {
        return WavelengthsCount;
    }

    public void setWavelengthsCount(byte wavelengthsCount) {
        WavelengthsCount = wavelengthsCount;
    }

    public byte getIntervalWavelengths() {
        return intervalWavelengths;
    }

    public void setIntervalWavelengths(byte intervalWavelengths) {
        this.intervalWavelengths = intervalWavelengths;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float[] getLabArrarys() {
        return labArrarys;
    }

    public void setLabArrarys(float[] labArrarys) {
        this.labArrarys = labArrarys;
    }

    public float[] getSci() {
        return sci;
    }

    public void setSci(float[] sci) {
        this.sci = sci;
    }

    public float[] getSce() {
        return sce;
    }

    public void setSce(float[] sce) {
        this.sce = sce;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
