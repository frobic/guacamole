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
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Attribute;
import weka.core.SparseInstance;
import weka.classifiers.Classifier;


/**
 * Created by jbgueusquin on 26/10/2015.
 */
public class MotionMeasurement implements SensorEventListener {

    private double FREQMIN = 0.5;
    private double FREQMAX = 15;
    private double[] FREQLIM = {3., 7.};


    // For motion :
    private SensorManager mSensorManager = null;
    private Sensor mAccelerometer = null;
    private Sensor mGyroscope = null;
    private int delay = SensorManager.SENSOR_DELAY_GAME; // 0,2s

    private List xAccHistory = new LinkedList();
    private List yAccHistory = new LinkedList();
    private List zAccHistory = new LinkedList();
    private List tAccHistory  = new LinkedList();

    private List xGyrHistory = new LinkedList();
    private List yGyrHistory = new LinkedList();
    private List zGyrHistory = new LinkedList();
    private List tGyrHistory = new LinkedList();
    private Classifier cls;


    private Attribute Attribute1 = new Attribute("Accelerometre_Axis_x_module_fft");
    private Attribute Attribute2 = new Attribute("Accelerometre_Axis_x_module_fft_plage_1");
    private Attribute Attribute3 = new Attribute("Accelerometre_Axis_x_module_fft_plage_2");
    private Attribute Attribute4 = new Attribute("Accelerometre_Axis_x_module_fft_plage_3");
    private Attribute Attribute5 = new Attribute("Accelerometre_Axis_y_module_fft");
    private Attribute Attribute6 = new Attribute("Accelerometre_Axis_y_module_fft_plage_1");
    private Attribute Attribute7 = new Attribute("Accelerometre_Axis_y_module_fft_plage_2");
    private Attribute Attribute8 = new Attribute("Accelerometre_Axis_y_module_fft_plage_3");
    private Attribute Attribute9 = new Attribute("Accelerometre_Axis_z_module_fft");
    private Attribute Attribute10 = new Attribute("Accelerometre_Axis_z_module_fft_plage_1");
    private Attribute Attribute11 = new Attribute("Accelerometre_Axis_z_module_fft_plage_2");
    private Attribute Attribute12 = new Attribute("Accelerometre_Axis_z_module_fft_plage_3");
    private Attribute Attribute13 = new Attribute("Gyroscope_Axis_x_module_fft");
    private Attribute Attribute14 = new Attribute("Gyroscope_Axis_x_module_fft_plage_1");
    private Attribute Attribute15 = new Attribute("Gyroscope_Axis_x_module_fft_plage_2");
    private Attribute Attribute16 = new Attribute("Gyroscope_Axis_x_module_fft_plage_3");
    private Attribute Attribute17 = new Attribute("Gyroscope_Axis_y_module_fft");
    private Attribute Attribute18 = new Attribute("Gyroscope_Axis_y_module_fft_plage_1");
    private Attribute Attribute19 = new Attribute("Gyroscope_Axis_y_module_fft_plage_2");
    private Attribute Attribute20 = new Attribute("Gyroscope_Axis_y_module_fft_plage_3");
    private Attribute Attribute21 = new Attribute("Gyroscope_Axis_z_module_fft");
    private Attribute Attribute22 = new Attribute("Gyroscope_Axis_z_module_fft_plage_1");
    private Attribute Attribute23 = new Attribute("Gyroscope_Axis_z_module_fft_plage_2");
    private Attribute Attribute24 = new Attribute("Gyroscope_Axis_z_module_fft_plage_3");

    private FastVector fvClassVal = new FastVector(2);

    private Attribute ClassAttribute;
    private FastVector fvWekaAttributes = new FastVector(25);
    private Instances mySet;


