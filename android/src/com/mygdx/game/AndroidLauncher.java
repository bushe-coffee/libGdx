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
import com.dressplus.dnn.DrawingSurfaceView;
import com.dressplus.dnn.ObjHunter;
import com.dressplus.dnn.ObjTracker;
import com.dressplus.dnn.TrackObject;
import com.dressplus.dnn.utils.LayoutUtil;
import com.dressplus.dnn.utils.MySystemParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AndroidLauncher extends AndroidApplication {

    private Camera mCamera;
    private SurfaceHolder mHolder;
    private ParticleAdapter particleAdapter;

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

        particleAdapter = new ParticleAdapter();
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

        if (mObjTracker == null) {
            mObjTracker = ObjTracker.getInstance();
        }

        mObjTracker.InitObjTracker(1, PREVIEW_WIDTH, PREVIEW_HEIGHT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
        mHolder.addCallback(holderCallback);

        killed = false;
        Thread thread = new Thread(tracker_runnable);

        thread.start();
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
            frame_data = data.clone();
            isNV21ready = true;
            camera.addCallbackBuffer(mBuffer);

        }
    };

    // ansync byte
    private boolean detected = false;
    private boolean tracker_init = false;
    private List<TrackObject> obj_result = null;
    private List<String> labels = new ArrayList();
    private static final int PREVIEW_WIDTH = 320, PREVIEW_HEIGHT = 240;
    private byte[] frame_data = null;
    private boolean killed = false;
    private boolean isNV21ready = false;

    private ObjTracker mObjTracker;

    private Runnable tracker_runnable = new Runnable() {
        byte[] tmp = null;

        @Override
        public void run() {

            while (!killed) {
                if (!isNV21ready) {
                    continue;
                }

                synchronized (frame_data) {
                    tmp = frame_data.clone();
                }

                isNV21ready = false;
                int width;
                int height;
                if (mCamera != null) {
                    Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
                    width = previewSize.width;
                    height = previewSize.height;

                    if (!detected) {
                        String result = ObjHunter.getInstance().objdetector(tmp, 1, width, height);
                        Log.e("maxiaoran  ", "ffffffffffff   " + result);
                        if (result != null && result.length() > 1) {
                            obj_result = TrackObject.segment(result, "~", 0);
                            if (obj_result != null && obj_result.size() > 0) {
                                detected = true;
                            }
                        }
                        //
                        if (obj_result != null && obj_result.size() > 0 && particleAdapter != null) {

                        } else {
                            System.out.println("position  false  ");
                            particleAdapter.setPosition(false, 0.1f, 0.1f);
                        }

                    }

                    Camera.CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
                    boolean rotate270 = info.orientation == 270;
                    boolean frontCamera = false;
                    if (detected && !tracker_init) {
                        Log.e("maxiaoran ", "System time5  " + System.currentTimeMillis() + obj_result.get(0).top + "  " + obj_result.get(0).left);
                        Collections.sort(obj_result, new Comparator<TrackObject>() {
                            public int compare(TrackObject o1, TrackObject o2) {
                                return o2.probability < o1.probability ? 1 : -1;
                            }
                        });

                        obj_result = obj_result.subList(0, obj_result.size() > 4 ? 4 : obj_result.size());

                        int index = 0;
                        for (TrackObject obj_item : obj_result) {
                            Log.e("maxiaoran ", "System time6  " + System.currentTimeMillis() + obj_result.get(0).top + "  " + obj_result.get(0).left);
                            labels.add(obj_item.getName());
                            int[] rect = new int[4];
                            rect[0] = (int) (obj_item.left * PREVIEW_HEIGHT);
                            rect[1] = (int) (obj_item.top * PREVIEW_WIDTH);
                            rect[2] = (int) (obj_item.width * PREVIEW_HEIGHT);
                            rect[3] = (int) (obj_item.height * PREVIEW_WIDTH);
                            rect = mObjTracker.calibreRectBeforeAdd(rect, rotate270, frontCamera);
                            int flag = mObjTracker.addObjTracker(tmp, rect, index);
                            index++;
                            if (flag == 0) {
                                detected = false;
                                break;
                            } else {
                                tracker_init = true;
                            }
                        }
                    }

                    if (tracker_init) {
                        List<int[]> result = mObjTracker.Update(tmp);
                        int[] _rect;
                        if (result == null) {
                            detected = false;
                            tracker_init = false;
                            obj_result = null;
                        } else {
                            for (int i = 0; i < result.size() && i < obj_result.size(); i++) {
                                _rect = result.get(i);
                                if (_rect == null) {
                                    detected = false;
                                    tracker_init = false;
                                    obj_result = null;
                                    break;
                                }
                                _rect = mObjTracker.calibreRectAfterUpdate(_rect, rotate270, frontCamera);
                                obj_result.get(i).update(_rect, PREVIEW_HEIGHT, PREVIEW_WIDTH);
                            }
                        }


                        if (obj_result != null && obj_result.size() > 0 && particleAdapter != null) {
                            TrackObject object = obj_result.get(0);
                            float x = object.left;// + object.width * 0.5f ;
                            float y = object.top;// + object.height * 0.5f ;
                            System.out.println("position left top  " + obj_result.get(0).left + "   " + obj_result.get(0).top);
                            System.out.println("position00  " + x + "   " + y);
                            particleAdapter.setPosition(true, x, y);
                        } else {
                            System.out.println("position  false  ");
                            particleAdapter.setPosition(false, 10, 10);
                        }
                    }
                }
            }
        }
    };

}
