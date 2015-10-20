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

public class MainActivity extends Activity {

    private ListView mListSensors = null;
    private ListView mListFreq = null;
    private Button mWrite = null;
    private Button mStart = null;

    private String[] mSensors = null;
    private String[] mFreq = {"Normal", "UI", "Jeu", "Le plus rapide"};

    private SensorManager mSensorManager = null;
    private Sensor mAccelerometer = null;
    private Sensor mGyroscope = null;

    private boolean isRecording = false;
    private String output = "";

    final SensorEventListener mAccelerometerEventListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            if(isRecording && mListSensors.getCheckedItemPositions().get(0)) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];
                long time = sensorEvent.timestamp;
                output += time + "(ns)," + "accelerometer," + x + "," + y + "," + z + "\n";
            }
        }
    };

    final SensorEventListener mGyroscopeEventListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            if(isRecording && mListSensors.getCheckedItemPositions().get(1)) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];
                long time = sensorEvent.timestamp;
                output += time + "(ns)," + "gyroscope," + x + "," + y + "," + z + "\n";
            }
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

        mSensors = new String[]{"Accéléromètre", "Gyroscope"};
        mListSensors.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, mSensors));
        mListSensors.setItemChecked(0, true);

        mListFreq.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, mFreq));
        mListFreq.setItemChecked(0, true);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mWrite.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Les données ont été enregistrées !", Toast.LENGTH_LONG).show();

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"jbgueusquin@gmail.com"});
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

                mSensorManager.unregisterListener(mAccelerometerEventListener, mAccelerometer);
                mSensorManager.unregisterListener(mGyroscopeEventListener, mGyroscope);
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
                        mSensorManager.registerListener(mAccelerometerEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                        mSensorManager.registerListener(mGyroscopeEventListener, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
                        break;
                    case 1:
                        mSensorManager.registerListener(mAccelerometerEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                        mSensorManager.registerListener(mGyroscopeEventListener, mGyroscope, SensorManager.SENSOR_DELAY_UI);
                        break;
                    case 2:
                        mSensorManager.registerListener(mAccelerometerEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
                        mSensorManager.registerListener(mGyroscopeEventListener, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
                        break;
                    case 3:
                        mSensorManager.registerListener(mAccelerometerEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                        mSensorManager.registerListener(mGyroscopeEventListener, mGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
                        break;
                }
            }
        });
    }
}
