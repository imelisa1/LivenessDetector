package com.example.livenessdetector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LivenessDetector {
    private Context context;
    private PreviewView previewView;
    private Interpreter tflite;
    private ExecutorService executorService;
    private ProcessCameraProvider cameraProvider;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageAnalysis imageAnalysis;
    private boolean isLive = false;

    public LivenessDetector(Context context) {
        this.context = context;
    }

    @SuppressLint("MissingInflatedId")
    public boolean initialize() {
        previewView = ((Activity) context).findViewById(R.id.previewView);

        executorService = Executors.newSingleThreadExecutor();

        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        startCamera(); // Kamerayı başlatıyoruz

        return isLive;
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(context.getAssets().openFd("model.tflite").getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = context.getAssets().openFd("model.tflite").getStartOffset();
        long declaredLength = context.getAssets().openFd("model.tflite").getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void captureImage() {
        if (imageAnalysis != null) {
            imageAnalysis.setAnalyzer(executorService, new ImageAnalysis.Analyzer() {
                @OptIn(markerClass = ExperimentalGetImage.class)
                @Override
                public void analyze(@NonNull ImageProxy image) {
                    if (image.getImage() != null) {
                        float[][][][] input = convertImageToInputBuffer(image);
                        float[] output1 = new float[1];
                        float[][][][] output2 = new float[1][1][14][14];

                        Map<Integer, Object> outputMap = new HashMap<>();
                        outputMap.put(0, output1);
                        outputMap.put(1, output2);

                        tflite.runForMultipleInputsOutputs(new Object[]{input}, outputMap);

                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                float threshold = 0.97f;
                                if (output1[0] > threshold) {
                                    isLive = false; // Sahte (Fake)
                                } else {
                                    isLive = true;  // Gerçek (Real)
                                }
                                Log.d("LivenessDetection", "Liveness result: " + isLive + " (Score: " + output1[0] + ")");

                                // Analiz bittiği anda kamerayı kapat ve serbest bırak
                                stopCamera();
                            }
                        });

                        image.close();
                    }
                }
            });
        }
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                // Eski kamera oturumlarını kapat
                if (cameraProvider != null) {
                    cameraProvider.unbindAll();
                }

                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void stopCamera() {
        // Kamerayı serbest bırak ve durdur
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        executorService.shutdown();
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(224, 224))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        // Kamera oturumunu canlılık analizi için başlatıyoruz
        cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview, imageAnalysis);

        // Tek seferlik analiz için captureImage() fonksiyonunu hemen çağırıyoruz
        captureImage();
    }

    private float[][][][] convertImageToInputBuffer(ImageProxy image) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        float[][][][] inputBuffer = new float[1][3][224][224]; // 1x3x224x224 input size
        int[] rgbBytes = new int[imageWidth * imageHeight];
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ImageUtils.convertYUV420ToARGB8888(
                planes[0].getBuffer(),
                planes[1].getBuffer(),
                planes[2].getBuffer(),
                imageWidth,
                imageHeight,
                planes[0].getRowStride(),
                planes[1].getRowStride(),
                planes[1].getPixelStride(),
                rgbBytes);

        int pixelIndex = 0;
        for (int i = 0; i < 224; ++i) {
            for (int j = 0; j < 224; ++j) {
                int pixel = rgbBytes[pixelIndex++];
                inputBuffer[0][0][i][j] = ((pixel >> 16) & 0xFF) / 255.0f;  // R
                inputBuffer[0][1][i][j] = ((pixel >> 8) & 0xFF) / 255.0f;   // G
                inputBuffer[0][2][i][j] = (pixel & 0xFF) / 255.0f;          // B
            }
        }

        return inputBuffer;
    }
}
