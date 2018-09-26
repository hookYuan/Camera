package com.yuan.camera.simple;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yuan.camera.R;
import com.yuan.camera.camera.CameraHelper;
import com.yuan.camera.glsurface.GLView;
import com.yuan.camera.glsurface.GLViewManager;
import com.yuan.camera.glsurface.TextureUtil;

public class GLSurfaceCamera extends AppCompatActivity {

    private GLView glCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glsurface_camera);
        glCamera = findViewById(R.id.glCameraView);
        CameraHelper.getInstance().openCamera(800);
        GLViewManager.getInstance().addPreview(glCamera);
        CameraHelper.getInstance().startPreview(TextureUtil.getInstance());
    }


    @Override
    protected void onDestroy() {
        GLViewManager.getInstance().remove(glCamera);
        CameraHelper.getInstance().close();
        super.onDestroy();
    }
}
