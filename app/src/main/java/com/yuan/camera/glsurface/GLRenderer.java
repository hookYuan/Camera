package com.yuan.camera.glsurface;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by YuanYe on 2018/8/24.
 * Camera渲染器
 */
public class GLRenderer implements GLSurfaceView.Renderer {

    private int mTextureID = -1; //对应纹理Id
    private DirectDrawer mDirectDrawer;
    private int mirror = 0;//是否对视频进行镜像处理

    public GLRenderer() {

    }

    public GLRenderer(int mirror) {
        this.mirror = mirror;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mTextureID = createTextureID();
        mDirectDrawer = new DirectDrawer(mirror, mTextureID);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        TextureUtil.draw(mDirectDrawer, mTextureID);
    }

    public int createTextureID() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

}
