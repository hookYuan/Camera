package com.yuan.camera.camerauvc;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;

import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Handler class to execute camera releated methods sequentially on private thread
 * 在子线程上顺序执行相机释放方法的处理程序类
 * CameraHandler对外部公开，真正的Camera处理在CameraThread子类中
 */
public final class CameraHandler extends Handler {

    private static final boolean DEBUG = true;
    private static final String TAG = "CameraHandler";

    private static final int MSG_OPEN = 0;  //打开Camera
    private static final int MSG_CLOSE = 1; //关闭Camera
    private static final int MSG_PREVIEW_START = 2; //开启预览
    private static final int MSG_PREVIEW_STOP = 3; //停止预览
    private static final int MSG_CAPTURE_STILL = 4; //
    private static final int MSG_CAPTURE_START = 5;
    private static final int MSG_CAPTURE_STOP = 6;
    private static final int MSG_MEDIA_UPDATE = 7; //刷新
    private static final int MSG_RELEASE = 9; //释放

    private static int previewWidth; //预览的宽高
    private static int previewHeight;
    private static int previewMode; //预览模式：

    private static IFrameCallback mIFrameCallback;

    private final WeakReference<CameraThread> mWeakThread;
    private static CameraSizeComparator sizeComparator = new CameraHandler.CameraSizeComparator();

    private static UVCHelper.OpenCameraCallBack openCameraCallBack;//摄像头回调

    /**
     * @param parent Activity子类
     * @param width  分辨率的宽
     * @param height 分辨率的高
     * @param format 颜色格式，0为FRAME_FORMAT_YUYV；1为FRAME_FORMAT_MJPEG
     * @return
     */
    public static final CameraHandler createHandler(final Activity parent,
                                                    final int width, final int height, final int format) {
        previewHeight = height;
        previewWidth = width;
        previewMode = format;

        final CameraHandler.CameraThread thread = new CameraHandler.CameraThread(parent);
        thread.start();
        return thread.getHandler();
    }

    private CameraHandler(final CameraHandler.CameraThread thread) {
        mWeakThread = new WeakReference<CameraThread>(thread);
    }

    public void setFrameCallback(IFrameCallback callback) {
        mIFrameCallback = callback;
    }

    /**
     * 相机是否打开
     *
     * @return
     */
    public boolean isCameraOpened() {
        final CameraHandler.CameraThread thread = mWeakThread.get();
        return thread != null ? thread.isCameraOpened() : false;
    }

    /**
     * 是否正在录音
     *
     * @return
     */
    public boolean isRecording() {
        final CameraHandler.CameraThread thread = mWeakThread.get();
        return thread != null ? thread.isRecording() : false;
    }

    /**
     * 打开摄像头
     *
     * @param ctrlBlock
     */
    public void openCamera(final USBMonitor.UsbControlBlock ctrlBlock, UVCHelper.OpenCameraCallBack openCameraCallBack) {
        this.openCameraCallBack = openCameraCallBack;
        sendMessage(obtainMessage(MSG_OPEN, ctrlBlock));
    }

