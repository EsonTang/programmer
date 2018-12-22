package com.gangyun.camera;

import android.content.Context;
import android.graphics.Bitmap;

public class LibVGestureDetect {

	   static
	    {
	        System.loadLibrary("BlurModoule");
	    }

	    /**
	     * V字手势识别
	     * @param width  yuv宽
	     * @param height  yuv 高
	     * @param yuv yuv数据
	     * @param back_front 0后置镜头  1 前置镜头
	     * nSkinMode 肤色模型0 1 2
	     * @return
	     */
	   
	    public static native boolean DetectVGesture(int width, int height, byte[] yuv, int back_front,int degree,int face_flag,int nSkinMode);
	    public static native void YUVtoRBGA(byte[] yuv420sp, int width, int height, int[] rgbOut);
	 
	    public static native boolean SetDebug(int nMode);
	    
	    public static native  String  GetSignData(Context ctx);
	    
	    public static native boolean FocusBlurEffect(int width, int height, byte[] yuv, int nx,int ny,int nRadiu,int nPower,int nSkipNums);
	    public static native int FocusBlurEffectWithOut(int width, int height, byte[] yuv, int nx,int ny,int nRadiu,int nPower,int nBoderPower, int[] rgbOut);
	    public static native boolean TextDetect(int width, int height, byte[] yuv, int back_front,int degree,int face_flag,int nSkinMode);
	    public static native int InitFilter(int nw,int nh,int nMode);

       
		public static native int FocusBlurEffectMain(int width, int height, byte[] yuv, byte[] u,byte[] v);
		public static native void SetBlurPara(int nx,int ny,int nRadiu,int nPower,int nBoderPower);
		//nMode	 enum ImageDataMode { IMAGEMODE_YUV420P=0, IMAGEMODE_YUV422=1, IMAGEMODE_YUV420SP=2,IMAGEMODE_RGB,IMAGEMODE_BGR,IMAGEMODE_RGBA,IMAGEMODE_BGRA};
		public static native int InitBlurData(int width,int height,int nMode);//返回值0错误，1：CPU，2：GPU
		public static native boolean FreeBlurPara();
		public static native void SetViewPort(int nViewWidth,int nViewHeight,int nRotation,int bFilpH,int bFilpV,int scType);
		public static native int PorcessImage(Bitmap inbmp,int nx,int ny,int nRadiu,int nPower,int nBorderPower);
		public static native void CurState(int bSetData,	int  inOutBuf[]);
		public static native int FocusBlurEffectOES(int nTextureOES);
		public static native int YUV420PtoRBGA(byte[] yuv420sp, int width, int height, int[] rgbOut,byte[] u,byte[] v);
       
}
