package com.yuan.camera.glsurface;

import android.graphics.SurfaceTexture;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YuanYe on 2018/8/27.
 * 管理摄像头数据绘制
 * <p>
 * 通过GLViewManager可以实现将一个摄像头等的数据同时显示到多个GLView之上
 * GLViewManager需要配合数据源使用
 */
public class GLViewManager implements SurfaceTexture.OnFrameAvailableListener {

    private List<GLView> glViews; //需要显示摄像头数据的数据集合

    private static GLViewManager manager;

    public static GLViewManager getInstance() {
        if (manager == null) manager = new GLViewManager();
        return manager;
    }

    private GLViewManager() {
        glViews = new ArrayList<>();
        TextureUtil.getInstance().setOnFrameAvailableListener(this);
    }

    /**
     * 将预览画面View添加到显示队列中
     *
     * @param glView
     */
    public void addPreview(GLView glView) {
        if (!glViews.contains(glView)) glViews.add(glView);
    }

    /**
     * 移除需要显示Camera数据的画布
     *
     * @param glView 可以是Activity、dialog
     */
    public void remove(GLView glView) {
        if (glViews.contains(glView)) {
            glViews.remove(glView);
        }
    }

    public void onResume() {
        for (GLView glView : glViews) {
            glView.onResume();
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        for (GLView glView : glViews) {
            glView.requestRender();
        }
    }
}
