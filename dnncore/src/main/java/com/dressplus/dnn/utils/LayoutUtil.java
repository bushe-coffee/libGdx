package com.dressplus.dnn.utils;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class LayoutUtil {
	public static void initViewParams(View view, int width, int height){
		LayoutParams params = view.getLayoutParams();
		if(params==null) return;
		if(width!=-1){
			params.width=width;
		}
		if(height!=-1){
			params.height=height;
		}
		view.setLayoutParams(params);
	}
	
	public static void initViewParams(View view, float scale){
		LayoutParams params = view.getLayoutParams();
		if(params==null) return;
		int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		view.measure(width,height);
		params.width=(int) (view.getMeasuredWidth()*scale);
		params.height=(int) (view.getMeasuredHeight()*scale);
		view.setLayoutParams(params);
	}
	
	public static void setViewMargin(View view, int left, int top, int right, int bottom){
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
		params.setMargins(left, top, right, bottom);
		view.setLayoutParams(params);
	}
	public static void setViewMarginOfLinearLayout(View view, int left, int top, int right, int bottom){
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
		params.setMargins(left, top, right, bottom);
		view.setLayoutParams(params);
	}
	public static int[] getViewWidthAndHeight(View view){
		int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		view.measure(width,height);
		return new int[]{view.getMeasuredWidth(),view.getMeasuredHeight()};
	}
}