    /**
     * 关闭摄像头
     */
    public void closeCamera() {
        stopPreview();
        sendEmptyMessage(MSG_CLOSE);
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    /**
     * 开启预览
     *
     * @param sureface
     */
    public void startPreview(final SurfaceView sureface) {
        if (sureface != null)
            sendMessage(obtainMessage(MSG_PREVIEW_START, sureface));
    }

    /**
     * 开启预览
     *
     * @param surfaceTexture
     */
    public void startPreview(final SurfaceTexture surfaceTexture) {
        if (surfaceTexture != null) {
            sendMessage(obtainMessage(MSG_PREVIEW_START, surfaceTexture));
        }

    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        stopRecording();
        final CameraHandler.CameraThread thread = mWeakThread.get();
        if (thread == null) return;
        synchronized (thread.mSync) {
            sendEmptyMessage(MSG_PREVIEW_STOP);
            // wait for actually preview stopped to avoid releasing Surface/SurfaceTexture
            // while preview is still running.
            // therefore this method will take a time to execute
            try {
                thread.mSync.wait();
            } catch (final InterruptedException e) {
            }
        }
    }

    /**
     * 停止录音
     */
    public void stopRecording() {
        sendEmptyMessage(MSG_CAPTURE_STOP);
    }

    /**
     * 释放资源
     */
    public void release() {
        sendEmptyMessage(MSG_RELEASE);
    }

    public List<Size> getSupportedPreviewSizes() {
        return mWeakThread.get().getSupportedSizes();
    }


    @Override
    public void handleMessage(final Message msg) {
        final CameraHandler.CameraThread thread = mWeakThread.get();
        if (thread == null) return;
        switch (msg.what) {
            case MSG_OPEN:
                thread.handleOpen((USBMonitor.UsbControlBlock) msg.obj);
                break;
            case MSG_CLOSE:
                thread.handleClose();
                break;
            case MSG_PREVIEW_START:
                thread.handleStartPreview(msg.obj);
                break;
            case MSG_PREVIEW_STOP:
                thread.handleStopPreview();
                break;
            case MSG_MEDIA_UPDATE:
                thread.handleUpdateMedia((String) msg.obj);
                break;
            case MSG_RELEASE:
                thread.handleRelease();
                break;
            default:
                throw new RuntimeException("unsupported message:what=" + msg.what);
        }
    }


    /**
     * 真实的摄像头处理线程
     */
    private static final class CameraThread extends Thread {
        private static final String TAG_THREAD = "CameraThread";
        private final Object mSync = new Object();
        private final WeakReference<Activity> mWeakParent;
        /**
         * shutter sound
         * 开门声
         */
        private SoundPool mSoundPool;
        private CameraHandler mHandler;
        /**
         * for accessing UVC camera
         * UVCCamera操作对象
         */
        private UVCCamera mUVCCamera;

        private CameraThread(final Activity parent) {
            super("CameraThread");
            mWeakParent = new WeakReference<Activity>(parent);
            loadSutterSound(parent);
        }

        @Override
        protected void finalize() throws Throwable {
            Log.i(TAG, "CameraThread#finalize");
            super.finalize();
        }

        public CameraHandler getHandler() {
            if (DEBUG) Log.v(TAG_THREAD, "getHandler:");
            synchronized (mSync) {
                if (mHandler == null)
                    try {
                        mSync.wait();
                    } catch (final InterruptedException e) {
                    }
            }
            return mHandler;
        }

        /**
         * UVCCamera是否开启
         *
         * @return boolean
         */
        public boolean isCameraOpened() {
            return mUVCCamera != null;
        }

        /**
         * UVCCamera是否正在录音
         *
         * @return boolean
         */
        public boolean isRecording() {
            return (mUVCCamera != null);
        }

        /**
         * 打开UVCCamera
         *
         * @return boolean
         */
        public void handleOpen(final USBMonitor.UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.v(TAG_THREAD, "handleOpen:");
            handleClose();
            mUVCCamera = new UVCCamera();
            mUVCCamera.open(ctrlBlock);
            if (DEBUG) Log.i(TAG, "supportedSize:" + mUVCCamera.getSupportedSize());
            if (openCameraCallBack != null) openCameraCallBack.onOpen();
        }

        /**
         * 关闭UVCCamera
         */
        public void handleClose() {
            if (DEBUG) Log.v(TAG_THREAD, "handleClose:");
            if (mUVCCamera != null) {
                mUVCCamera.stopPreview();
                mUVCCamera.destroy();
                mUVCCamera = null;
            }
        }

        /**
         * 开启预览
         */
        public void handleStartPreview(Object object) {
            if (DEBUG) Log.v(TAG_THREAD, "handleStartPreview:");
            if (mUVCCamera == null) return;
            try {
                //动态计算宽高
                Collections.sort(getSupportedSizes(), sizeComparator);
                int i = 0;
                for (Size s : getSupportedSizes()) {
                    if ((s.width >= previewWidth)) {
                        Log.i(TAG, "PreviewSize:w = " + s.width + "h = " + s.height);
                        previewWidth = s.width;
                        previewHeight = s.height;
                        break;
                    }
                    i++;
                }
                if (i == getSupportedSizes().size()) {
                    previewWidth = getSupportedSizes().get(0).width;
                    previewHeight = getSupportedSizes().get(0).height;
                }
                mUVCCamera.setPreviewSize(previewWidth, previewHeight, previewMode);
            } catch (final IllegalArgumentException e) {
                try {
                    // fallback to YUV mode
                    mUVCCamera.setPreviewSize(previewWidth, previewHeight, UVCCamera.DEFAULT_PREVIEW_MODE);
                } catch (final IllegalArgumentException e1) {
                    handleClose();
                }
            }
            if (mUVCCamera != null) {
                if (mIFrameCallback != null)
                    mUVCCamera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_NV21);
                //设置预览的画布类型
                if (object instanceof SurfaceTexture) {
                    mUVCCamera.setPreviewTexture((SurfaceTexture) object);
                }
                mUVCCamera.startPreview();
            }
        }

        /**
         * UVCCamera停止预览
         */
        public void handleStopPreview() {
            if (DEBUG) Log.v(TAG_THREAD, "handleStopPreview:");
            if (mUVCCamera != null) {
                mUVCCamera.stopPreview();
            }
            synchronized (mSync) {
                mSync.notifyAll();
            }
        }

        /**
         * 更新数据
         *
         * @param path
         */
        public void handleUpdateMedia(final String path) {
            if (DEBUG) Log.v(TAG_THREAD, "handleUpdateMedia:path=" + path);
            final Activity parent = mWeakParent.get();
            if (parent != null && parent.getApplicationContext() != null) {
                try {
                    if (DEBUG) Log.i(TAG, "MediaScannerConnection#scanFile");
                    MediaScannerConnection.scanFile(parent.getApplicationContext(), new String[]{path}, null, null);
                } catch (final Exception e) {
                    Log.e(TAG, "handleUpdateMedia:", e);
                }
                if (parent.isDestroyed())
                    handleRelease();
            } else {
                Log.w(TAG, "MainActivity already destroyed");
                // give up to add this movice to MediaStore now.
                // Seeing this movie on Gallery app etc. will take a lot of time.
                handleRelease();
            }
        }

        /**
         * 释放资源
         */
        public void handleRelease() {
            if (DEBUG) Log.v(TAG_THREAD, "handleRelease:");
            handleClose();
            if (!isRecording()) Looper.myLooper().quit();
        }


        // 获取支持的分辨率
        public List<Size> getSupportedSizes() {
            if ((mUVCCamera == null))
                return null;
            return mUVCCamera.getSupportedSizeList();
        }

        /**
         * prepare and load shutter sound for still image capturing
         * 用于拍照和加载快门声
         */
        @SuppressWarnings("deprecation")
        private void loadSutterSound(final Context context) {
            // get system stream type using refrection
            int streamType;
            try {
                final Class<?> audioSystemClass = Class.forName("android.media.AudioSystem");
                final Field sseField = audioSystemClass.getDeclaredField("STREAM_SYSTEM_ENFORCED");
                streamType = sseField.getInt(null);
            } catch (final Exception e) {
                streamType = AudioManager.STREAM_SYSTEM;    // set appropriate according to your app policy
            }
            if (mSoundPool != null) {
                try {
                    mSoundPool.release();
                } catch (final Exception e) {
                }
                mSoundPool = null;
            }
            // load sutter sound from resource
            mSoundPool = new SoundPool(2, streamType, 0);
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (mSync) {
                mHandler = new CameraHandler(this);
                mSync.notifyAll();
            }
            Looper.loop();
            synchronized (mSync) {
                mHandler = null;
                mSoundPool.release();
                mSoundPool = null;
                mSync.notifyAll();
            }
        }
    }


    public static class CameraSizeComparator implements Comparator<Size> {
        public int compare(Size lhs, Size rhs) {
            // TODO Auto-generated method stub
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}