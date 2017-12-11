package com.mygdx.game;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.dressplus.dnn.ObjHunter;
import com.dressplus.dnn.ObjTracker;
import com.dressplus.dnn.TrackObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AndroidLauncher2 extends AndroidApplication {

    private Camera mCamera;
    private SurfaceHolder mHolder;
    private ApplicationAdapter particleAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL30 = false;  // 使用 GL20  API > 11
        cfg.r = 8;
        cfg.g = 8;
        cfg.b = 8;
        cfg.a = 8;
        cfg.depth = 16;
        cfg.stencil = 0;

//        initialize(new ParticleAdapter(), cfg);

        particleAdapter = new ParticleAdapter3D();
        View view = initializeForView(particleAdapter, cfg);
        if (graphics.getView() instanceof GLSurfaceView) {
            // set background translate
            GLSurfaceView view1 = (GLSurfaceView) graphics.getView();
            view1.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            view1.setZOrderOnTop(true);
            view1.setZOrderMediaOverlay(true);
        }

        setContentView(R.layout.main_activity);
        FrameLayout container = (FrameLayout) findViewById(R.id.main_container);
        container.removeAllViews();
        container.addView(view);

        // camera view
        SurfaceView surfaceView = new SurfaceView(this);
        container.addView(surfaceView);
        surfaceView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mHolder = surfaceView.getHolder();

    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
        mHolder.addCallback(holderCallback);

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private byte mBuffer[];

    private void setStartPreview(SurfaceHolder holder) {
        try {
            if (mCamera != null && holder != null) {
                mCamera.setPreviewDisplay(holder);
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
                mCamera.setDisplayOrientation(info.orientation);

                // setPreviewCallbackWithBuffer 是可控的 需要在开始 之前分配一个 buffer 地址
                mCamera.setPreviewCallbackWithBuffer(previewCallback);
                Camera.Parameters parameters = mCamera.getParameters();
                Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
                Camera.Size pictureSize = mCamera.getParameters().getPictureSize();
                mBuffer = new byte[Math.max(
                        previewSize.width * previewSize.height * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8,
                        pictureSize.width * pictureSize.height * ImageFormat.getBitsPerPixel(ImageFormat.RGB_565) / 8
                )];

                // 触发第一次 回掉
                mCamera.addCallbackBuffer(mBuffer);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("maxiaoran  ", "camera preview fail");
        }
    }


    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void initCamera() {
        try {
            mCamera = Camera.open();
            // set camera parameters
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            parameters.setPreviewFormat(ImageFormat.NV21);
            parameters.set("jpeg-quality", 100);
            // 这句话 得有 ，不然返回的数据不对。
            parameters.setPreviewSize(320, 240);

            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            // default NV21  只能是 NV21 或者 YV12
            parameters.setPreviewFormat(ImageFormat.NV21);
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
            mCamera = null;
            Log.e("maxiaoran  ", "camera open fail");
        }
    }

    private SurfaceHolder.Callback holderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // when surface create ,the camera can build relation
            setStartPreview(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }
    };

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // 执行就会 回掉一次， 获得 数据
            camera.addCallbackBuffer(mBuffer);

        }
    };
}
