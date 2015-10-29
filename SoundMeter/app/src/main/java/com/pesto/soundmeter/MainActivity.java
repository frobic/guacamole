package com.pesto.soundmeter;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class MainActivity extends Activity {

    private Button mStart = null;
    private Button mStop = null;
    private TextView dBView = null;

    private RecordTask mProgress = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStart = (Button) findViewById(R.id.start);
        mStop = (Button) findViewById(R.id.stop);
        dBView = (TextView) findViewById(R.id.dBTag);
        mStop.setEnabled(false);

        mStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Début de la mesure !", Toast.LENGTH_LONG).show();
                mStop.setEnabled(true);
                mStart.setEnabled(false);

                mProgress = new RecordTask(MainActivity.this);
                mProgress.execute();
            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mProgress.cancel(true);

                mStop.setEnabled(false);
                mStart.setEnabled(true);
            }
        });

        mProgress = (RecordTask) getLastNonConfigurationInstance();
        if(mProgress != null)
            mProgress.link(this);
    }

    @Override
    public Object onRetainNonConfigurationInstance () {
        return mProgress;
    }

    // Update of dB
    void updateProgress(int amp) {
        dBView.setText(amp + " (Brut)");
    }

    // L'AsyncTask est une classe interne statique
    static class RecordTask extends AsyncTask<Void, Integer, Boolean> {
        // Référence faible à l'activité
        private WeakReference<MainActivity> mActivity = null;

        private int sampleRateInHz = 44100;
        private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        private int minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        private int bufferSize = Math.max(minBufferSize, Math.round(1.f*sampleRateInHz));
        private short[] buffer = new short[bufferSize];

        private AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, channelConfig, audioFormat, bufferSize);

        public RecordTask (MainActivity pActivity) {
            link(pActivity);
        }

        public void link (MainActivity pActivity) {
            mActivity = new WeakReference<MainActivity>(pActivity);
        }

        @Override
        protected void onPreExecute () {
            recorder.startRecording();
        }

        @Override
        protected void onPostExecute (Boolean result) {
            recorder.stop();
        }

        @Override
        protected Boolean doInBackground (Void... arg0) {
            try {

                while(!isCancelled()) {

                    // Définir un buffer size pour 0.125s
                    int nbSamples = recorder.read(buffer, 0, bufferSize);

                    int N = 1;
                    while(nbSamples >= N*2) {
                        N = N*2;
                    }

                    Complex[] window = new Complex[N];
                    for(int i = 0; i < N; i++)
                        window[i] = new Complex((double)buffer[i],0.0);

                    Complex[] spectrum = FFT.fft(window);

                    double c1 = 3.5041384e16;
                    double c2 = 20.598997*20.598997;
                    double c3 = 107.65265*107.65265;
                    double c4 = 737.86223*737.86223;
                    double c5 = 12194.217*12194.217;

                    double fMin = (double)sampleRateInHz/(double)N;

                    double energy = 0.0;
                    for(int i = 0; i < N/2; i++){
                        double amplitude = (spectrum[i]).abs();
                        double freq = fMin*i;

                        freq = freq*freq;
                        double num = c1*freq*freq*freq*freq;
                        double den = (c2+freq)*(c2+freq)*(c3+freq)*(c4+freq)*(c5+freq)*(c5+freq);
                        double A = num/den;

                        energy += A*A*amplitude*amplitude;
                    }

                    energy = energy/N; // Cas 0 ???
                    double meanEnergy = energy*fMin;
                    double dBA = 10.0*Math.log(meanEnergy)/Math.log(10.0)-29.0;
                    int dBA_int = Math.round((float)dBA);
                    publishProgress(dBA_int);
                }


                return true;
            }catch(Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate (Integer... prog) {
            // On met à jour le TextView
            if (mActivity.get() != null)
                mActivity.get().updateProgress(prog[0]);
        }

        @Override
        protected void onCancelled (Boolean result) {
            recorder.stop();

            if(mActivity.get() != null)
                Toast.makeText(mActivity.get(), "Fin de la mesure !", Toast.LENGTH_SHORT).show();
        }
    }
}
