package com.example.frobic.myapplication;

import android.content.ContextWrapper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import java.util.List;
import android.content.Context;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import java.util.LinkedList;
import java.util.ListIterator;
import java.io.FileOutputStream;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    TextView xData = null;
    TextView yData = null;
    TextView zData = null;

    List xHistory = new LinkedList();
    List yHistory = new LinkedList();
    List zHistory = new LinkedList();

    public static void longInfo(String str) {
        if(str.length() > 4000) {
            Log.e("T", str.substring(0, 4000));
            longInfo(str.substring(4000));
        } else
            Log.e("T", str);
    }

    private SensorManager mSensorManager = null;
    private Sensor mAccelerometer = null;

    final SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            xHistory.add(x);
            yHistory.add(y);
            zHistory.add(z);


            xData.setText("X: " + x);
            yData.setText("Y: " + y);
            zData.setText("Z: " + z);
        }

        @Override
        public void onAccuracyChanged(Sensor pSensor, int pAccuracy) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        xData = (TextView)findViewById(R.id.x);
        yData = (TextView)findViewById(R.id.y);
        zData = (TextView)findViewById(R.id.z);


        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        TextView text;
        String myText;
        ContextWrapper c = new ContextWrapper(this);
        Toast.makeText(this, c.getFilesDir().getPath(), Toast.LENGTH_LONG).show();

        text = (TextView) findViewById(R.id.textView);

        myText = (String) text.getText();

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename = "myfile";
                FileOutputStream outputStream;

                try {
                    outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                    Log.e("Error","Est-ce que je passe ?");
                    outputStream.write(xHistory.toString().getBytes());
                    outputStream.write(yHistory.toString().getBytes());
                    outputStream.write(zHistory.toString().getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorEventListener, mAccelerometer);
    }
}
