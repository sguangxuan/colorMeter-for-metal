package com.example.bluetoothsdkdemo.util;

import android.util.Log;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class TimeUtil {
    public static String unixTimestamp2Date(int time){
        Log.d("deep","unixTimestamp2Date===>time===>"+time);
        long t = time*1000L;
        String date = new SimpleDateFormat("yyyy/dd/MM HH:mm:ss").format(new Date(t));
        return date;
    }
    public static int getUnixTime(){
        return (int) (System.currentTimeMillis() / 1000);
    }
}
