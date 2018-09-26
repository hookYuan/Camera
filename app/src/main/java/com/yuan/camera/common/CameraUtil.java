package com.yuan.camera.common;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.yuan.camera.camera.CameraHelper;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by YuanYe on 2018/9/21.
 * 相机通用工具类
 * 1.根据最小宽度获取摄像头支持的最佳预览尺寸
 */
public class CameraUtil {

    private final static String TAG = "CameraUtil";
    private static final int TEN_DESIRED_ZOOM = 27;
    public static final String PRIVEWWIDTH = "width";
    public static final String PRIVEWHEIGHT = "height";


    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    /**
     * 获取系统Camera 指定最小宽度的最佳预览尺寸
     *
     * @param list
     * @param minWidth
     */
    public static Camera.Size getCameraPreviewSize(List<Camera.Size> list, int minWidth) {
        CameraSizeComparator sizeComparator = new CameraSizeComparator();
        Collections.sort(list, sizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            if ((s.width >= minWidth)) {
                Log.i(TAG, "PreviewSize:w = " + s.width + "h = " + s.height);
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;//如果没找到，就选最小的size
        }
        if (list != null && list.size() > 0) return list.get(i);
        return null;
    }

    /**
     * 获取UVCCamera指定最小宽度的最佳预览尺寸
     *
     * @param minWidth
     */
    public static void getUVCCameraPreviewSize(int minWidth) {

    }


    /**
     * 设置Camera变焦
     *
     * @param parameters
     */
    public static void setCameraZoom(Camera.Parameters parameters) {
        String zoomSupportedString = parameters.get("zoom-supported");
        if (zoomSupportedString != null && !Boolean.parseBoolean(zoomSupportedString)) {
            return;
        }
        int tenDesiredZoom = TEN_DESIRED_ZOOM;
        String maxZoomString = parameters.get("max-zoom");
        if (maxZoomString != null) {
            try {
                int tenMaxZoom = (int) (10.0 * Double.parseDouble(maxZoomString));
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom;
                }
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }

        String takingPictureZoomMaxString = parameters.get("taking-picture-zoom-max");
        if (takingPictureZoomMaxString != null) {
            try {
                int tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString);
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom;
                }
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }

        String motZoomValuesString = parameters.get("mot-zoom-values");
        if (motZoomValuesString != null) {
            tenDesiredZoom = findBestMotZoomValue(motZoomValuesString, tenDesiredZoom);
        }

        String motZoomStepString = parameters.get("mot-zoom-step");
        if (motZoomStepString != null) {
            try {
                double motZoomStep = Double.parseDouble(motZoomStepString.trim());
                int tenZoomStep = (int) (10.0 * motZoomStep);
                if (tenZoomStep > 1) {
                    tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
                }
            } catch (NumberFormatException nfe) {
                // continue
            }
        }
        if (maxZoomString != null || motZoomValuesString != null) {
            parameters.set("zoom", String.valueOf(tenDesiredZoom / 10.0));
        }
        if (takingPictureZoomMaxString != null) {
            parameters.set("taking-picture-zoom", tenDesiredZoom);
        }
    }

    private static int findBestMotZoomValue(CharSequence stringValues, int tenDesiredZoom) {
        int tenBestValue = 0;
        for (String stringValue : COMMA_PATTERN.split(stringValues)) {
            stringValue = stringValue.trim();
            double value;
            try {
                value = Double.parseDouble(stringValue);
            } catch (NumberFormatException nfe) {
                return tenDesiredZoom;
            }
            int tenValue = (int) (10.0 * value);
            if (Math.abs(tenDesiredZoom - value) < Math.abs(tenDesiredZoom - tenBestValue)) {
                tenBestValue = tenValue;
            }
        }
        return tenBestValue;
    }

    /**
     * 获取旋转角度
     *
     * @param activity
     * @param cameraId
     */
    public static int getOrientationDegrees(Activity activity, int cameraId) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;   // compensate the mirror
        } else {
            // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }


    public static class CameraSizeComparator implements Comparator<Camera.Size> {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
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


    /**
     * 根据View的大小取和系统Camera预览的宽高比获取最大的View尺寸
     *
     * @param activity
     * @return HashMap<></> key--width,value--height
     */
    public static HashMap<String, Integer> getBaseWidthHeight(Activity activity, int viewWidth, int viewHeight) {

        int minSize = viewWidth > viewHeight ? viewHeight : viewWidth;
        HashMap<String, Integer> map = new HashMap();

        if (CameraHelper.getInstance().getRealPreviewHeight() == 0 ||
                CameraHelper.getInstance().getRealPreviewWidth() == 0) {
            map.put(PRIVEWWIDTH, viewWidth);
            map.put(PRIVEWHEIGHT, viewHeight);
            return map;
        }
        float previewWidth = CameraHelper.getInstance().getRealPreviewWidth();
        float preViewHeight = CameraHelper.getInstance().getRealPreviewHeight();
        //最大边/最小边
        float rate = previewWidth > preViewHeight ? previewWidth / preViewHeight : preViewHeight / previewWidth;

        float maxSize = minSize * rate;

        //根据角度动态计算宽高比
        int degrees = CameraUtil.getOrientationDegrees(activity, CameraHelper.getInstance().getmCameraId());
        if (degrees == 90 || degrees == 270) {
            viewHeight = (int) maxSize;
            viewWidth = minSize;
        } else {
            viewHeight = (int) minSize;
            viewWidth = (int) maxSize;
        }
        map.put(PRIVEWWIDTH, viewWidth);
        map.put(PRIVEWHEIGHT, viewHeight);
        return map;
    }

}
