package com.yuan.camera.simple;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yuan.camera.R;
import com.yuan.camera.camerauvc.UVCHelper;
import com.yuan.camera.common.CameraUtil;
import com.yuan.camera.glsurface.GLView;
import com.yuan.camera.glsurface.GLViewManager;
import com.yuan.camera.glsurface.TextureUtil;

/**
 * USB摄像头，需要在设备上连接USB摄像头生效
 */
public class UVCCameraActivity extends AppCompatActivity {

    private GLView glCamera;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glsurface_camera);
        glCamera = findViewById(R.id.glCameraView);

        GLViewManager.getInstance().addPreview(glCamera);
        //异步开启摄像头
        UVCHelper.getInstance().openCamera(this, 800, 600, new UVCHelper.OpenCameraCallBack() {
            @Override
            public void onOpen() {
                //摄像头开启完成回调
                UVCHelper.getInstance().startPreview(TextureUtil.getInstance());
            }
        });
    }

    @Override
    protected void onDestroy() {
        UVCHelper.getInstance().close();
        super.onDestroy();
    }
}
