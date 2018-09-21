package com.yuan.camera.simple;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.yuan.camera.R;
import com.yuan.camera.surface.CameraView;

/**
 * 使用SurfaceView来显示Camera的画面
 */
public class SurfaceViewCamera extends AppCompatActivity implements View.OnClickListener {

    private CameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view_camera);
        cameraView = findViewById(R.id.cameraView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                break;
        }
    }
}