    public MotionMeasurement(Context mContext) {

        InputStream mymodel = mContext.getResources().openRawResource(R.raw.mymodel);
        try {
            cls = (Classifier) weka.core.SerializationHelper.read(mymodel);
        } catch(Exception e) {
            Log.e("Erreur",e.getMessage());
        }

        fvClassVal.addElement("NonRepetitive");
        fvClassVal.addElement("Repetitive");
        ClassAttribute = new Attribute("class", fvClassVal);
        fvWekaAttributes.addElement(Attribute1);
        fvWekaAttributes.addElement(Attribute2);
        fvWekaAttributes.addElement(Attribute3);
        fvWekaAttributes.addElement(Attribute4);
        fvWekaAttributes.addElement(Attribute5);
        fvWekaAttributes.addElement(Attribute6);
        fvWekaAttributes.addElement(Attribute7);
        fvWekaAttributes.addElement(Attribute8);
        fvWekaAttributes.addElement(Attribute9);
        fvWekaAttributes.addElement(Attribute10);
        fvWekaAttributes.addElement(Attribute11);
        fvWekaAttributes.addElement(Attribute12);
        fvWekaAttributes.addElement(Attribute13);
        fvWekaAttributes.addElement(Attribute14);
        fvWekaAttributes.addElement(Attribute15);
        fvWekaAttributes.addElement(Attribute16);
        fvWekaAttributes.addElement(Attribute17);
        fvWekaAttributes.addElement(Attribute18);
        fvWekaAttributes.addElement(Attribute19);
        fvWekaAttributes.addElement(Attribute20);
        fvWekaAttributes.addElement(Attribute21);
        fvWekaAttributes.addElement(Attribute22);
        fvWekaAttributes.addElement(Attribute23);
        fvWekaAttributes.addElement(Attribute24);
        fvWekaAttributes.addElement(ClassAttribute);

        mySet = new Instances("Rel", fvWekaAttributes, 10);
        mySet.setClassIndex(24);

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
            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];
            xAccHistory.add(x);
            yAccHistory.add(y);
            zAccHistory.add(z);
            tAccHistory.add((double) event.timestamp * Math.pow(0.1,9.0));
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];
            xGyrHistory.add(x);
            yGyrHistory.add(y);
            zGyrHistory.add(z);
            tGyrHistory.add((double) event.timestamp * Math.pow(0.1,9.0));
        }

    }

    public String[] stopStreaming() {

        mSensorManager.unregisterListener(this);

        String[] result = {"Result1", "Result2"};
        result[0] = process4vibration(xAccHistory, yAccHistory, zAccHistory, tAccHistory, xGyrHistory, yGyrHistory, zGyrHistory, tGyrHistory);
        result[1] = process4repetition(xAccHistory, yAccHistory, zAccHistory, tAccHistory, xGyrHistory, yGyrHistory, zGyrHistory, tGyrHistory);

        xAccHistory.clear();
        yAccHistory.clear();
        zAccHistory.clear();
        xGyrHistory.clear();
        yGyrHistory.clear();
        zGyrHistory.clear();

        return result;
    }

    public String process4vibration(List xAcc, List yAcc, List zAcc, List tAcc, List xGyr, List yGyr, List zGyr, List tGyr){
        Log.i("Mesures", xAccHistory.size() + " for acceleration");
        Log.i("Mesures", xGyrHistory.size() + " for gyroscope");

        double result = 0;
        for(int i = 0; i < xAcc.size(); i++) {
            double x = (double)xAcc.get(i);
            result = (result*i + x)/(i+1);
        }

        return String.format("%.3f (vib.)", result);
    }

    public String process4repetition(List xAcc, List yAcc, List zAcc, List tAcc, List xGyr, List yGyr, List zGyr, List tGyr) {

        double step = 1.;
        double size = 15.;

        int rep = 0;

        double tmin = (double) tAcc.get(0);
        double tmax = (double) tAcc.get(tAcc.size()-1);

        tmin = (tmin < (double) tGyr.get(0))?tmin:(double) tGyr.get(0);
        tmax = (tmax > (double) tGyr.get(tGyr.size()-1))?tmax:(double) tGyr.get(tGyr.size()-1);

        double tWinB = tmin;
        double tWinE = tmin+size;

        int accB = 0;
        int accE = 0;
        int gyrB = 0;
        int gyrE = 0;

        int nbWindows = 0;

        while(tWinE < tmax) {
            nbWindows++;
            while ((double) tAcc.get(accB) < tWinB && accB < tAcc.size() - 1) {
                accB++;
            }
            while ((double) tAcc.get(accE) < tWinE && accE < tAcc.size() - 1) {
                accE++;
            }
            while ((double) tAcc.get(gyrB) < tWinB && gyrB < tGyr.size() - 1) {
                gyrB++;
            }
            while ((double) tGyr.get(gyrE) < tWinE && gyrE < tGyr.size() - 1) {
                gyrE++;
            }
            rep += process4repetition_sub(xAcc.subList(accB, accE), yAcc.subList(accB,accE), zAcc.subList(accB,accE), tAcc.subList(accB,accE), xGyr.subList(gyrB,gyrE), yGyr.subList(gyrB,gyrE), zGyr.subList(gyrB,gyrE), tGyr.subList(gyrB,gyrE));
            tWinB = tWinB + step;
            tWinE = tWinE + step;
        }

        if ((double) rep / (double) nbWindows > .66) {
            return "Repetitive";
        }
        return "Non Repetitive";

    }

    public int process4repetition_sub(List xAcc, List yAcc, List zAcc, List tAcc, List xGyr, List yGyr, List zGyr, List tGyr){
        double result = 0;
        for(int i = 0; i < yAcc.size(); i++) {
            double y = (double)yAcc.get(i);
            result = (result*i + y)/(i+1);
        }
        //List cos = new LinkedList();
        double[] xAccFeatures;
        double[] yAccFeatures;
        double[] zAccFeatures;
        double[] xGyrFeatures;
        double[] yGyrFeatures;
        double[] zGyrFeatures;

        xAccFeatures = computeInputFeatures(xAcc, tAcc);
        yAccFeatures = computeInputFeatures(yAcc, tAcc);
        zAccFeatures = computeInputFeatures(zAcc, tAcc);
        xGyrFeatures = computeInputFeatures(xGyr, tGyr);
        yGyrFeatures = computeInputFeatures(yGyr, tGyr);
        zGyrFeatures = computeInputFeatures(zGyr, tGyr);

        double features[] = new double[24];

        for (int i = 0 ; i < 4 ; i++) {
            features[i] = xAccFeatures[i];
            features[i+4] = yAccFeatures[i];
            features[i+8] = zAccFeatures[i];
            features[i+12] = xGyrFeatures[i];
            features[i+16] = yGyrFeatures[i];
            features[i+20] = zGyrFeatures[i];
        }


        Instance myInstance;
        myInstance = toInstance(features);
        myInstance.setDataset(mySet);

        String response = "Erreur";
        int rep = -1;
        try {
            double myClass = cls.classifyInstance(myInstance);
            rep = (int) myClass;
            response = mySet.classAttribute().value((int) myClass);
        } catch(Exception e) {
            Log.e("Erreur",e.getMessage());
        }

        Log.i("Reponse", response);

        return rep;
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
        spectrumAmpFFT   = new RealDoubleFFT(nAcc);
        for (int i = 0 ; i < nAcc ; i++) {
            ff[i] = (double) coord.get(i);
        }
        spectrumAmpFFT.ft(ff);
        norm[0] = Math.abs(ff[0])/nAcc;
        for (int i = 1 ; i < nAcc/2 ; i++) {
            norm[i] = Math.sqrt(ff[2*i]*ff[2*i]+ff[2*i-1]*ff[2*i-1])/nAcc;
        }

        int i0 = ind(FREQMIN, pas, nAcc);
        int i1 = ind(FREQLIM[0], pas, nAcc);
        int i2 = ind(FREQLIM[1], pas, nAcc);
        int i3 = ind(FREQMAX, pas, nAcc);


        result[0] = normL2(norm, i0, i3, 0);
        result[1] = normL2(norm,i0,i1,1)/(result[0]*result[0]);
        result[2] = normL2(norm,i1+1,i2,1)/(result[0]*result[0]);
        result[3] = normL2(norm,i2+1,i3,1)/(result[0]*result[0]);

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
    public Instance toInstance(double[] values) {
        Instance result = new SparseInstance(values.length+1);
        for (int i = 0; i < values.length; i++) {
            result.setValue((Attribute) fvWekaAttributes.elementAt(i), values[i]);
        }
        return result;
    }

}
