package com.yuan.camera.simple;

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yuan.camera.R;
import com.yuan.camera.camera.CameraHelper;
import com.yuan.camera.common.CameraUtil;
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
        CameraHelper.getInstance().openCamera(0, 800);
        GLViewManager.getInstance().addPreview(glCamera);
        CameraHelper.getInstance().startPreview(TextureUtil.getInstance());
        glCamera.setAdapterSize(new GLView.OnMeasureListener() {
            @Override
            public void onMeasure(int width, int height) {
                //设置摄像头画布大小
                CameraUtil.getBaseWidthHeight(GLSurfaceCamera.this,
                        width, height);
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) glCamera.getLayoutParams();
                params.width = width;
                params.height = height;
            }
        });
    }


    @Override
    protected void onDestroy() {
        GLViewManager.getInstance().remove(glCamera);
        CameraHelper.getInstance().close();
        super.onDestroy();
    }

}
