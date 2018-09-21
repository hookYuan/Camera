package com.yuan.camera.surface;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.yuan.camera.camera.CameraHelper;
import com.yuan.camera.common.CameraUtil;

/**
 * Created by YuanYe on 2018/9/21.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraView";

    private Context context;

    public CameraView(Context context) {
        super(context);
        initCamera();
    }

    public CameraView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initCamera();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initCamera();
    }

    private void initCamera() {
        getHolder().addCallback(this);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childWidthSize = getMeasuredWidth();
        int childHeightSize = getMeasuredHeight();

        int minSize = childWidthSize > childHeightSize ? childHeightSize : childWidthSize;

        if (context instanceof Activity) {
            //根据角度动态计算宽高比
            int degrees = CameraUtil.getOrientationDegrees((Activity) context, CameraHelper.getInstance().getmCameraId());
            if (CameraHelper.getInstance().getRealPreviewHeight() == 0 ||
                    CameraHelper.getInstance().getRealPreviewWidth() == 0) {
                return;
            }
            float previewWidth = CameraHelper.getInstance().getRealPreviewWidth();
            float preViewHeight = CameraHelper.getInstance().getRealPreviewHeight();
            //最大边/最小边
            float rate = previewWidth > preViewHeight ? previewWidth / preViewHeight : preViewHeight / previewWidth;

            float maxSize = minSize * rate;

            if (degrees == 90 || degrees == 270) {
                childHeightSize = (int) maxSize;
                childWidthSize = minSize;
            } else {
                childHeightSize = (int) minSize;
                childWidthSize = (int) maxSize;
            }
        }
        //设置宽高比始终保持为摄像头画面的真实宽高比
        setMeasuredDimension(childWidthSize, childHeightSize);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "创建surfaceView");
        //这里默认开启的是
        CameraHelper.getInstance().openCamera(800);
        if (context instanceof Activity) {
            CameraHelper.getInstance().startPreview(holder, (Activity) context);
        } else {
            CameraHelper.getInstance().startPreview(holder);
        }
        //刷新宽高
        requestLayout();
        invalidate();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "刷新surfaceView");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "销毁surfaceView");
        CameraHelper.getInstance().close();
    }
}
