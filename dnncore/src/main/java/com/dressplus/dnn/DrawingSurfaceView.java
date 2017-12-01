package com.dressplus.dnn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.dressplus.dnn.utils.ImageUtil;

import java.io.IOException;
import java.util.List;

public class DrawingSurfaceView extends SurfaceView implements Callback, OnTouchListener {
    private SurfaceHolder mSurfaceHolder;
    private MyDrawThread myDrawThread;
    private MyTrackingThread myTrackingThread;
    private List<TrackObject> objects;
    private Rect COCO_RECT;
    private boolean isShowRect = true;

    public List<TrackObject> getObjects() {
        return objects;
    }

    public void setObjects(List<TrackObject> objects) {
        if (this.objects != null) {
            synchronized (this.objects) {
                this.objects = objects;
            }
        } else {
            this.objects = objects;
        }
    }

    public DrawingSurfaceView(Context context) {
        super(context);
        constructor(context);
    }

    public DrawingSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        constructor(context);
    }

    public DrawingSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructor(context);
    }

    private void constructor(Context context) {
        mSurfaceHolder = getHolder();
        setZOrderOnTop(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mSurfaceHolder.addCallback(this);
        this.setOnTouchListener(this);
        myDrawThread = new MyDrawThread(mSurfaceHolder);
        myTrackingThread = new MyTrackingThread(mSurfaceHolder, context);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        myDrawThread.isRun = true;
        new Thread(myDrawThread).start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        myDrawThread.isRun = false;
        myTrackingThread.isRun = false;
    }

    private class MyTrackingThread implements Runnable {
        private SurfaceHolder mHolder;
        public boolean isRun = false;
        private byte direction = 0, pre_diretion = 0;//Right:0,Left:1
        private int x = -1, y = getHeight() >> 1;
        private String[] left = null;
        private String[] right = null;
        private String[] coco_cola = null;
        private Context context = null;
        private static final int walking_begin = 9, walking_end = 53;
        private static final int stop_begin = 54, stop_end = 60;
        private int walking_index = walking_begin, stop_index = stop_begin;
        private byte comfrom = 0;
        private Bitmap frame = null, coco_cola_bit = null;
        private boolean showingCola = false;
        private static final int COLA_BEGIN = 0, COLA_END = 34;
        private int showColaIndex = COLA_BEGIN;
        private Paint mPaint = null;

        public MyTrackingThread(SurfaceHolder holder, Context context) {
            mHolder = holder;
            this.context = context;
            mPaint = new Paint();
            mPaint.setColor(Color.WHITE);
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(6);
            mPaint.setStyle(Style.STROKE);
        }

        private void loadPath() {
            try {
                left = context.getAssets().list("left");
                right = context.getAssets().list("right");
                coco_cola = context.getAssets().list("coco_cola");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            loadPath();
            while (isRun) {
                Canvas mCanvas = null;
                try {
                    long beforeRead = System.currentTimeMillis();
                    synchronized (mHolder) {
                        mCanvas = mHolder.lockCanvas();
                    }
                    mCanvas.drawColor(Color.TRANSPARENT);
                    mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
                    if (objects != null && objects.size() > 0) {
                        int width = getWidth();
                        int height = getHeight();
                        int obj_x = (int) ((objects.get(0).left + objects.get(0).width / 2) * width);
                        int obj_y = (int) ((objects.get(0).top + objects.get(0).height / 2) * height);
                        if (Math.abs(x - obj_x) < 20 && Math.abs(y - obj_y) < 20) {
                            direction = 2;
                        } else if (x < obj_x) {
                            direction = 0;
                        } else if (x > obj_x) {
                            direction = 1;
                        }
                        x += (obj_x - x) / 10;
                        y += (obj_y - y) / 10;
                    }
                    if (direction == 0) {
                        if (pre_diretion != direction) {
                            walking_index = walking_begin;
                        }
                        frame = ImageUtil.readBitmapFromAssets(context, "right/" + right[walking_index]);
                    } else if (direction == 1) {
                        if (pre_diretion != direction) {
                            walking_index = walking_begin;
                        }
                        frame = ImageUtil.readBitmapFromAssets(context, "left/" + left[walking_index]);
                    } else if (direction == 2) {
                        if (pre_diretion != direction) {
                            stop_index = stop_begin;
                            comfrom = pre_diretion;
                        }
                        if (comfrom == 0) {
                            frame = ImageUtil.readBitmapFromAssets(context, "right/" + right[stop_index]);
                        } else if (comfrom == 1) {
                            frame = ImageUtil.readBitmapFromAssets(context, "left/" + left[stop_index]);
                        }
                    }
                    if (direction == 2) {
                        stop_index++;
                        if (stop_index > stop_end) {
                            stop_index = stop_end;
                        } else if (stop_index < stop_begin) {
                            stop_index = stop_end;
                        }
                    } else {
                        walking_index++;
                        if (walking_index > walking_end) {
                            walking_index = walking_begin;
                        } else if (walking_index < walking_begin) {
                            walking_index = walking_end;
                        }
                    }
                    if (frame != null && x >= 0) {
                        if (x < 0) {
                            x = 0;
                        } else if (x > getWidth() - frame.getWidth()) {
                            x = getWidth() - frame.getWidth();
                        }
                        if (y < 0) {
                            y = 0;
                        } else if (y > getHeight() - frame.getHeight()) {
                            y = getHeight() - frame.getHeight();
                        }

                        Rect mDestRect = new Rect(x - frame.getWidth() / 2, y - frame.getHeight() / 2, x + frame.getWidth() / 2, y + frame.getWidth() / 2);
                        mCanvas.drawBitmap(frame, null, mDestRect, null);
                    } else {
                        Log.d("onDraw...", "frame is null");
                    }
                    if (direction == 2 && stop_index == stop_end) {
                        showingCola = true;
                    }
                    //show coco cola
                    if (showingCola) {
                        coco_cola_bit = ImageUtil.readBitmapFromAssets(context, "coco_cola/" + coco_cola[showColaIndex]);
                        showColaIndex++;
                        if (showColaIndex == COLA_END) {
                            showingCola = false;
                            showColaIndex = COLA_BEGIN;
                        }
                        if (coco_cola_bit != null) {
                            int coco_left = x + frame.getWidth() / 2;
                            if (coco_left + coco_cola_bit.getWidth() > getWidth()) {
                                coco_left = getWidth() - coco_cola_bit.getWidth();
                            }

                            int coco_top = y - coco_cola_bit.getWidth();
                            int coco_right = coco_left + coco_cola_bit.getWidth();
                            int coco_bottom = coco_top + coco_cola_bit.getHeight();
                            COCO_RECT = new Rect(coco_left, coco_top, coco_right, coco_bottom);
                            mCanvas.drawBitmap(coco_cola_bit, null, COCO_RECT, null);
                        }
                    } else {
                        COCO_RECT = null;
                    }
                    pre_diretion = direction;

                    if (isShowRect) {
                        int width = getWidth();
                        int height = getHeight();
                        if (objects != null) {
                            for (int index = 0; index < objects.size(); index++) {
                                TrackObject obj = objects.get(index);
                                mCanvas.drawRect(new RectF(obj.left * width, obj.top * height, (obj.left + obj.width) * width, (obj.top + obj.height) * height), mPaint);
                            }
                        }
                    }

                    long afterRead = System.currentTimeMillis();
                    long sleepTime = 40 - (afterRead - beforeRead);

                    if (sleepTime > 0) {
                        //Thread.sleep(sleepTime);
                    }
                } catch (Exception e) {

                } finally {
                    if (mCanvas != null) {
                        synchronized (mHolder) {
                            mHolder.unlockCanvasAndPost(mCanvas);
                        }
                    }
                }
            }//end of while(isRun)
        }
    }

    private class MyDrawThread implements Runnable {
        private SurfaceHolder mHolder;
        public boolean isRun = false;
        private Paint mPaint;

        public MyDrawThread(SurfaceHolder holder) {
            this.mHolder = holder;
            mPaint = new Paint();
            mPaint.setColor(Color.WHITE);
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(6);
            mPaint.setStyle(Style.STROKE);
            mPaint.setTextSize(40);
        }

        @Override
        public void run() {
            while (isRun) {
                Canvas mCanvas = null;
                try {
                    synchronized (mHolder) {
                        mCanvas = mHolder.lockCanvas();
                    }

                    mCanvas.drawColor(Color.TRANSPARENT);
                    mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
                    int width = getWidth();
                    int height = getHeight();
                    if (objects != null) {
                        for (int i = 0; i < objects.size(); i++) {
                            TrackObject obj = objects.get(i);
                            mCanvas.drawRect(new RectF(obj.left * width, obj.top * height, (obj.left + obj.width) * width, (obj.top + obj.height) * height), mPaint);
                            mCanvas.drawText(obj.getName(), obj.left * width, obj.top * height, mPaint);
                        }
                    }

                } catch (Exception e) {

                } finally {
                    if (mCanvas != null) {
                        synchronized (mHolder) {
                            mHolder.unlockCanvasAndPost(mCanvas);
                        }
                    }
                }
            }
        }
    }

    private float[] startPoints = new float[2];
    private byte mStatus = 0;//0:None, 1:Down

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mStatus = 1;
                startPoints[0] = event.getX();
                startPoints[1] = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (mStatus == 1) {
                    if (COCO_RECT != null) {
                        float end_pointX = event.getX();
                        float end_pointY = event.getY();
                        if (isInRect(end_pointX, end_pointY, COCO_RECT)) {
                            float dist = (startPoints[0] - end_pointX) * (startPoints[0] - end_pointX) + (startPoints[1] - end_pointY) * (startPoints[1] - end_pointY);
                            if (dist < 10) {
//                                Intent intent = new Intent(getContext(), BuyItemWVActivity.class);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                intent.putExtra(ConstantsUtil.ITEM_URL, "http://aa5d.com/h.bfkszf?cv=AAQBmOGO&sm=59b6bb");
//                                getContext().startActivity(intent);
                            }
                        }
                    }
                    mStatus = 0;
                }
                break;
        }
        return true;
    }

    private boolean isInRect(float x, float y, Rect rect) {
        boolean inRect = true;
        if (x < rect.left || x > rect.right) {
            inRect = false;
        }
        if (y > rect.bottom || y < rect.top) {
            inRect = false;
        }
        return inRect;
    }

}
