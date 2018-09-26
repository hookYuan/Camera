package com.yuan.camera.glsurface;

import android.content.Context;
import android.content.res.TypedArray;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.yuan.camera.R;
import com.yuan.camera.common.CameraUtil;

import java.util.HashMap;

/**
 * Created by YuanYe on 2018/8/24.
 * 使用GLSurfaceView来预览Camera界面
 */
public class GLView extends GLSurfaceView {

    private static final String TAG = "GLView";

    private boolean isInvalidate = false;//是否刷新
    private OnMeasureListener onMeasureListener;

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
        setRenderer(new GLRenderer(mirror));
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isInvalidate && onMeasureListener != null) {
            int childWidthSize = getMeasuredWidth();
            int childHeightSize = getMeasuredHeight();
            onMeasureListener.onMeasure(childWidthSize, childHeightSize);
            isInvalidate = false;
        }
    }

    /**
     * 设置GLView自适应宽高
     */
    public void setAdapterSize(OnMeasureListener onMeasureListener) {
        isInvalidate = true;
        this.onMeasureListener = onMeasureListener;
        //刷新宽高
        requestLayout();
        invalidate();
    }

    /**
     * 用于监听View的初始宽高，根据实际相机宽高比动态计算显示宽高
     * 保证Camera显示不发生形变
     */
    public interface OnMeasureListener {
        void onMeasure(int width, int height);
    }
}
