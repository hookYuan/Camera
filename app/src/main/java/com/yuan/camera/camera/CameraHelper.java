package com.yuan.camera.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;

import com.yuan.camera.common.DataCallBack;
import com.yuan.camera.common.CameraUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 操作相机类
 * 这里只用于获取摄像头数，关于画面预览的数据交由CameraRenderer处理
 * 用于开启、关闭Camera.获取Camera的数据回调
 */
public class CameraHelper implements Camera.PreviewCallback {

    private static final String TAG = "CameraInterface";
    private Camera mCamera;
    private boolean isPreviewing = false;
    private static CameraHelper mCameraInterface;
    private int mCameraId = 0; //需要打开的摄像头ID
    private int minWidth = 800; //预期 预览时的最小宽度
    private int realPreviewWidth = 0; //真实预览的宽度
    private int realPreviewHeight = 0; //真实预览的高度
    //数据回调
    private DataCallBack cameraCallBack;
    //全局错误码
    private int errorCode = Result.CAMERA_INIT;

    private int cameraId = 0;

    private CameraHelper() {
    }

    public static synchronized CameraHelper getInstance() {
        if (mCameraInterface == null) {
            mCameraInterface = new CameraHelper();
        }
        return mCameraInterface;
    }


    public void setCameraCallBack(DataCallBack cameraCallBack) {
        this.cameraCallBack = cameraCallBack;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (cameraCallBack != null) {
            //这里的data数据格式为NV21格式
            cameraCallBack.getData(data, mCameraId, realPreviewWidth, realPreviewHeight);
        }
    }


    public void openCamera(int minWidth) {
        openCamera(minWidth, null);
    }

    public void openCamera(int minWidth, OpenCallBack callback) {
        openCamera(0, minWidth, callback);
    }

    /**
     * 打开Camera
     *
     * @param cameraId 需要打开的摄像头id
     * @param minWidth 需要启用摄像头最小的画面宽度（画面宽高自动计算相近值）
     * @param callback 开启状态码回调
     */
    public void openCamera(int cameraId, int minWidth, OpenCallBack callback) {
        this.minWidth = minWidth;
        if (mCamera == null) {
            int cameraNumber = Camera.getNumberOfCameras();
            if (cameraNumber <= 0) {
                errorCode = Result.CAMERA_NO;
                return;
            }
            if (cameraNumber <= cameraId) {
                errorCode = Result.CAMERA_NOFIND;
                return;
            }
            try {
                this.cameraId = cameraId;
                mCamera = Camera.open(cameraId);
                errorCode = Result.CAMERA_OPENOK;
            } catch (RuntimeException e) {
                Log.e(TAG, e.getMessage());
                errorCode = Result.CAMERA_PERMISSION;
            }
        } else {
            errorCode = Result.CAMERA_OPENING;
        }
        if (callback != null) callback.onOpen(errorCode);
    }


    /**
     * 使用SurfaceView开启预览
     * 摄像头画面使用默认方向
     *
     * @param holder
     */
    public void startPreview(SurfaceHolder holder) {
        startPreview(holder, null);
    }

