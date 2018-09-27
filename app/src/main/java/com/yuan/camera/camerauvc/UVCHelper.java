package com.yuan.camera.camerauvc;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.yuan.camera.common.DataCallBack;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by YuanYe on 2018/9/17.
 * 调用UVC库的工具类
 * 实现打开相机、数据回调
 */
public class UVCHelper {

    private static final String TAG = "UVCHelper";
    /**
     * for accessing USB
     */
    private USBMonitor mUSBMonitor;

    private CameraHandler mHandler;
    private static UVCHelper instance;

    public static UVCHelper getInstance() {
        if (instance == null) {
            instance = new UVCHelper();
        }
        return instance;
    }

    private UVCHelper() {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void openCamera(final Activity activity, final int width, final int height) {
        openCamera(activity, width, height, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void openCamera(final Activity activity, final int width, final int height, final OpenCameraCallBack openCameraCallBack) {
        //USB连接初始化
        mUSBMonitor = new USBMonitor(activity, new USBMonitor.OnDeviceConnectListener() {
            @Override
            public void onAttach(UsbDevice device) {
            }

            @Override
            public void onDettach(UsbDevice device) {
            }

            @Override
            public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                //开启摄像头
                mHandler.openCamera(ctrlBlock, openCameraCallBack);
                mHandler.setFrameCallback(new IFrameCallback() {
                    @Override
                    public void onFrame(ByteBuffer frame) {
                        if (cameraCallBack != null) {
                            int len = frame.capacity();
                            final byte[] yuv = new byte[len];
                            frame.get(yuv);
                            cameraCallBack.getData(yuv, 1, mHandler.getPreviewWidth()
                                    , mHandler.getPreviewHeight());
                            Log.i(TAG, "-摄像头返回数据------" + yuv.length);
                        }
                    }
                });
            }

            @Override
            public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
                if (mHandler != null) {
                    //断开摄像头
                    mHandler.closeCamera();
                }
            }

            @Override
            public void onCancel(UsbDevice device) {

            }
        });
        mUSBMonitor.register();
        List<UsbDevice> list = mUSBMonitor.getDeviceList();
        for (int i = 0; i < list.size(); i++) {
            //TODO 这里通过摄像头信息过滤需要开启的指定摄像头，更换摄像头时需要更改
            if ("Alcor Micro, Corp.".equals(list.get(i).getManufacturerName())) {
                mUSBMonitor.requestPermission(list.get(i));
            }
        }
        mHandler = CameraHandler.createHandler(activity, width, height, UVCCamera.DEFAULT_PREVIEW_MODE);
    }

    public void startPreview(SurfaceTexture surfaceTexture) {
        if (mHandler != null && mUSBMonitor != null
                && mUSBMonitor.isRegistered() && mHandler.isCameraOpened()) {
            mHandler.startPreview(surfaceTexture);
        }
    }

    public void stopPreview() {
        if (mHandler != null && mUSBMonitor != null
                && mUSBMonitor.isRegistered() && mHandler.isCameraOpened()) {
            mHandler.stopPreview();
        }
    }

    public void close() {
        if (mHandler != null && mUSBMonitor != null
                && mUSBMonitor.isRegistered() && mHandler.isCameraOpened()) {
            mHandler.release();
        }
    }

    //数据回调
    private DataCallBack cameraCallBack;

    public void setCameraCallBack(DataCallBack cameraCallBack) {
        this.cameraCallBack = cameraCallBack;
    }

    public interface OpenCameraCallBack {
        void onOpen();
    }
}
