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

import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import com.google.corp.productivity.specialprojects.android.fft.RealDoubleFFT;

/**
 * Created by frobic on 02/11/15.
 */
public class VibrationMeasurement implements SensorEventListener {

    private double FREQMIN = 25;


    // For motion :
    private SensorManager mSensorManager = null;
    private Sensor mAccelerometer = null;
    private int delay = SensorManager.SENSOR_DELAY_FASTEST; // 0,2s

    private List xAccHistory = new LinkedList();
    private List yAccHistory = new LinkedList();
    private List zAccHistory = new LinkedList();
    private List tAccHistory  = new LinkedList();


    public VibrationMeasurement(Context mContext) {

        mSensorManager =
                (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mSensorManager.registerListener(this, mAccelerometer, delay);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];
            xAccHistory.add(x);
            yAccHistory.add(y);
            zAccHistory.add(z);
            tAccHistory.add((double) event.timestamp * Math.pow(0.1,9.0));
        }

    }

    public String[] stopStreaming() {

        mSensorManager.unregisterListener(this);

        String[] result = {"Result1"};
        result[0] = process(xAccHistory, yAccHistory, zAccHistory, tAccHistory);

        xAccHistory.clear();
        yAccHistory.clear();
        zAccHistory.clear();

        return result;
    }


    public String process(List xAcc, List yAcc, List zAcc, List tAcc) {

        double rep = process4repetition_sub(xAcc, yAcc, zAcc, tAcc);

        return ""+rep ;

    }

    public double process4repetition_sub(List xAcc, List yAcc, List zAcc, List tAcc){
        double result = 0;
        for(int i = 0; i < yAcc.size(); i++) {
            double y = (double)yAcc.get(i);
            result = (result*i + y)/(i+1);
        }
        //List cos = new LinkedList();
        double[] xAccFeatures;
        double[] yAccFeatures;
        double[] zAccFeatures;

        xAccFeatures = computeInputFeatures(xAcc, tAcc);
        yAccFeatures = computeInputFeatures(yAcc, tAcc);
        zAccFeatures = computeInputFeatures(zAcc, tAcc);


        double rep = xAccFeatures[0]+yAccFeatures[0]+zAccFeatures[0];

        Log.i("Reponse", ""+Math.sqrt(rep));

        return Math.sqrt(rep);
    }

    public double[] computeInputFeatures(List coord, List time){
        double[] ff;
        double[] norm;
        double pas = 0;
        double[] result = new double[4];
        int nAcc = coord.size();

        for (int i = 0 ; i <  nAcc-1 ; i++) {
            pas = pas * ((double) i / (double) (i + 1)) + ((double) time.get(i+1)- (double)time.get(i)) / (double) (i + 1);
        }


        ff = new double[nAcc];
        norm = new double[nAcc/2];

        RealDoubleFFT spectrumAmpFFT;
        Log.i("n",""+nAcc);
        spectrumAmpFFT   = new RealDoubleFFT(nAcc);
        for (int i = 0 ; i < nAcc ; i++) {
            ff[i] = (double) coord.get(i);
        }
        spectrumAmpFFT.ft(ff);
        norm[0] = Math.abs(ff[0])/nAcc;
        for (int i = 1 ; i < nAcc/2 ; i++) {
            norm[i] = 2.*Math.sqrt(ff[2*i]*ff[2*i]+ff[2*i-1]*ff[2*i-1])/nAcc;
        }

        int i0 = ind(FREQMIN, pas, nAcc);
        int i1 = nAcc/2-1;


        result[0] = normL2(norm, i0, i1, 0);

        return result;

    }

    public int ind(double f, double pas, int n) {
        return (int) Math.ceil((double) n * f * pas);
    }

    public double normL2(double[] t, int a, int b, int squared) {
        double r = 0;
        for (int i = a ; i <= b ; i++) {
            r += t[i]*t[i];
        }

        if (squared == 1) {
            return r;
        } else {
            return Math.sqrt(r);
        }
    }



}
