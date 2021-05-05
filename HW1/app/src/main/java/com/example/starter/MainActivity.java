package com.example.starter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MyActivity";
    private TextView textBottom;
    private GraphView graph;
    private RecorderStereo recThread;
    private int bufferCount;
    private int [] recData;
    private int fullCount;
    private Thread thAudioGl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bufferCount = 0;
        recData = new int [4800];
        Arrays.fill(recData, 0);
        textBottom = findViewById(R.id.textMsg1);
        graph = findViewById(R.id.graph);
        fullCount = 0;
    }

    public void createTone(View view){
        TextView text_top = (TextView)findViewById(R.id.introText);
        text_top.setText("Assignment 1 GO");
        int bufferSize = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioSpeaker thAudio = new AudioSpeaker(this, 48000, AudioFormat.CHANNEL_OUT_MONO, new AudioSpeaker.SampleGenerator(){
            @Override
            public short[] generate(){
                final int sampleRate = 48000; // sampling frequency
//
                final int note = 18000;
                short [] buffer = new short[bufferSize];
                for(int i=0; i<bufferSize; i++){
                    buffer[i] = (short) (Math.sin(2*Math.PI*note/sampleRate*i)*Short.MAX_VALUE);
                }
                return buffer;
            }

        });
        thAudio.start();


//        Runnable myRunnable = () -> {
//            Process.setThreadPriority(0);
//            final int duration = 8; // duration of sound
//            final int sampleRate = 48000; // sampling frequency
//            final int numSamples = duration * sampleRate;
//            final double[] samples = new double[numSamples];
////        final short[] buffer = new short[numSamples];
//            final int note = 9000;
//            final byte generatedSnd[] = new byte[2 * numSamples];
//            for (int i = 0; i < numSamples; ++i)
//            {
//                samples[i] =  Math.sin(2 * Math.PI * i / (sampleRate / note)); // Sine wave
////            buffer[i] = (short) (samples[i] * Short.MAX_VALUE);  // Higher amplitude increases volume
//            }
//            int idx = 0;
//            for (final double dVal : samples) {
//                // scale to maximum amplitude
//                final short val = (short) ((dVal * 32767));
//                // in 16 bit wav PCM, first byte is the low order byte
//                generatedSnd[idx++] = (byte) (val & 0x00ff);
//                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
//
//            }
//
//            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
//                    sampleRate, AudioFormat.CHANNEL_OUT_MONO,
//                    AudioFormat.ENCODING_PCM_16BIT, 2*generatedSnd.length,
//                    AudioTrack.MODE_STATIC);
//            audioTrack.write(generatedSnd, 0, generatedSnd.length);
//            audioTrack.play();
//            Log.v(TAG, "Check Play");
//
//        };
//        Thread thAudio = new Thread(myRunnable);
//        thAudio.start();

//        Log.v(TAG, "index=hello" );
//        double [] im = new double[2048];
//        FFT fft = new FFT(2048);
//        double [] small = Arrays.copyOfRange(samples,0,2048);
//        fft.fft(small, im);
//        double [] fftMag = new double[2048];
//        for (int i = 0; i < small.length; i++) {
//            fftMag[i] = Math.pow(small[i], 2) + Math.pow(im[i], 2);
//        }
//        Log.v(TAG, "index=hello" );
//        GraphView graph = (GraphView) findViewById(R.id.graph);
//        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
//        double maxFFT = Arrays.stream(fftMag).max().getAsDouble();
//        List <Double> place = Arrays.stream(fftMag).boxed().collect(Collectors.toList());
//        int index_maxNumber = place.indexOf(Collections.max(place));
////        int index = Arrays.asList(fftMag).indexOf(maxFFT);
//        for (int i = 0; i<2048; i++) {series.appendData(new DataPoint(i, fftMag[i]/maxFFT), true, 2048);
//            Log.v(TAG, "index " + i +" hello" + fftMag[i]);}
//        Log.v(TAG, "index See " + index_maxNumber);
//        graph.addSeries(series);


    }

