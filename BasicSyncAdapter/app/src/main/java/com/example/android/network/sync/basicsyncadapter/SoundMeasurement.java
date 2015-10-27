package com.example.android.network.sync.basicsyncadapter;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.example.android.network.sync.basicsyncadapter.calc.Complex;
import com.example.android.network.sync.basicsyncadapter.calc.FFT;

/**
 * Created by jbgueusquin on 26/10/2015.
 */
public class SoundMeasurement {

    // For sound :
    private int sampleRateInHz = 44100;
    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
    private int bufferSize = Math.max(minBufferSize, Math.round(0.125f * sampleRateInHz));
    private int nSoundLoops = 50;

    // Sound measurement
    public int measureSound(){
        double dB_average = 0;
        int iLoop = 0;
        short[] buffer = new short[bufferSize];

        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, channelConfig, audioFormat, bufferSize);
        recorder.startRecording();


        while(iLoop<nSoundLoops) {

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

            energy = energy/N;
            double meanEnergy = energy*fMin;
            if(meanEnergy == 0.0)
                meanEnergy = 1e-17;

            double dBA = 10.0*Math.log(meanEnergy)/Math.log(10.0)+36.0;
            dB_average = (iLoop*dB_average+dBA)/(iLoop+1);
            iLoop++;
        }

        recorder.stop();
        recorder.release();
        recorder = null;

        return (Math.round((float)dB_average));
    }


}
