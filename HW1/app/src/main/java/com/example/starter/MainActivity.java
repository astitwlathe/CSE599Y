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
    private boolean init;

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
                        dupData[i] = (double) recData[i]/Short.MAX_VALUE*(.53836-.46164*Math.cos(2*Math.PI*i/(sampleWindow-1)));
//                        Log.v(TAG, "Yo Yo Next Rdata: "+ dupData[i]);
                    }
                    int [] tmpRecdata = recData;
                    Arrays.fill(recData, 0);


                    System.arraycopy(tmpRecdata, sampleWindow/2, recData, 0, sampleWindow/2);

                    for(int i = bufferCount; i < 128; i++) recData[i+sampleWindow/2-bufferCount] = data[i];
                    bufferCount += sampleWindow/2;
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
                    int maxIndex = Arrays.asList(fftMag).indexOf(maxFFT);
                    maxFFT = Double.MIN_VALUE;
                    for(int i = 0; i< fftMag.length/2; i++){
                        if (maxFFT<fftMag[i]){
                            maxIndex = i;
                            maxFFT = fftMag[i];
                        }
                    }

                    boolean rightSide = false;
                    boolean leftSide = false;
//                    double relDrop = 0.16 * maxFFT;
                    double relDrop = 0.1 * maxFFT;
                    Log.v(TAG, "Yo Yo end copy"+leftSide+ "fft max Index"+maxIndex);
                    Log.v(TAG, "Yo Yo end copy"+leftSide+ "fft magnitude"+maxFFT);
                    for(int i=maxIndex+18; i<maxIndex+24 && i<fftMag.length-1; i++){
//                        Log.v(TAG, "Yo Yo in loop - fft value "+fftMag[i] + " | rel drop: "+ relDrop + " | index: " + i);
                        if((0*fftMag[i-1]+1*fftMag[i]+0*fftMag[i+1]+0*fftMag[i+2])/1>relDrop) {rightSide=true; break;}
                       else rightSide=false;
                    }
//                    Log.v(TAG, "Yo Yo Work");
//                    relDrop = 0.0301 * maxFFT;
                    relDrop = 0.1 * maxFFT;
                    for(int i=maxIndex-10; i>maxIndex-14 && i>=1; i--){
//                        Log.v(TAG, "Yo Yo in 2 loop"+i);
                        if((0*fftMag[i-1]+1*fftMag[i]+0*fftMag[i+1]+0*fftMag[i+2])/1>relDrop) {leftSide=true;break;}
                        else leftSide=false;
                    }
                    Log.v(TAG, "Yo Yo end copy"+leftSide+ "fft val"+maxIndex);
                    final boolean fRightSide = rightSide;
                    final boolean fLeftSide = leftSide;
//                    Log.v(TAG, "Yo Yo check");
                    fullCount +=1;
                    if (fullCount==1){
                        if(fRightSide || fLeftSide)
                        fullCount = -250;
                        else fullCount = 0;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

//                            Log.d(TAG, "Yo Yo thread");
//                            Log.v(TAG, "Yo Yo thread" + fLeftSide);
                                if(fLeftSide) textBottom.setText("Pull Gesture");
                                else if(fRightSide)  textBottom.setText("Push Gesture");
                                else textBottom.setText("Audio Processing");
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