package com.yuan.camera.simple;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.yuan.camera.R;
import com.yuan.camera.surface.CameraView;

/**
 * 使用SurfaceView来显示Camera的画面
 */
public class SurfaceViewCamera extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view_camera);
    }

}