    /**
     * 使用SurfaceView开启预览
     *
     * @param activity 根据Activity的方向，调整摄像头的方向
     * @param holder
     */
    public void startPreview(SurfaceHolder holder, Activity activity) {
        Log.i(TAG, "doStartPreview...");
        if (isPreviewing) {
            return;
        }
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                errorCode = Result.CAMERA_PREVIEW_FAIL;
            }
            initCameraForPreview(activity);
        }
    }

    /**
     * 使用TextureView预览Camera
     *
     * @param surface
     */
    public void startPreview(SurfaceTexture surface) {
        if (isPreviewing) {
            return;
        }
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surface);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                errorCode = Result.CAMERA_PREVIEW_FAIL;
            }
            initCameraForPreview(null);
        }
    }

    /**
     * 使用TextureView预览Camera2
     *
     * @param surface
     */
    public void startPreviewCamera2(SurfaceTexture surface) {
        Log.i(TAG, "doStartPreview...");
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surface);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "绑定preview到surface失败" + e.toString());
                e.printStackTrace();
                errorCode = Result.CAMERA_PREVIEW_FAIL;
            }
            if (!isPreviewing) {
                initCameraForPreview(null);
            }
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            isPreviewing = false;
        }
    }

    /**
     * 停止预览，关闭Camera
     */
    public void close() {
        if (null != mCamera) {
            try {
                isPreviewing = false;
                mCamera.cancelAutoFocus();
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    /**
     * 获取是否正在预览中
     *
     * @return
     */
    public boolean isPreviewing() {
        return isPreviewing;
    }

    public int getRealPreviewWidth() {
        return realPreviewWidth;
    }

    public int getRealPreviewHeight() {
        return realPreviewHeight;
    }

    public int getmCameraId() {
        return mCameraId;
    }

    /**
     * 初始化相机参数配置、适配相机数据返回为NV21
     */
    private void initCameraForPreview(Activity activity) {
        if (mCamera != null) {
            Camera.Parameters mParams = mCamera.getParameters();
            //获取摄像头对焦模式
            List<String> focusModes = mParams.getSupportedFocusModes();
            if (focusModes.contains("continuous-video")) {
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            //获取预览时fps的范围
            List<int[]> range = mParams.getSupportedPreviewFpsRange();
            for (int j = 0; j < range.size(); j++) {
                int[] r = range.get(j);
                for (int k = 0; k < r.length; k++) {
                    Log.i(TAG, TAG + r[k]);
                }
            }
            //设置最佳预览尺寸
            Size previewSize = CameraUtil.getCameraPreviewSize(
                    mParams.getSupportedPreviewSizes(), minWidth);
            if (previewSize != null) {
                realPreviewWidth = previewSize.width;
                realPreviewHeight = previewSize.height;
                mParams.setPreviewSize(previewSize.width, previewSize.height);
            } else {
                Log.i(TAG, "设置预览失败");
                errorCode = Result.CAMERA_PREVIEW_SIZE_FAIL;
            }
            Log.i(TAG, "最终设置:preview--With = " + realPreviewWidth
                    + "Height = " + realPreviewHeight);
            //设置预览回调数据格式
            mParams.setPreviewFormat(ImageFormat.NV21);
            CameraUtil.setCameraZoom(mParams);
            mCamera.setParameters(mParams);
            if (activity != null) {
                mCamera.setDisplayOrientation(CameraUtil.getOrientationDegrees(activity, cameraId));
            }
            mCamera.startPreview();//开启预览
            isPreviewing = true;
            mCamera.setPreviewCallback(this);
        }
    }


    /**
     * 重新开启预览，停止预览后有效。
     */
    public void reStartPreview() {
        if (mCamera != null && !isPreviewing) {
            mCamera.startPreview();
        }
    }

    /**
     * 拍照
     *
     * @param savePath    拍照保存地址
     * @param onTakePhoto 拍照成功、照片保存成功后回调接口
     */
    public void takePhoto(final String savePath, final OnTakePhoto onTakePhoto) {
        if (mCamera != null)
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    //将字节数组
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    //压缩图片
                    Matrix matrix = new Matrix();
                    matrix.setScale(0.1f, 0.1f);
                    Bitmap mSrcBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    bitmap = null;
                    //输出流保存数据
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(savePath);
                        mSrcBitmap.compress(Bitmap.CompressFormat.PNG, 85, fileOutputStream);
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        camera.stopPreview();
                        isPreviewing = false;
                        onTakePhoto.onSuccess();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
    }

    /**
     * 拍照成功回调
     */
    public interface OnTakePhoto {
        void onSuccess();
    }

    /**
     * 摄像头开启时状态回调
     */
    public interface OpenCallBack {
        void onOpen(int result);
    }

    /**
     * 摄像头开始错误码及说明
     */
    public interface Result {
        int CAMERA_OPENOK = 0; //摄像头开启成功
        int CAMERA_NO = 1; //不存在摄像头
        int CAMERA_NOFIND = 2; //未找到指定id的摄像头
        int CAMERA_OPENING = 3;//摄像头已经打开
        int CAMERA_INIT = 4;//摄像头未初始化
        int CAMERA_PREVIEW_FAIL = 5;//摄像头设置预览画布失败
        int CAMERA_PREVIEW_SIZE_FAIL = 6;//摄像头设置预览尺寸失败
        int CAMERA_PERMISSION = 7;//检测摄像头权限是否开启
    }

}
