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
public class GLView extends GLSurfaceView {

    private static final String TAG = "GLCameraView";


    public GLView(Context context) {
        super(context);
        init(0);
    }

    public GLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GLView);
        int mirror = ta.getInteger(R.styleable.GLView_gl_mirror, 0);
        ta.recycle();
        init(mirror);
    }

    /**
     * 初始化
     *
     * @param mirror 是否启用画面镜像
     */
    private void init(int mirror) {
        setEGLContextClientVersion(2);
        setRenderer(new GLRenderer(3));
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
}
