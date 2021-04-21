package com.example.bluetoothsdkdemo.encode;


import android.os.Handler;

import com.example.bluetoothsdkdemo.bean.parse.AdjustBean;
import com.example.bluetoothsdkdemo.bean.parse.DeviceInfoBean;
import com.example.bluetoothsdkdemo.bean.parse.MeasureBean;
import com.example.bluetoothsdkdemo.bean.parse.ReadLabMeasureDataBean;
import com.example.bluetoothsdkdemo.bean.parse.ReadMeasureDataBean;
import com.example.bluetoothsdkdemo.bean.parse.ReadRgbMeasureDataBean;
import com.example.bluetoothsdkdemo.bean.parse.StandardSampleDataBean;
import com.example.bluetoothsdkdemo.bean.parse.struct.DeviceInfoStruct;
import com.example.bluetoothsdkdemo.util.ByteUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataParse {
    private boolean isComplete = true;
    private boolean isFirst = true;
    private byte[] firstBytes;
    private int index = 0;
    private byte[] dataBuffer = new byte[256];
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            isComplete = true;
            isFirst = true;
            index = 0;
        }
    };

//    /**
//     * 暴露方法
//     *
//     * @param bytes
//     * @param dataParseCallback
//     */
//    public void setDataType(byte[] bytes, DataParseCallback dataParseCallback) {
//        setState();
//        if (isComplete) {
//            parse(bytes, dataParseCallback);
//            Log.d("deep", "parse");
//        } else {
//            if (isFirst) {
////                Log.d("deep", "first");
//                index = index + firstBytes.length;
////                Log.d("deep", "firstIndex===>" + index);
//                System.arraycopy(firstBytes, 0, dataBuffer, 0, firstBytes.length);
////                Log.d("deep", "data1===>" + ByteUtil.printArrays(dataBuffer));
//                System.arraycopy(bytes, 0, dataBuffer, index, bytes.length);
////                Log.d("deep", "data2===>" + ByteUtil.printArrays(dataBuffer));
//                parse(dataBuffer, dataParseCallback);
//                index = index + bytes.length;
//                isFirst = false;
//            } else {
////                Log.d("deep", "parse1");
//                System.arraycopy(bytes, 0, dataBuffer, index, bytes.length);
////                Log.d("deep", "data2===>" + ByteUtil.printArrays(dataBuffer));
//                index = index + bytes.length;
////                Log.d("deep", "index===>" + index);
//                parse(dataBuffer, dataParseCallback);
//            }
//
//        }
//
//    }
//
//    public void setState() {
//        handler.removeCallbacks(runnable);
//        handler.postDelayed(runnable, 500);
//    }
//
//    public void removeState() {
//        handler.removeCallbacks(runnable);
//    }
//
//    private void parse(byte[] bytes, DataParseCallback dataParseCallback) {
//        if (bytes[0] == (byte) 0xbb) {
//            if (bytes[1] == 0x10) {
//                dataParseCallback.blackAdjust(bytes, parseAdjust(bytes));
//                removeState();
//            } else if (bytes[1] == 0x11) {
//                dataParseCallback.whiteAdjust(bytes, parseAdjust(bytes));
//                removeState();
//            } else if (bytes[1] == 0x01) {
//                dataParseCallback.measure(bytes, parseMeasure(bytes));
//                removeState();
//            } else if (bytes[1] == 0x02) {
//                if (bytes[2] == 0x01 || bytes[2] == 0x00) {
//                    if (isFirst) {
//                        firstBytes = bytes;
//                        isComplete = false;
//                    } else {
//                        Log.d("deep", "judgeSumCheck===>" + ByteUtil.judgeSumCheck(bytes, 249, bytes[249]));
//                        if (!ByteUtil.judgeSumCheck(bytes, 249, bytes[249])) {
//                            isComplete = false;
//                            Log.d("deep", "isComplete1===>" + isComplete);
//                            firstBytes = bytes;
//                        } else {
//                            isComplete = true;
//                            isFirst = true;
//                            index = 0;
//                            Log.d("deep", "isComplete===>succest");
//                            dataParseCallback.readMeasureData(bytes, parseReadMeasureData(bytes));
//                            removeState();
//                        }
//                    }
//                } else if (bytes[2] == 0x10 || bytes[2] == 0x11) {
//                    if (isFirst) {
//                        firstBytes = bytes;
//                        isComplete = false;
//                    } else {
//                        Log.d("deep", "judgeSumCheck===>" + ByteUtil.judgeSumCheck(bytes, 199, bytes[199]));
//                        if (!ByteUtil.judgeSumCheck(bytes, 199, bytes[199])) {
//                            isComplete = false;
//                            Log.d("deep", "isComplete1===>" + isComplete);
//                            firstBytes = bytes;
//                        } else {
//                            isComplete = true;
//                            isFirst = true;
//                            index = 0;
//                            Log.d("deep", "isComplete===>succest");
//                            dataParseCallback.readMeasureData(bytes, parseReadMeasureData(bytes));
//                            removeState();
//                        }
//                    }
//                }
//            } else if (bytes[1] == 0x03) {
//                dataParseCallback.readLabMeasureData(bytes, parseReadLabMeasureData(bytes));
//                removeState();
//            } else if (bytes[1] == 0x04) {
//                dataParseCallback.readRgbMeasureData(bytes, parseReadRgbMeasureData(bytes));
//                removeState();
//            } else if (bytes[1] == 0x12) {
//                if (bytes[2] == 0x00) {
//                    if (isFirst) {
//                        firstBytes = bytes;
//                        isComplete = false;
//                    } else {
//                        Log.d("deep", "judgeSumCheck===>" + ByteUtil.judgeSumCheck(bytes, 249, bytes[249]));
//                        if (!ByteUtil.judgeSumCheck(bytes, 249, bytes[249])) {
//                            isComplete = false;
//                            Log.d("deep", "isComplete1===>" + isComplete);
//                            firstBytes = bytes;
//                        } else {
//                            isComplete = true;
//                            isFirst = true;
//                            index = 0;
//                            Log.d("deep", "isComplete===>succest");
//                            dataParseCallback.getDeviceInfo(bytes, parseDeviceInfo(bytes));
//                            removeState();
//                        }
//
//                    }
//                } else if (bytes[2] == 0x01) {
//                    if (isFirst) {
//                        firstBytes = bytes;
//                        isComplete = false;
//                    } else {
//                        Log.d("deep", "judgeSumCheck===>" + ByteUtil.judgeSumCheck(bytes, 199, bytes[199]));
//                        if (!ByteUtil.judgeSumCheck(bytes, 199, bytes[199])) {
//                            isComplete = false;
//                            Log.d("deep", "isComplete1===>" + isComplete);
//                            firstBytes = bytes;
//                        } else {
//                            isComplete = true;
//                            isFirst = true;
//                            index = 0;
//                            Log.d("deep", "isComplete===>succest");
//                            dataParseCallback.getDeviceInfo(bytes, parseDeviceInfo(bytes));
//                            removeState();
//                        }
//
//                    }
//                }
//
//            } else if (bytes[1] == 0x16) {
//                if (bytes[2] == 0x00) {
//                    dataParseCallback.getStandardCount(bytes, parseStandCount(bytes));
//                    removeState();
//                } else if (bytes[2] == 0x01) {
//                    if (isFirst) {
//                        firstBytes = bytes;
//                        isComplete = false;
//                    } else {
//                        Log.i("deep", "index===>" + index);
//                        if (index == 250) {
//                            Log.i("deep", "index==250===>" + ByteUtil.printArrays(bytes));
//                            Log.i("deep", "judgeSumCheck===>" + ByteUtil.judgeSumCheck(bytes, 249, bytes[249]));
//                            isComplete = true;
//                            isFirst = true;
//                            index = 0;
//                            Log.d("deep", "isComplete===>succest");
//                            dataParseCallback.getStandardDataForNumber(bytes, bytes[5], parseGetStandardForNum(bytes));
//                            removeState();
//                        } else {
//                            isComplete = false;
//                            Log.d("deep", "isComplete1===>" + isComplete);
//                            firstBytes = bytes;
//                        }
////                        if (!ByteUtil.judgeSumCheck(bytes, 249, bytes[249])) {
////                            isComplete = false;
////                            Log.d("deep", "isComplete1===>" + isComplete);
////                            firstBytes = bytes;
////                        } else {
////                            isComplete = true;
////                            isFirst = true;
////                            index = 0;
////                            Log.d("deep", "isComplete===>succest");
////                            dataParseCallback.getStandardDataForNumber(bytes, bytes[5], parseGetStandardForNum(bytes));
////                            removeState();
////
////                        }
//                    }
//                } else if (bytes[2] == 0x04) {
//                    dataParseCallback.deleteAllStandardData(bytes, bytes[3]);
//                    removeState();
//                } else if (bytes[2] == 0x02) {
//                    dataParseCallback.deleteStandardDataForNumber(bytes, ByteUtil.getShort(bytes, 3), bytes[5]);
//                    removeState();
//                } else if (bytes[2] == 0x05) {
//                    dataParseCallback.getSampleCountForStandardNumber(bytes, ByteUtil.getShort(bytes, 3, ByteUtil.TYPE_LOW_FIRST),
//                            bytes[5], ByteUtil.getShort(bytes, 6, ByteUtil.TYPE_LOW_FIRST));
//                    removeState();
//                } else if (bytes[2] == 0x06) {
//                    if (isFirst) {
//                        firstBytes = bytes;
//                        isComplete = false;
//                    } else {
//                        Log.d("deep", "judgeSumCheck===>" + ByteUtil.judgeSumCheck(bytes, 249, bytes[249]));
//                        if (!ByteUtil.judgeSumCheck(bytes, 249, bytes[249])) {
//                            isComplete = false;
//                            Log.d("deep", "isComplete1===>" + isComplete);
//                            firstBytes = bytes;
//                        } else {
//                            isComplete = true;
//                            isFirst = true;
//                            index = 0;
//                            Log.d("deep", "isComplete===>succest");
//                            dataParseCallback.getNumSampleDataForNumStandard(bytes, ByteUtil.getShort(bytes, 3, ByteUtil.TYPE_LOW_FIRST),
//                                    ByteUtil.getShort(bytes, 5, ByteUtil.TYPE_LOW_FIRST), bytes[7], parseGetNumSampleDataForNumStandard(bytes));
//                            removeState();
//                        }
//                    }
//                } else if (bytes[2] == 0x07) {
//                    dataParseCallback.deleteAllSampleForStandardNum(bytes, ByteUtil.getShort(bytes, 3), bytes[5]);
//                    removeState();
//                } else if (bytes[2] == 0x08) {
//                    dataParseCallback.deleteNumSampleDataForNumStandard(bytes, ByteUtil.getShort(bytes, 3),
//                            ByteUtil.getShort(bytes, 5), bytes[7]);
//                    removeState();
//                } else if (bytes[2] == 0x0a) {
//                    dataParseCallback.postStandardData(bytes, bytes[3]);
//                    removeState();
//                }
//            } else if (bytes[1] == 0x17) {
//                dataParseCallback.setPowerManagementTime(bytes, bytes[2]);
//                removeState();
//            } else if (bytes[1] == 0x18) {
//                dataParseCallback.setBluetooth(bytes, bytes[2]);
//                removeState();
//            } else if (bytes[1] == 0x1A) {
//                dataParseCallback.setDeviceDisplayParam(bytes, bytes[2]);
//                removeState();
//            } else if (bytes[1] == 0x14) {
//                dataParseCallback.setDeviceTime(bytes, bytes[2]);
//                removeState();
//            } else if (bytes[1] == 0x1B) {
//                dataParseCallback.setTolerance(bytes, bytes[2]);
//                removeState();
//            } else if (bytes[1] == 0x1C) {
//                dataParseCallback.setSaveMode(bytes, bytes[2]);
//                removeState();
//            } else if (bytes[1] == 0x1D) {
//                dataParseCallback.getDevicePowerInfo(bytes, parseDevicePowerInfo(bytes));
//                removeState();
//            } else if (bytes[1] == 0x1E) {
//                dataParseCallback.getDeviceAdjustState(bytes, parseDeviceAdjustState(bytes));
//                removeState();
//            } else if (bytes[1] == 0x1F) {
//                dataParseCallback.setBluetoothName(bytes, bytes[2]);
//                removeState();
//            }
//        }
//    }

    public static AdjustBean parseAdjust(byte[] bytes) {
        AdjustBean bean = new AdjustBean();
        bean.setAdjustState(bytes[2]);
        bean.setTime(ByteUtil.getInt(bytes, 3, ByteUtil.TYPE_LOW_FIRST));
        bean.setAdjustBeforeState(bytes[7]);
        return bean;
    }

    public static MeasureBean parseMeasure(byte[] bytes) {
        MeasureBean bean = new MeasureBean();
        bean.setMeasureMode(bytes[2]);
        bean.setMeasureState(bytes[3]);
        if (bytes[5] == (byte) 0x90) {
            bean.setStartWavelengths((short) 400);
        } else {
            bean.setStartWavelengths((short) 360);
        }
        bean.setIntervalWavelengths(bytes[6]);
        bean.setWavelengthsCount(bytes[7]);
        return bean;
    }

    public static ReadMeasureDataBean parseReadMeasureData(byte[] bytes) {
        ReadMeasureDataBean bean = new ReadMeasureDataBean();
        bean.setMeasureMode(bytes[2]);
        bean.setDataMode(bytes[3]);
        if (bytes[5] == (byte) 0x90) {
            bean.setStartWavelengths((short) 400);
        } else {
            bean.setStartWavelengths((short) 360);
        }

        bean.setIntervalWavelengths(bytes[6]);
        bean.setWavelengthsCount(bytes[7]);

        if (bean.getDataMode() == 0) {
            float[] dataArrays = new float[bean.getWavelengthsCount()];
            for (int i = 0; i < bean.getWavelengthsCount(); i++) {
                dataArrays[i] = ByteUtil.getFloat(bytes, 8 + i * 4, ByteUtil.TYPE_LOW_FIRST);
            }

            float[] labDataArrays = new float[3];
            for (int i = 0; i < 3; i++) {
                labDataArrays[i] = ByteUtil.getFloat(bytes, 184 + i * 4, ByteUtil.TYPE_LOW_FIRST);
            }
            bean.setData(dataArrays);
            bean.setLabData(labDataArrays);
        } else if (bean.getDataMode() == 1) {
            float[] labDataArrays = new float[3];
            for (int i = 0; i < 3; i++) {
                labDataArrays[i] = ByteUtil.getFloat(bytes, 184 + i * 4, ByteUtil.TYPE_LOW_FIRST);
            }
            bean.setLabData(labDataArrays);
        }
        return bean;
    }

    public static ReadLabMeasureDataBean parseReadLabMeasureData(byte[] bytes) {
        ReadLabMeasureDataBean bean = new ReadLabMeasureDataBean();
        bean.setMeasureMode(bytes[2]);
        bean.setLightSource(bytes[3]);
        bean.setAngle(bytes[4]);
        float[] labDataArrays = new float[3];
        for (int i = 0; i < 3; i++) {
            labDataArrays[i] = ByteUtil.getFloat(bytes, 5 + i * 4, ByteUtil.TYPE_LOW_FIRST);
        }
        bean.setLab(labDataArrays);
        return bean;
    }

    public static ReadRgbMeasureDataBean parseReadRgbMeasureData(byte[] bytes) {
        ReadRgbMeasureDataBean bean = new ReadRgbMeasureDataBean();
        bean.setMeasureMode(bytes[2]);
        bean.setLightSource(bytes[3]);
        bean.setAngle(bytes[4]);
        short[] rgbDataArrays = new short[3];
        for (int i = 0; i < 3; i++) {
            rgbDataArrays[i] = ByteUtil.getShort(bytes, 5 + i * 2, ByteUtil.TYPE_LOW_FIRST);
        }
        bean.setRgb(rgbDataArrays);
        return bean;
    }

    public static short parseStandCount(byte[] bytes) {
        return ByteUtil.getShort(bytes, 3, ByteUtil.TYPE_LOW_FIRST);
    }

    public static StandardSampleDataBean.StandardDataBean parseGetStandardForNum(byte[] bytes) {

        StandardSampleDataBean.StandardDataBean standardDataBean = new StandardSampleDataBean.StandardDataBean();
        standardDataBean.setStandardNum(ByteUtil.getShort(bytes, 3, ByteUtil.TYPE_LOW_FIRST));
        standardDataBean.setState(bytes[6]);
        standardDataBean.setLightSource(ByteUtil.get8Bit(bytes[7]));
        standardDataBean.setAngle(getAngle(bytes[7]));
        standardDataBean.setMeasureMode(bytes[8]);
        standardDataBean.setDataMode(bytes[9]);
        standardDataBean.setStartWave((short) (bytes[10] * 10));
        standardDataBean.setWaveLength(bytes[11]);
        standardDataBean.setWaveCount(bytes[12]);

        char[] nameChars = ByteUtil.getChars(bytes, 13, 18);
        standardDataBean.setName(new String(cutCharArrays(nameChars)));

        standardDataBean.setL(ByteUtil.getFloat(bytes, 34, ByteUtil.TYPE_LOW_FIRST));
        standardDataBean.setA(ByteUtil.getFloat(bytes, 38, ByteUtil.TYPE_LOW_FIRST));
        standardDataBean.setB(ByteUtil.getFloat(bytes, 42, ByteUtil.TYPE_LOW_FIRST));
        List<Float> dataSCI = new ArrayList<>();
        List<Float> dataSCE = new ArrayList<>();
        for (int i = 0; i < bytes[12]; i++) {
            dataSCI.add(ByteUtil.getShort(bytes, 46 + i * 2, ByteUtil.TYPE_LOW_FIRST) / 100f);
            dataSCE.add(ByteUtil.getShort(bytes, 132 + i * 2, ByteUtil.TYPE_LOW_FIRST) / 100f);
        }
        standardDataBean.setDataSCI(dataSCI);
        standardDataBean.setDataSCE(dataSCE);
        standardDataBean.setRecordCount(ByteUtil.getInt(bytes, 218, ByteUtil.TYPE_LOW_FIRST));
        standardDataBean.setTime(ByteUtil.getInt(bytes, 222, ByteUtil.TYPE_LOW_FIRST));

        return standardDataBean;
    }

    public static StandardSampleDataBean.SampleDataBean parseGetNumSampleDataForNumStandard(byte[] bytes) {
        StandardSampleDataBean.SampleDataBean sampleDataBean = new StandardSampleDataBean.SampleDataBean();
        sampleDataBean.setState(bytes[8]);
        sampleDataBean.setLightSource(ByteUtil.get8Bit(bytes[9]));
        sampleDataBean.setAngle(getAngle(bytes[9]));
        sampleDataBean.setDataMode(bytes[10]);
        sampleDataBean.setStartWave((short) (bytes[12] * 10));
        sampleDataBean.setWaveLength(bytes[13]);
        sampleDataBean.setWaveCount(bytes[14]);
        char[] nameChars = ByteUtil.getChars(bytes, 15, 18);
        sampleDataBean.setName(new String(cutCharArrays(nameChars)));
        sampleDataBean.setL(ByteUtil.getFloat(bytes, 36));
        sampleDataBean.setA(ByteUtil.getFloat(bytes, 40));
        sampleDataBean.setB(ByteUtil.getFloat(bytes, 44));
        List<Float> dataSCI = new ArrayList<>();
        List<Float> dataSCE = new ArrayList<>();
        for (int i = 0; i < bytes[14]; i++) {
            dataSCI.add(ByteUtil.getShort(bytes, 48 + i * 2) / 100f);
            dataSCE.add(ByteUtil.getShort(bytes, 134 + i * 2) / 100f);
        }
        sampleDataBean.setDataSCI(dataSCI);
        sampleDataBean.setDataSCE(dataSCE);
        sampleDataBean.setTime(ByteUtil.getInt(bytes, 224, ByteUtil.TYPE_LOW_FIRST));
        return sampleDataBean;
    }

    public static DeviceInfoStruct parseDeviceInfo(byte[] bytes) {
        DeviceInfoStruct struct = new DeviceInfoStruct();
        struct.setFlag(bytes[3]);
        struct.setDeviceCode(ByteUtil.getShort(bytes, 5));
        char[] originalNumChars = cutCharArrays(ByteUtil.getChars(bytes, 7, 30));
        char[] instrumentModelChars = cutCharArrays(ByteUtil.getChars(bytes, 37, 30));
        char[] instrumentSerialChars = cutCharArrays(ByteUtil.getChars(bytes, 67, 30));
        char[] softwareVersionChars = cutCharArrays(ByteUtil.getChars(bytes, 97, 30));
        char[] hardwareVersionChars = cutCharArrays(ByteUtil.getChars(bytes, 127, 30));
        struct.setOriginalNum(new String(originalNumChars));
        struct.setInstrumentModel(new String(instrumentModelChars));
        struct.setInstrumentSerial(new String(instrumentSerialChars));
        struct.setSoftwareVersion(new String(softwareVersionChars));
        struct.setHardwareVersion(new String(hardwareVersionChars));
        struct.setModeDisplayFlag(bytes[138]);
        struct.setPrototypeFlag(bytes[139]);
        struct.setNeutralFlag(bytes[140]);
        struct.setLimitTimes(ByteUtil.getShort(bytes, 141));
        struct.setCameraXStart(ByteUtil.getShort(bytes, 143));
        struct.setCameraYStart(ByteUtil.getShort(bytes, 145));
        return struct;
    }

    public static DeviceInfoBean.PowerInfo parseDevicePowerInfo(byte[] bytes) {
        DeviceInfoBean.PowerInfo powerInfo = new DeviceInfoBean.PowerInfo();
        powerInfo.setElectricQuantity(bytes[2]);
        return powerInfo;
    }

    public static DeviceInfoBean.DeviceAdjustState parseDeviceAdjustState(byte[] bytes) {
        DeviceInfoBean.DeviceAdjustState deviceAdjustState = new DeviceInfoBean.DeviceAdjustState();
        deviceAdjustState.setWhiteAdjustState(bytes[2]);
        deviceAdjustState.setBlackAdjustState(bytes[7]);
        deviceAdjustState.setWhiteADjustTime(ByteUtil.getInt(bytes, 3));
        deviceAdjustState.setBlackAdjustTime(ByteUtil.getInt(bytes, 8));
        return deviceAdjustState;
    }

    public static byte getAngle(byte b) {
        b = (byte) ((b & 0b10000000) >>> 7);
        return b;
    }

    /**
     * 分割无用字符 (Segmentation of useless characters)
     *
     * @return
     */
    public static char[] cutCharArrays(char[] chars) {
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == 0x00) {
                chars = Arrays.copyOf(chars, i);
            }
        }
        return chars;
    }
}
