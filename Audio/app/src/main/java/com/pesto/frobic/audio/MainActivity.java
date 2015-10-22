package com.pesto.frobic.audio;

import android.app.Activity;
import android.content.Context;
import android.media.AudioRecord;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.lang.Math;
import com.google.corp.productivity.specialprojects.android.fft.RealDoubleFFT;



public class MainActivity extends AppCompatActivity {

    //Properties (AsyncTask)
    protected TextView _textField;
    protected InitTask _initTask;

    public AudioRecord audioRecord;
    public static final int SAMPPERSEC = 8000;
    public int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    public int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    public int buffersizebytes;
    public static short[] buffer;
    public int mSamplesRead;
    List history = new LinkedList();
    File root;
    File dir;
    File file;
    FileOutputStream f;
    PrintWriter pw;

    int max = 0;
    int min = 0;

    private static final String TAG = "MEDIA";


    @Override
    protected void onDestroy() {
        try {
            pw.flush();
            pw.close();
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
         }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        _textField = (TextView)findViewById(R.id.text);

        buffersizebytes = AudioRecord.getMinBufferSize(SAMPPERSEC, channelConfiguration, audioEncoding)*8; //4096 on ion
        buffersizebytes = 4096;
        buffer = new short[buffersizebytes];
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,SAMPPERSEC,channelConfiguration,audioEncoding,buffersizebytes); //constructor

        _initTask = new InitTask();
        _initTask.execute(this);

        root = android.os.Environment.getExternalStorageDirectory();
        _textField.append("\nExternal file system root: " + root);

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

        dir = new File (root.getAbsolutePath() + "/download");
        dir.mkdirs();
        file = new File(dir, "myData.txt");

        try {
            f = new FileOutputStream(file);
            pw = new PrintWriter(f);
        } catch (Exception e) {
            e.printStackTrace();
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, ""+buffersizebytes, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                String filename = "myfile";
                FileOutputStream outputStream;

                try {
                    //outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                    //outputStream.write(history.toString().getBytes());
                    //outputStream.close();

                    //pw.println(history.toString());
                    Log.i("Max", ""+max);
                    Log.i("Min", ""+min);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected class InitTask extends AsyncTask<Context, Integer, String>
    {



        // -- run intensive processes here
        // -- notice that the datatype of the first param in the class definition matches the param passed to this method
        // -- and that the datatype of the last param in the class definition matches the return type of this method
        @Override
        protected String doInBackground( Context... params )
        {
            //-- on every iteration
            //-- runs a while loop that causes the thread to sleep for 50 milliseconds
            //-- publishes the progress - calls the onProgressUpdate handler defined below
            //-- and increments the counter variable i by one
            //int i = 0;

            audioRecord.startRecording();
            double[] ff;
            ff = new double[4096];
            RealDoubleFFT spectrumAmpFFT;
            spectrumAmpFFT   = new RealDoubleFFT(4096);

            while( true )
            {
                try{
                    mSamplesRead = audioRecord.read(buffer, 0, buffersizebytes);

                    Log.i("Diff",""+(mSamplesRead));
                    int amp;
                    float moy = 0;

                    for(int i = 0; i < mSamplesRead - 1; i++){
                        ff[i] = (double)buffer[i];
                        //moy = Math.abs((float) buffer[i])/(float)(i+1)+moy*(float)i/(float)(i+1);
                        amp = (int)buffer[i];
                        max = (max > amp)?max:amp;
                        min = (min < amp)?min:amp;
                        publishProgress( amp );
                    }
                    spectrumAmpFFT.ft(ff);

                    //publishProgress( (int)moy );

                } catch( Exception e ){
                }
            }
        }

        // -- gets called just before thread begins
        @Override
        protected void onPreExecute()
        {
            //Log.i( "makemachine", "onPreExecute()" );
            super.onPreExecute();

        }

        // -- called from the publish progress
        // -- notice that the datatype of the second param gets passed to this method
        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
            //Log.i( "makemachine", "onProgressUpdate(): " +  String.valueOf( values[0] ) );
            //history.add(values[0]);
            pw.println(values[0]+"");
            //_textField.setText( String.valueOf(values[0]));
        }

        // -- called as soon as doInBackground method completes
        // -- notice that the third param gets passed to this method
        @Override
        protected void onPostExecute( String result ) {
            super.onPostExecute(result);
            //Log.i( "makemachine", "onPostExecute(): " + result );
        }
    }

    private void checkExternalMedia(){
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        _textField.append("\n\nExternal Media: readable="
                + mExternalStorageAvailable + " writable=" + mExternalStorageWriteable);
    }

    /** Method to write ascii text characters to file on SD card. Note that you must add a
     WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
     a FileNotFound Exception because you won't have write permission. */

    private void writeToSDFile(){

        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

        File root = android.os.Environment.getExternalStorageDirectory();
        _textField.append("\nExternal file system root: "+root);

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

        File dir = new File (root.getAbsolutePath() + "/download");
        dir.mkdirs();
        File file = new File(dir, "myData.csv");

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.println("Hi , How are you");
            pw.println("Hello");
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
        _textField.append("\n\nFile written to " + file);
    }

    /** Method to read in a text file placed in the res/raw directory of the application. The
     method reads in all lines of the file sequentially. */

    private void readRaw(){
        _textField.append("\nData read from res/raw/textfile.txt:");
        InputStream is = this.getResources().openRawResource(R.raw.textfile);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr, 8192);    // 2nd arg is buffer size

        // More efficient (less readable) implementation of above is the composite expression
    /*BufferedReader br = new BufferedReader(new InputStreamReader(
            this.getResources().openRawResource(R.raw.textfile)), 8192);*/

        try {
            String test;
            while (true){
                test = br.readLine();
                // readLine() returns null if no more lines in the file
                if(test == null) break;
                _textField.append("\n"+"    "+test);
            }
            isr.close();
            is.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        _textField.append("\n\nThat is all");
    }
}