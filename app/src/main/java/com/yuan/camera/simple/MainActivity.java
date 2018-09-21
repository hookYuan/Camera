package com.yuan.camera.simple;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.yuan.camera.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera_surfaceView:
                startActivity(new Intent(this, SurfaceViewCamera.class));
                break;
            case R.id.btn_camera_glSurfaceView:

                break;
            case R.id.btn_uvcCamera_glSurfaceView:

                break;

        }
    }
}
