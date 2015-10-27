package com.example.android.network.sync.basicsyncadapter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jbgueusquin on 26/10/2015.
 */
public class MotionMeasurement implements SensorEventListener {

    // For motion :
    private SensorManager mSensorManager = null;
    private Sensor mAccelerometer = null;
    private Sensor mGyroscope = null;
    private int delay = SensorManager.SENSOR_DELAY_NORMAL; // 0,2s

    private List xAccHistory = new LinkedList();
    private List yAccHistory = new LinkedList();
    private List zAccHistory = new LinkedList();
    private List xGyrHistory = new LinkedList();
    private List yGyrHistory = new LinkedList();
    private List zGyrHistory = new LinkedList();


    public MotionMeasurement(Context mContext) {
        mSensorManager =
                (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mSensorManager.registerListener(this, mAccelerometer, delay);
        mSensorManager.registerListener(this, mGyroscope, delay);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            xAccHistory.add(x);
            yAccHistory.add(y);
            zAccHistory.add(z);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            xGyrHistory.add(x);
            yGyrHistory.add(y);
            zGyrHistory.add(z);
        }

    }

    public String[] stopStreaming() {

        mSensorManager.unregisterListener(this);

        String[] result = {"Result1", "Result2"};
        result[0] = process4vibration(xAccHistory, yAccHistory, zAccHistory, xGyrHistory, yGyrHistory, zGyrHistory);
        result[1] = process4repetition(xAccHistory, yAccHistory, zAccHistory, xGyrHistory, yGyrHistory, zGyrHistory);

        xAccHistory.clear();
        yAccHistory.clear();
        zAccHistory.clear();
        xGyrHistory.clear();
        yGyrHistory.clear();
        zGyrHistory.clear();

        return result;
    }

    public String process4vibration(List xAcc, List yAcc, List zAcc, List xGyr, List yGyr, List zGyr){
        Log.i("Mesures", xAccHistory.size() + " for acceleration");
        Log.i("Mesures", xGyrHistory.size() + " for gyroscope");

        float result = 0;
        for(int i = 0; i < xAcc.size(); i++) {
            float x = (float)xAcc.get(i);
            result = (result*i + x)/(i+1);
        }

        return String.format("%.3f (vib.)", result);
    }

    public String process4repetition(List xAcc, List yAcc, List zAcc, List xGyr, List yGyr, List zGyr){
        float result = 0;
        for(int i = 0; i < yAcc.size(); i++) {
            float y = (float)yAcc.get(i);
            result = (result*i + y)/(i+1);
        }

        return String.format("%.3f (rep.)", result) ;
    }
}
