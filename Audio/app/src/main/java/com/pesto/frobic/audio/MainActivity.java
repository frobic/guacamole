package com.pesto.frobic.audio;

import android.content.Context;
import android.media.AudioRecord;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        _textField = (TextView)findViewById(R.id.text);

        buffersizebytes = AudioRecord.getMinBufferSize(SAMPPERSEC, channelConfiguration, audioEncoding); //4096 on ion
        buffer = new short[buffersizebytes];
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,SAMPPERSEC,channelConfiguration,audioEncoding,buffersizebytes); //constructor

        _initTask = new InitTask();
        _initTask.execute( this );

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, ""+buffersizebytes, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                String filename = "myfile";
                FileOutputStream outputStream;

                try {
                    outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(history.toString().getBytes());
                    outputStream.close();
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

            while( true )
            {
                try{
                    mSamplesRead = audioRecord.read(buffer, 0, buffersizebytes);

                    int amp;

                    for(int i = 0; i < buffersizebytes - 1; i++){
                        amp = (int)buffer[i];
                        publishProgress( amp );
                    }

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
            history.add(values[0]);
            _textField.setText( String.valueOf(values[0]) );
        }

        // -- called as soon as doInBackground method completes
        // -- notice that the third param gets passed to this method
        @Override
        protected void onPostExecute( String result ) {
            super.onPostExecute(result);
            //Log.i( "makemachine", "onPostExecute(): " + result );
        }
    }
}