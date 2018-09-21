package com.yuan.camera.glsurface;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by lixun on 16/9/25.
 * 创建一个用来最后显示的SurfaceTexture来显示处理后的数据
 */
public class TextureUtil {

    private static final String TAG = "TextureUtil";
    private static SurfaceTexture mSurfaceTexture;

    public static SurfaceTexture getInstance(int id) {
        if (mSurfaceTexture == null) {
            mSurfaceTexture = new SurfaceTexture(id);

            try {
                mSurfaceTexture.detachFromGLContext();
            } catch (Exception e) {
                Log.e(TAG, "初次detach失败");
            }
        }
        return mSurfaceTexture;
    }

    public static SurfaceTexture getInstance() {
        if (mSurfaceTexture == null) {
            mSurfaceTexture = new SurfaceTexture(createTextureID());
            try {
                mSurfaceTexture.detachFromGLContext();
            } catch (Exception e) {
                Log.e(TAG, "初次detach失败");
            }
        }
        return mSurfaceTexture;
    }

    public synchronized static void draw(DirectDrawer mDirectDrawer, int mTextureID) {

        // TODO Auto-generated method stub
//        Log.i(TAG, "开始绘制纹理Frame..." + mTextureID);
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        getInstance().attachToGLContext(mTextureID);
        getInstance().updateTexImage();
        float[] mtx = new float[16];
        getInstance().getTransformMatrix(mtx);
        mDirectDrawer.draw(mtx);
        getInstance().detachFromGLContext();
//        Log.i(TAG, "完成绘制纹理Frame..." + mTextureID);
    }

    private static int createTextureID() {
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return texture[0];
    }
}
