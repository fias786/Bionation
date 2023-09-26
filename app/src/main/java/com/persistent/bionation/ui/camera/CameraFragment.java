package com.persistent.bionation.ui.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.view.CameraController;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.persistent.bionation.R;

public class CameraFragment extends Fragment {

    PreviewView previewView;
    private BottomNavigationView bottomNavigationView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_camera, container, false);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        bottomNavigationView = requireActivity().findViewById(R.id.nav_view);
        ViewGroup.LayoutParams layoutParams = bottomNavigationView.getLayoutParams();
        layoutParams.height = 0;
        bottomNavigationView.setLayoutParams(layoutParams);
        previewView = root.findViewById(R.id.CameraPreview);
        getPermission();
        startCamera();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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

    private void startCamera() {
        LifecycleCameraController cameraController = new LifecycleCameraController(getContext());
        cameraController.bindToLifecycle(this);
        cameraController.setCameraSelector(CameraSelector.DEFAULT_BACK_CAMERA);
        previewView.setController(cameraController);
    }

    private void getPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 101);
    }
}