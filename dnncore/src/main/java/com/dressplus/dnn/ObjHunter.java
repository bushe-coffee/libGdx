package com.dressplus.dnn;

public class ObjHunter {
	private static ObjHunter mInstance;
	private static Object singleLock=new Object();
	
	/**
	 * get the instance of ObjHunter
	 * @return
	 */
	public static ObjHunter getInstance(){
		if(mInstance==null){
			synchronized (singleLock) {
				if(mInstance==null){
					mInstance=new ObjHunter();
				}//end of if(mInstance==null)
			}//end of synchronized (singleLock)
		}//end of if(mInstance==null)
		return mInstance;
	}
	/**
	 * constructor
	 */
	static {
		System.loadLibrary("caffe");
		System.loadLibrary("dressplus_objhunter_jni");
	}
	/**
	 * @param modelPath
	 * @param rotateCode: 0=no Rotate, 1=CW, 2=180, 3=CCW
	 * @return
	 */
	public native int initDetector(String modelPath, int rotateCode);
	/**
	 * @param dataArray:0:jpeg 1: yuv; 2:rgba
	 * @param dataType
	 * @param width
	 * @param height
	 * @return
	 */
	public native String objdetector(byte[] dataArray, int dataType, int width, int height);
	/**
	 * 
	 * @param dataArray
	 * @param dataType: 0:jpeg 1: yuv; 2:rgba
	 * @param rotateCode: 0=no Rotate, 1=CW, 2=180, 3=CCW
	 * @param width
	 * @param height
	 * @param objRects
	 * @return
	 */
	public native int initTracker(byte[] dataArray, int dataType, int rotateCode, int width, int height,float[] objRects);
	/**
	 * 
	 * @param dataArray
	 * @param dataType 0:jpeg 1: yuv; 2:rgba
	 * @param width
	 * @param height
	 * @return
	 */
	public native String objtracker(byte[] dataArray, int dataType, int width, int height);
}
