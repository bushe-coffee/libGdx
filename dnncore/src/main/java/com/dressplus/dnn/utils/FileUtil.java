package com.dressplus.dnn.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
	private static final String TAG="FileUtil";
	private static final File parentPath = Environment.getExternalStorageDirectory();
	private static String storagePath = "";
	private static final String DST_FOLDER_NAME = "PlayCamera"; 
	public static final byte YUV=0;
	/**
	 * initialize the path
	 * @return
	 */
	private static String initPath(){  
        if(storagePath.equals("")){  
            storagePath = parentPath.getAbsolutePath()+"/" + DST_FOLDER_NAME;  
            File f = new File(storagePath);  
            if(!f.exists()){  
                f.mkdir();  
            }  
        }  
        return storagePath;  
    }
	/**
	 * get the absolute path of the path
	 * @param path
	 * @return
	 */
	public static String getPath(String path){
		File file=new File(parentPath, path);
		if(file.exists()){
			return file.getAbsolutePath();
		}else{
			return null;
		}
	}
	/**
	 * save bitmap to sdcard
	 * @param b
	 */
	public static void saveBitmap(Bitmap b){
        String path = initPath();  
        long dataTake = System.currentTimeMillis();  
        String jpegName = path + "/" + dataTake +".jpg";  
        Log.i(TAG, "saveBitmap:jpegName = " + jpegName);  
        try {  
            FileOutputStream fout = new FileOutputStream(jpegName);  
            BufferedOutputStream bos = new BufferedOutputStream(fout);  
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);  
            bos.flush();  
            bos.close();  
            Log.i(TAG, "saveBitmap successful");  
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            Log.i(TAG, "saveBitmap failed");  
            e.printStackTrace();  
        }  
  
    }
	/**
	 * get file path using uri
	 * @param context
	 * @param uri
	 * @return
	 */
	public static String getFilePath(Activity context, Uri uri) {
		String uriString = uri.toString();
		if(uriString.startsWith("content://")) {
			String[] proj = { MediaColumns.DATA };
			@SuppressWarnings("deprecation")
			Cursor actualimagecursor = context.managedQuery(uri, proj, null, null, null);
			int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaColumns.DATA);
			actualimagecursor.moveToFirst();
			String filePath = actualimagecursor.getString(actual_image_column_index);
			return filePath;
		}else if(uriString.startsWith("file:///")) {
			return uriString.substring(6);
		}else {
			return null;
		}
	}
	/**
	 * save image byte 
	 * @param data
	 * @param width
	 * @param height
	 * @param mode--YUV:0
	 */
	public static void saveImageFromByte(byte[] data,int width,int height,byte mode){
		byte[] saving_data=data.clone();
		if(mode==YUV){
			ByteArrayOutputStream os=new ByteArrayOutputStream(data.length);
			final YuvImage mYuvImage=new YuvImage(data, ImageFormat.NV21, width, height, null);
			if(!mYuvImage.compressToJpeg(new Rect(0,0,width,height), 100, os)){
				return;
			}
			saving_data=os.toByteArray();
		}
		Bitmap bitmap=BitmapFactory.decodeByteArray(saving_data, 0, saving_data.length);
		saveBitmap(bitmap);
		if(bitmap!=null){
			bitmap.recycle();
			bitmap=null;
			saving_data=null;
		}
	}
	/**
	 * copy files from root (assets dir) to desc (sdcard) using recursive
	 * @param context
	 * @param root
	 * @param desc
	 */
	public static void CopyFilesFromAssets(Context context,String root,String desc){
		String filenames[];
		InputStream is=null;
		FileOutputStream fos=null;
		try {
			filenames = context.getAssets().list(root);
			if(filenames!=null&&filenames.length>0){
				File destDir=new File(parentPath,desc);
				if(!destDir.exists()){
					destDir.mkdirs();
				}
				for(String sub_file:filenames){
					CopyFilesFromAssets(context, root+"/"+sub_file, desc+"/"+sub_file);
				}
			}else{
				is=context.getAssets().open(root);
				fos=new FileOutputStream(new File(parentPath,desc));
				byte[] buffer=new byte[1024];
				int byteCount=0;
				while((byteCount=is.read(buffer))!=-1){
					fos.write(buffer, 0, byteCount);
				}
				fos.flush();
				is.close();
				fos.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try{
				if(is!=null){
					is.close();
				}
				if(fos!=null){
					fos.close();
				}
			}catch(Exception e){
				
			}
		}
	}
}
