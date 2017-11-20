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

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import java.io.IOException;

public class AndroidLauncher extends AndroidApplication {

    private Camera mCamera;
    private SurfaceHolder mHolder;

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

        View view = initializeForView(new YangMainActivity(), cfg);
        if (graphics.getView() instanceof GLSurfaceView) {
            // set background translate
            GLSurfaceView view1 = (GLSurfaceView) graphics.getView();
            view1.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            view1.setZOrderOnTop(true);
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
        // 设置 工作模式
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
                mCamera.setDisplayOrientation(90);
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
            Log.e("maxiaoran ", "System time1  " + System.currentTimeMillis());
            Log.e("maxiaoran  ", "camera data length1  " + data.length);
            // 执行就会 回掉一次， 获得 数据
            camera.addCallbackBuffer(mBuffer);

            Log.e("maxiaoran  ", "camera data length2  " + mBuffer.length);
            Log.e("maxiaoran ", "System time2  " + System.currentTimeMillis());

        }
    };
}
