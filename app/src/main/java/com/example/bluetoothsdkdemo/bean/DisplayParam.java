package com.example.bluetoothsdkdemo.bean;

public class DisplayParam {
    private int measureMode;
    private int firstLightSource;
    private int firstAngle;
    private int secondLightSource;
    private int secondAngle;
    private int colorSpace;
    private int colorDiff;

    public DisplayParam(int measureMode, int firstLightSource, int firstAngle, int secondLightSource, int secondAngle, int colorSpace, int colorDiff) {
        this.measureMode = measureMode;
        this.firstLightSource = firstLightSource;
        this.firstAngle = firstAngle;
        this.secondLightSource = secondLightSource;
        this.secondAngle = secondAngle;
        this.colorSpace = colorSpace;
        this.colorDiff = colorDiff;
    }

    public int getMeasureMode() {
        return measureMode;
    }

    public void setMeasureMode(int measureMode) {
        this.measureMode = measureMode;
    }

    public int getFirstLightSource() {
        return firstLightSource;
    }

    public void setFirstLightSource(int firstLightSource) {
        this.firstLightSource = firstLightSource;
    }

    public int getFirstAngle() {
        return firstAngle;
    }

    public void setFirstAngle(int firstAngle) {
        this.firstAngle = firstAngle;
    }

    public int getSecondLightSource() {
        return secondLightSource;
    }

    public void setSecondLightSource(int secondLightSource) {
        this.secondLightSource = secondLightSource;
    }

    public int getSecondAngle() {
        return secondAngle;
    }

    public void setSecondAngle(int secondAngle) {
        this.secondAngle = secondAngle;
    }

    public int getColorSpace() {
        return colorSpace;
    }

    public void setColorSpace(int colorSpace) {
        this.colorSpace = colorSpace;
    }

    public int getColorDiff() {
        return colorDiff;
    }

    public void setColorDiff(int colorDiff) {
        this.colorDiff = colorDiff;
    }
}