//          Ideal Values find for audio, run the following code
////        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
////        String rate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
////        String size = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
////        Log.v(TAG, "Size :" + size + " & Rate: " + rate);

    public void recStart(View view) throws InterruptedException{
        int sampleWindow = 4800;
//        int power2 = 8192;
        Log.v(TAG, "Yo Yo1");
        RecorderStereo th = new RecorderStereo(48000, 256, new RecorderStereo.Callback(){
            @Override
            public void call(short[] data){
                // Done for dupData when filled can then be processed for FFT.

                double [] dupData = new double [2 * sampleWindow];
                Arrays.fill(dupData, 0);
                bufferCount += 128;
//                Log.v(TAG, "Yo Yo new buffer"+bufferCount);
//                for(int i=0; i<30; i++)  Log.v(TAG, "Yo Yo data"+ data[i]+ "m"+i);
                for(int i = bufferCount-128; i < bufferCount && i < sampleWindow; i++) {
//                    Log.v(TAG, "Yo Yo Loop"+i);
                    recData[i] = data[i-bufferCount+128];
//                    Log.v(TAG, "Yo Yo end copy"+ data[127]);
                }
//                Log.v(TAG, "Yo Yo end copy"+ data[127]);
                if (bufferCount > sampleWindow) {
//                    Log.v(TAG, "Yo Yo bufferCount > sampleWindow check");
                    bufferCount -= sampleWindow;
                    for(int i=0; i<sampleWindow; i++) {
                        dupData[i] = (double) recData[i]/Short.MAX_VALUE;
//                        Log.v(TAG, "Yo Yo Next Rdata: "+ dupData[i]);
                    }
                    Arrays.fill(recData, 0);
                    for(int i = bufferCount; i < 128; i++) recData[i-bufferCount] = data[i];
//                    for(int i = bufferCount; i < 128; i++) recData[i-bufferCount] = data[i];
//                    Log.v(TAG, "Yo Yo Next Check");
                    // Taking FFT and updating chart
                    final double [] fDupData = dupData;

//                    FFT fft = new FFT(power2);
                    DoubleFFT_1D fft = new DoubleFFT_1D(sampleWindow);
//                    double [] im = new double[sampleWindow];

                    fft.realForwardFull(dupData);
                    double [] fftMag = new double[sampleWindow];
//                    Log.v(TAG, "Yo Yo Next Check2");
                    for (int i = 0; i < sampleWindow; i++) {
                        fftMag[i] = Math.pow(dupData[i], 2);
//                        Log.v(TAG, "Yo Yo Next Rdata: "+ dupData[i]);
                    }
//                    Log.v(TAG, "Yo Yo Next Check3");
//                    fftMag[0] = Math.pow(fDupData[0], 2);
//                    fftMag[1] = Math.pow(fDupData[sampleWindow/2], 2);
//                    Log.v(TAG, "Yo Yo end copy 1:"+ fDupData[1] + " | n/2: " + Math.pow(dupData[1], 2));
                    double maxFFT = Arrays.stream(fftMag).max().getAsDouble();
                    fullCount +=1;
                    if (fullCount==10){
                        fullCount = 0;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                            Log.d(TAG, "Yo Yo thread");
                                textBottom.setText("Audio Processing");
//                                if (thGlAudio != null && thGlAudio.isAlive()) thGlAudio.resume();
//                            Log.v(TAG, "index=hello" );
                                LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
//        int index = Arrays.asList(fftMag).indexOf(maxFFT); /Math.log10(maxFFT)
                                for (int i = 1734; i<1866; i++) {series.appendData(new DataPoint(i, 20*Math.log10(fftMag[i])), true, 1024);
//                                Log.v(TAG, "index " + i +" hello" + fftMag[i]);

                                }
//                            Log.v(TAG, "index See " + index_maxNumber);
                                graph.removeAllSeries();
                                graph.addSeries(series);
//                                graph.getViewport().setMinX(1024);
//                                graph.getViewport().setMaxX(2400);
//                                graph.getViewport().setXAxisBoundsManual(true);
//                                graph.getViewport().setMinY(0);
//                                graph.getViewport().setMaxY(2);
//                                graph.getViewport().setYAxisBoundsManual(true);


                            }
                        });
                    }



                }
//                else {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.i(TAG, "Yo Yo thread");
//                            textBottom.setText("Audio Processing");
//
//
//                        }
//                    });
//                }
            }

        });
        th.start();
        recThread = th;
//        Thread.sleep(4000);
//        th.recording = false;
//        Log.v(TAG, "Yo Yo1"+ th.isAlive());
    }
    public void recStop(View view) throws InterruptedException{
        recThread.recording = false;
        Log.v(TAG, "close"+ recThread.isAlive());


    }
}