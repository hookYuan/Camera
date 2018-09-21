package com.yuan.camera.glsurface;

import android.content.Context;
import android.content.res.TypedArray;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.yuan.camera.R;

/**
 * Created by YuanYe on 2018/8/24.
 * 使用GLSurfaceView来预览Camera界面
 */
public class CameraGLView extends GLSurfaceView {

    public CameraGLView(Context context) {
        super(context);
    }

    public CameraGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CameraGLView);
        boolean mirror = ta.getBoolean(R.styleable.CameraGLView_gl_mirror, false);
        ta.recycle();
        setEGLContextClientVersion(2);
        setRenderer(new CameraRenderer(mirror));
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
