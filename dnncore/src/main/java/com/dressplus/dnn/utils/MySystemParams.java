package com.dressplus.dnn.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class MySystemParams {
	public static final int SCREEN_ORIENTATION_HORIZONTAL = 2;
	public static final int SCREEN_ORIENTATION_VERTICAL = 1;
	private static MySystemParams params;
	private final String TAG = "SystemParams";
	public int densityDpi;
	public float fontScale;
	public float scale;
	public int screenHeight;
	public int screenOrientation;
	public int screenWidth;
	private final static Object singleLock=new Object();
	public static MySystemParams getInstance() {
		if (params == null)
			synchronized (singleLock) {
				if(params==null){
					params = new MySystemParams();
				}
			}	
		return params;
	}

	public void init(Context mContext) {
		DisplayMetrics mDisplayMetrics = mContext.getApplicationContext().getResources().getDisplayMetrics();
		screenWidth = mDisplayMetrics.widthPixels;
		screenHeight = mDisplayMetrics.heightPixels;
		densityDpi = mDisplayMetrics.densityDpi;
		scale = mDisplayMetrics.density;
		fontScale = mDisplayMetrics.scaledDensity;
		if (screenHeight > screenWidth) {
			screenOrientation = SCREEN_ORIENTATION_VERTICAL;
		}else{
			screenOrientation = SCREEN_ORIENTATION_HORIZONTAL;
		}
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public int getScreenWidth() {
		return screenWidth;
	}
}
