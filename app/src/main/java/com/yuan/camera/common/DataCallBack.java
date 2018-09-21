package com.yuan.camera.common;


/**
 * Created by YuanYe on 2018/5/24.
 * 摄像头实时预览帧数据回调
 */
public interface DataCallBack {

    /**
     * @param data        摄像头回调原始数据
     * @param cameraId    摄像头id
     * @param videoWidth  摄像头数据的真实宽
     * @param videoHeight 摄像头数据的真实高
     */
    void getData(byte[] data, int cameraId, int videoWidth, int videoHeight);
}
