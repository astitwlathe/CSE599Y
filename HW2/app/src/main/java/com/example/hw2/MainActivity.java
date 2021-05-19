package com.example.hw2;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "MyActivity";
    private static final String TAG2 = "NewAc";
    private Sensor gyroscopeSensor;
    private Sensor accelerometerSensor;
    private SensorManager sensorManager;
    private double [] gBias;
    private double [] gNoise;
    private double [] aBias;
    private double [] aNoise;
    private int nSamples;
    private boolean gCalibrate = false;
    private boolean aCalibrate = false;
    private ArrayList<Float> [] gSamples;
    private ArrayList<Float> [] aSamples;
    private GraphView graph;
    private boolean plot;
    private LineGraphSeries<DataPoint> series;
    private long millis;
    private int count;
//    private int update;
    private Quaternion qt;
    private long time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        gSamples = (ArrayList<Float> []) new ArrayList[3];
        aSamples = (ArrayList<Float> []) new ArrayList[3];
        for(int i=0; i<3; i++){
            gSamples[i] = new ArrayList<Float>();
            aSamples[i] = new ArrayList<Float>();
        }
        gBias = new double[3];
        aBias = new double[3];
        gNoise = new double[3];
        aNoise = new double[3];
        graph = findViewById(R.id.graph);
        nSamples=-2;

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//        Log.v(TAG, "Size :" + size + " & Rate: " + rate);
//        Log.v(TAG, "Check"+gSamples[0].size()+" ui "+ nSamples);
        if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
            if (!gCalibrate) {
                if (gSamples[0].size() < nSamples) {
                    for (int i = 0; i < 3; i++) gSamples[i].add(event.values[i]);

                } else {
                    gCalibrate = true;
                    for (int i = 0; i < 3; i++) {
                        gBias[i] = 0;
                        gNoise[i] = 0;
                    }
                    for (int i = 0; i < nSamples; i++) {
                        gBias[0] += gSamples[0].get(i);
                        gBias[1] += gSamples[1].get(i);
                        gBias[2] += gSamples[2].get(i);
                    }
                    for (int i = 0; i < 3; i++) gBias[i] /= nSamples;
                    for (int i = 0; i < nSamples; i++) {
                        gNoise[0] += Math.pow(gSamples[0].get(i) - gBias[0], 2);
                        gNoise[1] += Math.pow(gSamples[1].get(i) - gBias[1], 2);
                        gNoise[2] += Math.pow(gSamples[2].get(i) - gBias[2], 2);
                    }
                    for(int i=0; i<3; i++)aNoise[i] /= nSamples;
                    for(int i=0; i<3; i++)gNoise[i] = Math.sqrt(gNoise[i]);
                }

            }

        }
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER_UNCALIBRATED){

            if(!aCalibrate) {
                if(aSamples[0].size()<nSamples) {
                    for (int i = 0; i < 3; i++) aSamples[i].add(event.values[i]);

                }
                else{
                    aCalibrate = true;
                    for(int i=0; i<3; i++) {aBias[i] = 0; aNoise[i] = 0;}
                    for(int i=0; i<nSamples; i++){
                        aBias[0] += aSamples[0].get(i);
                        aBias[1] += aSamples[1].get(i);
                        aBias[2] += aSamples[2].get(i);
                    }
                    for(int i=0; i<3; i++)aBias[i] /= nSamples;
                    for(int i=0; i<nSamples; i++){
                        aNoise[0] += Math.pow(aSamples[0].get(i) - aBias[0], 2);
                        aNoise[1] += Math.pow(aSamples[1].get(i) - aBias[1], 2);
                        aNoise[2] += Math.pow(aSamples[2].get(i) - aBias[2], 2);
                    }
                    for(int i=0; i<3; i++)aNoise[i] /= nSamples;
                    for(int i=0; i<3; i++)aNoise[i] = Math.sqrt(aNoise[i]);
                }
            }
        }

        if(aCalibrate && gCalibrate && nSamples>0){
            nSamples = -1;
            TextView resultText = (TextView)findViewById(R.id.result);
            resultText.setText("Result - Gyro (rad/s): Bias "+String.format("%.3f", gBias[0])+","+String.format("%.3f", gBias[1])+","+String.format("%.3f", gBias[2])+" | Noise (rad/s): "+String.format("%.3f", gNoise[0])+","+String.format("%.3f", gNoise[1])+","+String.format("%.3f", gNoise[2])+"\n"+
                                         "Accel (m/s^2):Bias "+String.format("%.3f", aBias[0])+","+String.format("%.3f", aBias[1])+","+String.format("%.3f", aBias[2])+" | Noise (m/s^2): "+String.format("%.3f", aNoise[0])+","+String.format("%.3f", aNoise[1])+","+String.format("%.3f", aNoise[2]));

        }
        RadioButton rbA = (RadioButton) findViewById(R.id.rButtonA);
        RadioButton rbG = (RadioButton) findViewById(R.id.rButtonG);
        RadioButton rbBAG = (RadioButton) findViewById(R.id.rButtonBAG);
        Log.d(TAG2, "Radio "+rbA.isChecked()+ " t: "+(System.currentTimeMillis()-millis)/1000.0);
        if(rbA.isChecked() && plot && (System.currentTimeMillis()-millis)<300000) {

            if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER_UNCALIBRATED) {
                double theta = Math.acos(event.values[2]/Math.sqrt(Math.pow(event.values[0], 2)+Math.pow(event.values[1], 2)+Math.pow(event.values[2], 2)));
                series.appendData(new DataPoint(count, theta), true, 1024);
                count++;

                if(count%100==0){

                    series = new LineGraphSeries<DataPoint>();
                    graph.removeAllSeries();
                    graph.addSeries(series);

                }
                Log.v(TAG, "Check"+theta);
                }

        }
        if(rbG.isChecked() && plot && (System.currentTimeMillis()-millis)<300000) {
            if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
                double [] angularVelocity = {event.values[0], event.values[1], event.values[2]};
                double angularMagnitude = Math.sqrt(Math.pow(angularVelocity[0], 2)+ Math.pow(angularVelocity[1], 2)+Math.pow(angularVelocity[2], 2));
                double [] axis = {angularVelocity[0]/angularMagnitude, angularVelocity[1]/angularMagnitude, angularVelocity[2]/angularMagnitude};
                double theta = angularMagnitude*(System.currentTimeMillis()-time)/1000;
                time = System.currentTimeMillis();

                Quaternion dq = new Quaternion(Math.cos(theta/2), axis[0]*Math.sin(theta/2), axis[1]*Math.sin(theta/2), axis[2]*Math.sin(theta/2));
                qt = qt.times(dq);
                theta = 2*Math.acos(qt.x0);
                series.appendData(new DataPoint(count, theta), true, 1024);
                count++;

                if(count%100==0){

                    series = new LineGraphSeries<DataPoint>();
                    graph.removeAllSeries();
                    graph.addSeries(series);

                }
                Log.v(TAG, "Check"+theta);


            }
        }

        if(rbBAG.isChecked() && plot && (System.currentTimeMillis()-millis)<300000) {
            if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
                double [] angularVelocity = {event.values[0]-gBias[0], event.values[1]-gBias[1], event.values[2]-gBias[2]};
                double angularMagnitude = Math.sqrt(Math.pow(angularVelocity[0], 2)+ Math.pow(angularVelocity[1], 2)+Math.pow(angularVelocity[2], 2));
                double [] axis = {angularVelocity[0]/angularMagnitude, angularVelocity[1]/angularMagnitude, angularVelocity[2]/angularMagnitude};
                double theta = angularMagnitude*(System.currentTimeMillis()-time)/1000;
                time = System.currentTimeMillis();

                Quaternion dq = new Quaternion(Math.cos(theta/2), axis[0]*Math.sin(theta/2), axis[1]*Math.sin(theta/2), axis[2]*Math.sin(theta/2));
                qt = qt.times(dq);
                theta = 2*Math.acos(qt.x0);

            }
            if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER_UNCALIBRATED) {
                double alpha = .001;
                double qVectorMagnitude = Math.sqrt(Math.pow(event.values[0], 2)+Math.pow(event.values[1], 2)+Math.pow(event.values[2], 2));
                Quaternion qVector = new Quaternion(0, event.values[0]/qVectorMagnitude, event.values[1]/qVectorMagnitude, event.values[2]/qVectorMagnitude);
                Log.v(TAG, "Qt values:"+ qt.x0+ "|"+qt.x1+ "|"+qt.x2+"|"+qt.x3);
                Quaternion qVectorGlobal = qt.times(qVector.times(qt.inverse()));
                Log.v(TAG, "Qt Global values:"+ qVectorGlobal.x0+ "|"+qVectorGlobal.x1+ "|"+qVectorGlobal.x2+"|"+qVectorGlobal.x3);
                double angle = Math.acos(qVectorGlobal.x3);

                double nVectorMagnitude = Math.sqrt(Math.pow(qVectorGlobal.x2, 2)+Math.pow(qVectorGlobal.x1, 2));
                double [] axis = {qVectorGlobal.x2/nVectorMagnitude, -qVectorGlobal.x1/nVectorMagnitude, 0};
                Log.v(TAG, "Execute: "+ nVectorMagnitude);
                Quaternion qTilt = new Quaternion(Math.cos((-alpha)*angle/2), axis[0]*Math.sin((-alpha)*angle/2), axis[1]*Math.sin((-alpha)*angle/2), axis[2]*Math.sin((-alpha)*angle/2));//new Quaternion(0, );

                qt = qTilt.times(qt);
                Log.v(TAG, "Execute: check Go"+qt.x0);
                double theta = 2*Math.acos(qt.x0);
                series.appendData(new DataPoint(count, theta), true, 1024);
                count++;
                Log.v(TAG, "Execute1: ");
                TextView resultText = (TextView)findViewById(R.id.result);
                resultText.setText("Tilt(radians) = "+ (theta-3.14));
                if(count%100==0){

                    series = new LineGraphSeries<DataPoint>();
                    graph.removeAllSeries();
                    graph.addSeries(series);

                }
                Log.v(TAG, "CheckBAG "+theta);


            }


        }

    }

    public void onClickCalibrate (View view){
        TextView text_top = (TextView)findViewById(R.id.introText);
        text_top.setText("Assignment 2 GO");
        TextView resultText = (TextView)findViewById(R.id.result);
        resultText.setText("Processing...");
        nSamples = 2000;
        this.gCalibrate = false;
        this.aCalibrate = false;

        Log.v(TAG, "Size ");
        for(int i=0; i<3; i++){
            Log.v(TAG, "Go "+i);
            if(gSamples[i].size()>0) gSamples[i].clear();
            if(aSamples[i].size()>0) aSamples[i].clear();
        }

    }

    public void onClickPlot (View view){
        plot = true;
//        TextView resultText = (TextView)findViewById(R.id.result);
//        resultText.setText("Processing...");
        series = new LineGraphSeries<DataPoint>();
        graph.removeAllSeries();
        graph.addSeries(series);
        millis = System.currentTimeMillis();
        count = 0;
        Log.v(TAG,"Plot "+millis);

        RadioButton rbG = (RadioButton) findViewById(R.id.rButtonG);
        time = millis;
        qt = new Quaternion(1, 0, 0, 0);
//        graph.getViewport().setYAxisBoundsManual(true);
//        graph.getViewport().setMinY(0);
//        graph.getViewport().setMaxY(180);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}