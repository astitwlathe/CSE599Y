package com.example.receive;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;

import org.jtransforms.fft.DoubleFFT_1D;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MyActivity";
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private TextView textDisplay;
    private ExecutorService cameraExecutor;
    private double previousLuma = 0;
    private ArrayList<Double> windowTotal;
    private boolean start;
//    private ArrayList<Integer[]> totalFrames;
    private int count;
    private int windowCount;
    private ArrayList<Integer>  candidates;
    private DoubleFFT_1D fft;
    private double [] window;
    private String pattern;
    private ArrayList<Double>  bitNoise0;
    private ArrayList<Double>  bitNoise1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        previewView = findViewById(R.id.previewView);
        textDisplay = findViewById(R.id.textDisplay);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraExecutor = Executors.newSingleThreadExecutor();
        windowTotal = new ArrayList<Double>();
        bitNoise0 = new ArrayList<Double>();
        bitNoise1 = new ArrayList<Double>();
//        totalFrames = new ArrayList<Integer[]>();
        fft = new DoubleFFT_1D(6);
        candidates = new ArrayList<Integer>();
        window = new double[12];
        pattern = "";


        count = -1;
        start = false;
        windowCount = 0;
        cameraProviderFuture.addListener(() -> {
            try {
                // Camera provider is now guaranteed to be available
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();
                // Set up the view finder use case to display camera preview
                Preview preview = new Preview.Builder().build();

                // Set up the capture use case to allow users to take photos
//                imageCapture = new ImageCapture.Builder()
//                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//                        .build();

                // Choose the camera by requiring a lens facing
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
//                                .setTargetResolution(new Size(1280, 720))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(cameraExecutor, new LuminosityAnalyzer());

                // Attach use cases to the camera with the same lifecycle owner
                Camera camera = cameraProvider.bindToLifecycle(
                        ((LifecycleOwner) this), cameraSelector, imageAnalysis, preview);

                // Connect the preview use case to the previewView
                preview.setSurfaceProvider(
                        previewView.getSurfaceProvider());
            } catch (InterruptedException | ExecutionException e) {
                // Currently no exceptions thrown. cameraProviderFuture.get()
                // shouldn't block since the listener is being called, so no need to
                // handle InterruptedException.
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

    }
    


    public class LuminosityAnalyzer implements ImageAnalysis.Analyzer {
        private byte[] byteBufferToByteArray(ByteBuffer buffer) {
            buffer.rewind();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            return data;
        }
        @Override
        public void analyze(ImageProxy image) {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] data = byteBufferToByteArray(buffer);
            Integer[] pixels = new Integer[data.length];
            int pos = 0;
            for (byte b : data) {
                pixels[pos] = b & 0xFF;
                pos++;
            }
            // Compute average luminance for the image
            double luma = Stream.of(pixels).mapToDouble(a -> a).average().orElse(Double.NaN);
//            totalFrames.add(pixels);
            if(count>=0){
                windowTotal.add(luma-previousLuma);
            }

            if (count>5){
//                Log.v(TAG, "Yo before");
//                window = windowTotal.subList(count-6, count).stream().mapToDouble(a -> a).toArray();
                System.arraycopy(windowTotal.subList(count-6, count).stream().mapToDouble(a -> a).toArray(), 0,  window, 0, 6);
//                Log.v(TAG, "Yo after");

//                Log.v(TAG, "Yo aftercheck"+ Arrays.toString(window));
                fft.realForwardFull(window);
//                Log.v(TAG, "Yo after3");
                if (Math.pow(window[2], 0) > Math.pow(window[3], 0)) {candidates.add(0); bitNoise0.add(window[2]);}
                else {candidates.add(1); bitNoise1.add(window[3]);}
//                Log.v(TAG, "Yo after2");
                if(count%6==0 && count>6) {
                if (candidates.stream().filter(value -> value==0).count()>2) pattern += 0;
                else pattern += 1;
                }
                Log.v(TAG, "Window 2:"+ window[2] + " | Window 3: " + window[3]);
            }

            previousLuma = luma;



//            textDisplay.setText("Luminosity: " + luma + " | pattern: " + pattern);
//            Button btnText = (Button) findViewById(R.id.camera_capture_button);
//            if(count%6)


            Log.v(TAG, "Image:"+ luma + " | Value: "+ System.currentTimeMillis() + " | pattern: " + pattern);
            count++;
            image.close();

        }

    }
}