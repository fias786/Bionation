package com.persistent.bionation.ui.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.CameraController;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ComponentActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.persistent.bionation.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {

    private static final String TAG = "CameraFragment";

    PreviewView previewView;
    private BottomNavigationView bottomNavigationView;
    ImageAnalysis imageAnalysis;
    ImageClassifier imageClassifier;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private SortedMap<Float,Map<String,Object>> results;
    private OverlayView overlayView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        results = new TreeMap<>();
        //overlayView = new OverlayView(getContext());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_camera, container, false);
        overlayView = root.findViewById(R.id.CameraOverlay);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
        ViewGroup.LayoutParams layoutParams = bottomNavigationView.getLayoutParams();
        layoutParams.height = 0;
        bottomNavigationView.setLayoutParams(layoutParams);
        previewView = root.findViewById(R.id.CameraPreview);
        getPermission();
        startCamera();

        try {
            AssetFileDescriptor model = requireActivity().getAssets().openFd("model.tflite");
            InputStream label = requireActivity().getAssets().open("labels.csv");
            imageClassifier = new ImageClassifier(model,label,"1.0");
        } catch (IOException e) {
            e.printStackTrace();
        }


        return root;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraProvider.unbindAll();
        cameraProvider.shutdown();
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int bottomNavHeightPixels = (int) (50 * scale + 0.5f);
        bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
        ViewGroup.LayoutParams layoutParams = bottomNavigationView.getLayoutParams();
        layoutParams.height = bottomNavHeightPixels;
        bottomNavigationView.setLayoutParams(layoutParams);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getActivity().getWindow().setNavigationBarColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionGranted = true;
        for (int grantResult : grantResults) {
            if(grantResult != PackageManager.PERMISSION_GRANTED){
                getPermission();
                permissionGranted = false;
            }
        }
        if(!permissionGranted){
            Toast.makeText(getContext(),"Permission request denied",Toast.LENGTH_SHORT).show();
        }else{
            startCamera();
        }
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .build();

        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), new ImageAnalysis.Analyzer() {
            @SuppressLint("UnsafeOptInUsageError")
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                if (imageClassifier != null) {
                    Bitmap bmp = BitmapUtils.getBitmap(imageProxy);
                    // Crop the center square of the frame
                    int minDim = Math.min(bmp.getWidth(), bmp.getHeight());
                    int cropX = (bmp.getWidth() - minDim) / 2;
                    int cropY = (bmp.getHeight() - minDim) / 2;
                    Bitmap croppedBitmap = Bitmap.createBitmap(bmp, cropX, cropY, minDim, minDim);

                    // Resize to expected classifier input size
                    Bitmap rescaledBitmap = Bitmap.createScaledBitmap(
                            croppedBitmap,
                            ImageClassifier.DIM_IMG_SIZE_X,
                            ImageClassifier.DIM_IMG_SIZE_Y,
                            false);
                    bmp.recycle();
                    bmp = rescaledBitmap;
                    List<Prediction> predictions = imageClassifier.classifyFrame(bmp);
                    for (Prediction prediction : predictions) {
                        if (prediction.rank % 10 != 0) {
                            continue;
                        }
                        if (prediction.probability > 0.7) {
                            Map<String, Object> map = Taxonomy.predictionToMap(prediction);
                            if (map == null) continue;
                            results.put(prediction.node.rank, map);
                            if (prediction.node.rank == 10.0) {
                                overlayView.setResults(prediction);
                            } else{
                                overlayView.setResults(null);
                                overlayView.clear();
                            }
                        }
                        Log.d(TAG, "Object Detection: " + results.toString());
                    }
                }
                imageProxy.close();
            }
        });


        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageAnalysis);
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {

            }
        }, ContextCompat.getMainExecutor(getContext()));

    }


    private void getPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 101);
    }
}