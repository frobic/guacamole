package com.pesto.tapdata;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends Activity{

    private ListView mListSensors;
    private ListView mListFreq;
    private Button mWrite ;
    private Button mStart;

    private String[] mSensors;
    private String[] mFreq = {"Normal", "UI", "Jeu", "Le plus rapide"};

    private SensorManager mSensorManager;
    private int mNbSensors = 5;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mGravity;
    private Sensor mLinearAcceleration;
    private Sensor mRotationVector;
    private Sensor[] mSensorsArray = new Sensor[mNbSensors];





    private boolean isRecording = false;
    private String output;

    final SensorEventListener mEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (isRecording) {
                int i = Arrays.asList(mSensorsArray).indexOf(sensorEvent.sensor);
                    if (mListSensors.getCheckedItemPositions().get(i)) {
                        long time = sensorEvent.timestamp;
                        output += time + "(ns)," + mSensors[i] + "," + Arrays.toString(sensorEvent.values).replace("[","").replace("]","")+ "\n";
                    }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListSensors = (ListView) findViewById(R.id.listSensors);
        mListFreq = (ListView) findViewById(R.id.listFreq);
        mWrite = (Button) findViewById(R.id.writeData);
        mStart = (Button) findViewById(R.id.start);
        mWrite.setEnabled(false);

        mSensors = new String[]{"Accelerometre", "Gyroscope", "Gravite", "Accelerationlineaire", "Vecteurderotation"};
        mListSensors.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, mSensors));
        mListSensors.setItemChecked(0, true);

        mListFreq.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, mFreq));
        mListFreq.setItemChecked(0, true);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensorsArray[0] = mAccelerometer;
        mSensorsArray[1] = mGyroscope;
        mSensorsArray[2] = mGravity;
        mSensorsArray[3] = mLinearAcceleration;
        mSensorsArray[4] = mRotationVector;

        mWrite.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Les données ont été enregistrées !", Toast.LENGTH_LONG).show();




                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"bogard.vincent@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Data");
                i.putExtra(Intent.EXTRA_TEXT, output);
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }

                output = "";
                mWrite.setEnabled(false);
                mStart.setEnabled(true);
                isRecording = false;

                for(Sensor sensor : mSensorsArray){
                    mSensorManager.unregisterListener(mEventListener,sensor);
                }
            }
        });

        mStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Début de la mesure !", Toast.LENGTH_LONG).show();

                output = "";
                mStart.setEnabled(false);
                mWrite.setEnabled(true);
                isRecording = true;



                switch (mListFreq.getCheckedItemPosition())
                {
                    case 0:
                        for(Sensor sensor : mSensorsArray)
                            mSensorManager.registerListener(mEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                        break;
                    case 1:
                        for(Sensor sensor : mSensorsArray)
                            mSensorManager.registerListener(mEventListener, sensor, SensorManager.SENSOR_DELAY_UI);
                        break;
                    case 2:
                        for(Sensor sensor : mSensorsArray)
                            mSensorManager.registerListener(mEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);
                        break;
                    case 3:
                        for(Sensor sensor : mSensorsArray)
                            mSensorManager.registerListener(mEventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
                        break;
                }
            }
        });
    }
}
