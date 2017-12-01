package com.dressplus.dnn.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUtil {
	/**
	 * rotate image of rotateDegree degree
	 * @param b
	 * @param rotateDegree
	 * @return
	 */
	public static Bitmap getRotateBitmap(Bitmap b, float rotateDegree){
        Matrix matrix = new Matrix();
        matrix.postRotate((float)rotateDegree);  
        Bitmap rotaBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
        return rotaBitmap;  
    }  
	/**
	 * get bitmap from assets folder using resource id
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Bitmap readBitmap(Context context, int resId){
		BitmapFactory.Options mOptions=new BitmapFactory.Options();
		mOptions.inPreferredConfig= Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeStream(context.getResources().openRawResource(resId), null, mOptions);
	}
	/**
	 * 
	 * @param context
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static Bitmap readBitmapFromAssets(Context context, String path) throws IOException {
		BitmapFactory.Options mOptions=new BitmapFactory.Options();
		mOptions.inPreferredConfig= Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeStream(context.getAssets().open(path),null,mOptions);
	}
	public static byte[] getScaledBitmapData(byte[] data,int scale_param,int facing,int orientation){
		BitmapFactory.Options opts = new BitmapFactory.Options();
    	opts.inJustDecodeBounds = true;
		Bitmap mBitmap=null;
		if(null==data) return null;
		mBitmap= BitmapFactory.decodeByteArray(data, 0, data.length,opts);
		float realWidth=opts.outWidth;
		float realHeight=opts.outHeight;
		opts.inSampleSize=scale_param;
		opts.inJustDecodeBounds=false;
		mBitmap= BitmapFactory.decodeByteArray(data, 0, data.length,opts);
		if(null==mBitmap) return null;
		Bitmap rotateBitmap=null;
		/*if(facing==Camera.CameraInfo.CAMERA_FACING_BACK){
			rotateBitmap=ImageUtil.getRotateBitmap(mBitmap, orientation);
		}else{
			rotateBitmap=ImageUtil.getRotateBitmap(mBitmap, 270f);
		}*/
		rotateBitmap=ImageUtil.getRotateBitmap(mBitmap, orientation);
		int size=rotateBitmap.getRowBytes()*rotateBitmap.getHeight();
		if(size>614400){//size > 600K
			int dstHeight=224;
			int dstWidth=(int) (dstHeight*((float)rotateBitmap.getWidth()/(float)rotateBitmap.getHeight()));
			rotateBitmap= Bitmap.createScaledBitmap(rotateBitmap, dstWidth, dstHeight, true);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		rotateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] result=baos.toByteArray().clone();
		if(mBitmap!=null){
			mBitmap.recycle();
			mBitmap=null;
		}
		if(rotateBitmap!=null){
			rotateBitmap.recycle();
			rotateBitmap=null;
		}
		try {
			baos.flush();
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}
