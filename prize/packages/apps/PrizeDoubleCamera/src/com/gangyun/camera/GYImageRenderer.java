/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gangyun.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.android.camera.Log;
import com.android.camera.manager.ModePicker;
import com.android.prize.DoubleBuzzyStrategy;
import com.android.prize.IBuzzyStrategy;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.gangyun.camera.TextureRotationUtil.TEXTURE_NO_ROTATION;
 
@TargetApi(11)
public class GYImageRenderer implements Renderer, PreviewCallback {

    private static final String TAG = "GYLog GYImageRenderer";	 
    public static final int NO_IMAGE = -1;
    static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };
   public Context mContext=null;
   public int m_nCurMode=3;
   public int m_nCurDegree=0;
   public int m_nCameraID =0;
   private int m_nX=-1,m_nY=-1;
    private int m_level=60,m_radius=40,m_nBoderPower=30;
    private GYImageFilter mFilter;

    public final Object mSurfaceChangedWaiter = new Object();

    private int mGLTextureId = NO_IMAGE;
    private SurfaceTexture mSurfaceTexture = null;
    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;
    private IntBuffer mGLRgbBuffer;
    private int   mTextureRetId[] ={0,0};
    private  byte[] mYUVdata;
    private int mYUVWidth,mYUVHeight;
    
    private int mOutputWidth;
    private int mOutputHeight;
    private int mImageWidth;
    private int mImageHeight;
    private int mAddedPadding;
	private int nInited;

    private final Queue<Runnable> mRunOnDraw;
    private final Queue<Runnable> mRunOnDrawEnd;
    private Rotation mRotation;
    private boolean mFlipHorizontal;
    private boolean mFlipVertical;
    private GYImage.ScaleType mScaleType = GYImage.ScaleType.CENTER_CROP;
    public int mnYUVMode=0;//{ IMAGEMODE_YUV420P=0, IMAGEMODE_YUV422=1, IMAGEMODE_YUV420SP=2,IMAGEMODE_RGB,IMAGEMODE_BGR,IMAGEMODE_RGBA,IMAGEMODE_BGRA}
    private int mnCoreProcessMode = 0;
    private GYSurfaceView mGLSurfaceView;
	private int mGLTextureOESId = NO_IMAGE;
    private boolean isParameterChange =false;
	private int gysize[] ={0,0,0,0,0,0,0,0,0,0,0};// width , high,x,y,radius,level,scope ,format,Rotation,isFont
  
	private ModePicker mModePicker;
	private DoubleBuzzyStrategy mBuzzyStrategy;
    public void setPos( int nx, int ny)
    {
    	 
    	 float outputWidth = mOutputWidth;
         float outputHeight = mOutputHeight;
        if (mRotation == Rotation.ROTATION_270 || mRotation == Rotation.ROTATION_90) {
        	
             outputWidth = mOutputHeight;
             outputHeight = mOutputWidth;
             int nTemp;
             nTemp =ny;
             ny= nx;
             nx=nTemp;
         }
        

         float ratio1 = outputWidth / mImageWidth;
         float ratio2 = outputHeight / mImageHeight;
         float ratioMax = Math.max(ratio1, ratio2);
       
		 Log.i(TAG, "setPos ratio:" + ratio1 + "," + ratio2 + "," + ratioMax);
	     m_nX=  Math.round(nx/ratioMax);
	     m_nY=  Math.round( ny/ratioMax);
        //PRIZE-modify-preview focus point doesn't match with the tap point-20170310-pengcancan-start
        m_nY = mImageHeight - m_nY;
        //FYI, m_nX, m_nY are for the preview side, when preview focus point is not right,
        //you should try to change these two params, m_nX is actual for up-down side, and m_nY for left-right side
        //gysize is the focus point of the picture you take, and gysize[2] is for up-down side, gysize[3] is for left-right side

	   gysize[2] = m_nX;
	   gysize[3] = m_nY;
        //PRIZE-modify-preview focus point doesn't match with the tap point-20170310-pengcancan-end
	   LibVGestureDetect.CurState(1,gysize);
       isParameterChange = true;
       Log.i(TAG," SetPos outputWidth:" + outputWidth +","+mImageWidth + "; outputHeight:" + outputHeight+ ","+mImageHeight+"; x: " + m_nX + " y:" + m_nY);
    }
    
    public void gyInitBlurPara(int width, int height, int nx,int ny,int nRadiu,int nPower,int nBoderPower,int nMode){ 		
		    Log.i(TAG," gyInitBlurPara width:" + width +" height:" + height+ "   x:" + nx + " ny:" + ny + " nRadiu:"+nRadiu + " nPower:"+nPower + " nBoderPower:"+nBoderPower + " nMode:" +nMode );
	}
	
	public int getPosX(){
		return m_nX;
	}
	
	public int getPosY(){
		return m_nY;
	}
	
    public void setParameter(int radius,int level,int nBoderPower)
    {
		m_level=level;
		m_radius=radius;
		m_nBoderPower = nBoderPower;
		//LibVGestureDetect.SetBlurPara(x,y,radius,level,nBoderPower);

	
	   gysize[4] = m_radius;
	   gysize[5] = m_level;
	   gysize[6] = m_nBoderPower;
	   LibVGestureDetect.CurState(1,gysize);
	   isParameterChange = true;
       Log.i(TAG, "setParameter level:"+level+" radius:"+radius);
    }
    
    public void gyRelease(){
		Log.i(TAG, "Blur FreeBlurPara");
		 LibVGestureDetect.FreeBlurPara();
	}
	
    public GYImageRenderer(final GYImageFilter filter) {
        mFilter = filter;
        mRunOnDraw = new LinkedList<Runnable>();
        mRunOnDrawEnd = new LinkedList<Runnable>();

        mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        setRotation(Rotation.NORMAL, false, false);
      
    }

    @Override
    public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
        Log.i(TAG,"onSurfaceCreated");
        GLES20.glDisable(GL10.GL_DITHER);
        GLES20.glClearColor(0,0,0,0);
        GLES20.glEnable(GL10.GL_CULL_FACE);
        GLES20.glEnable(GL10.GL_DEPTH_TEST);
        mFilter.init();
    }
	

    @Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
        Log.i(TAG,"onSurfaceChanged width:"+width + " height:"+height);
        mOutputWidth = width;
        mOutputHeight = height;
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(mFilter.getProgram());
        mFilter.onOutputSizeChanged(width, height);
        adjustImageScaling();
        if(mGLRgbBuffer!=null)
           mGLRgbBuffer.clear();
        mGLRgbBuffer=null;        
         
        mYUVdata=null;
        synchronized (mSurfaceChangedWaiter) {
            mSurfaceChangedWaiter.notifyAll();
        }
    }
	

    @Override
    public void onDrawFrame(final GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        runAll(mRunOnDraw);
        
        //Log.i("GYImageRenderer","onDrawFrame"+" width="+ mImageWidth+" height="+ mImageHeight);
        /*
		if (mSurfaceTexture != null) {
			mSurfaceTexture.updateTexImage();
		}else{
			Log.e(TAG, "mSurfaceTexture is null");
		}
		*/

        mFilter.onDraw(mGLTextureId, mGLCubeBuffer, mGLTextureBuffer);


        runAll(mRunOnDrawEnd);

         
    }

    private void runAll(Queue<Runnable> queue) {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        }
    }

     Toast m_toast = null;
    private final Handler msgHandler = new Handler(){
        public void handleMessage(Message msg) {
                switch (msg.arg1) {
                case 1:
                	if(m_toast==null)
                	{
                		m_toast = Toast.makeText(mContext, msg.obj.toString(), Toast.LENGTH_SHORT);
                		m_toast .show();
                	}
                	else
                	{
                		m_toast.setText(msg.obj.toString());
                		m_toast .show();
                	}
                        
                        break;
                default:
                        break;
                }
        }
};

static int nPicPos=0;
public static void saveData( byte[] data,int w,int h,int nV)
{
	 File imageFile=null;
	String fileName = String.format("yuv_%dx%d_v%d_%d.yuv",w,h,nV,nPicPos);
	
	   imageFile = new File(Environment.getExternalStorageDirectory()+"/save/", fileName); 
	   if(imageFile==null)
	     imageFile = new File(Environment.getExternalStorageDirectory(), fileName); 
	   if(imageFile==null)
		   return;
	 nPicPos++;
	 // File imageFile = new File(fileName);
    FileOutputStream fstream = null;
    BufferedOutputStream bStream = null;
    try
    {
        fstream = new FileOutputStream(imageFile);
        bStream = new BufferedOutputStream(fstream);
        bStream.write(data);
    }
    catch (FileNotFoundException e)
    {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    catch (IOException e)
    {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    finally
    {
        if (bStream != null)
        {
            try
            {
                bStream.close();
                bStream = null;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
    static int inited=0;
    String strPrev=null;
   
    static long m_nTimeCount=0;
    static long m_nFpsCount=0;
   
    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
    	
    	/*if(mBuzzyStrategy != null && mBuzzyStrategy.isReadSurfaceData() == true){
    		mBuzzyStrategy.setIsReadSurfaceData(false);
    		mBuzzyStrategy.setMainData(data);
    		synchronized (mBuzzyStrategy) {
    			mBuzzyStrategy.notify();
			}
    		
    	}*/
    	Log.d(TAG, "onPreviewFrame");
		try{
			final Size previewSize = camera.getParameters().getPreviewSize();
			if(mYUVdata == null){
				mYUVdata = new byte[data.length];
				mYUVWidth=previewSize.width;
				mYUVHeight = previewSize.height;
			}
			   if (mGLRgbBuffer == null) {
				mGLRgbBuffer = IntBuffer.allocate(previewSize.width * previewSize.height);
			}
			/*
			try {
				         Log.i("GYImageRenderer","FileOutputStream save");
						 FileOutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + "yuv-"+previewSize.width +"X" +previewSize.height+"-" + ".yuv");
						 output.write(data);
						
						 output.flush();
						 output.close();
					 }catch (IOException e){
						 e.printStackTrace();
					 }
        */
        if (mRunOnDraw.isEmpty()) {
            runOnDraw(new Runnable() {
                @Override
                public void run() {
                	 m_nFpsCount++;
                	 long time = System.currentTimeMillis();
					
                	int[] processId = new int[5];
					if(m_nCurMode == 3)
                	{
                		if(m_nX==-1)
                		{
                			m_nX = previewSize.width/2;
                			m_nY =  previewSize.height/2;
                		}
                	
                		    if( mnCoreProcessMode ==0)
                	        {
                                /*prize-modify-blur function-xiaoping-20170602-start*/
                	        	mnCoreProcessMode = LibVGestureDetect.InitBlurData(mYUVWidth, mYUVHeight, mnYUVMode+0x2000);
                                 /*prize-modify-blur function-xiaoping-20170602-end*/
                	        	Log.i(TAG, "Blur InitBlurData=" + mnCoreProcessMode);
                				m_nX=mYUVWidth/2;
								m_nY = mYUVHeight/2; 
								gysize[2] = m_nX;
								gysize[3] = mYUVHeight - m_nY;
								LibVGestureDetect.CurState(1,gysize); 
                	        }
                	        
                	        if(mnCoreProcessMode == 2)
                	    	{
                            if(isParameterChange){
					         	isParameterChange = false;
					        	LibVGestureDetect.SetBlurPara(m_nX, m_nY, m_radius, m_level, m_nBoderPower);
                              }
                  	    		mGLTextureId= LibVGestureDetect.FocusBlurEffectMain(mYUVWidth, mYUVHeight, data, null, null);
                	    		//Log.i(TAG,"onPreviewFrame"+" Start FocusBlurEffect end mGLTextureId= "+mGLTextureId + " mx:"+m_nX + " m_nY:"+m_nY);

                	    		GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);			 
                				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGLTextureId);
                				camera.addCallbackBuffer(data);
                        		 
                       		  if (mOutputWidth != previewSize.width) {
                                     mImageWidth = previewSize.width;
                                     mImageHeight = previewSize.height;
                                     adjustImageScaling();
                                 }
								return; 
                	    	}
                	        else{
                	        	LibVGestureDetect.YUVtoRBGA(data, previewSize.width, previewSize.height, mGLRgbBuffer.array());
							}
                	}  
                	else
                	{
                		LibVGestureDetect.YUVtoRBGA(data, previewSize.width, previewSize.height, mGLRgbBuffer.array());
                	}
					

/*
                	 m_nTimeCount+=(time1-time);
                	 m_nFpsCount++;
                	 if(m_nFpsCount>=90)
                	 {
						 1000
                		 long nn=90000/m_nTimeCount;
                		  Message msg = msgHandler.obtainMessage();
	                        
	                     	msg.arg1 = 1;
	                     	msg.obj ="Times:" +nn +"FPS"+(time1-time);
	                     	 
	                     	msgHandler.sendMessage(msg);
	                     	m_nTimeCount=0;
	                     	m_nFpsCount=0;
                	 }
                	*/
                	 
                	 if(mTextureRetId[0]>0)
                		 mGLTextureId = 	 mTextureRetId[0];
                	 else
                        mGLTextureId = OpenGlUtils.loadTexture(mGLRgbBuffer, previewSize, mGLTextureId);
                    camera.addCallbackBuffer(data);

                  // Log.i(TAG,"onPreviewFrame"+" width="+ previewSize.width+" height="+ previewSize.height);
                    if (mImageWidth != previewSize.width) {
                        mImageWidth = previewSize.width;
                        mImageHeight = previewSize.height;
                        adjustImageScaling();
                    }
                }
            });
             
        }
		}catch(RuntimeException e){
			//
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}
        
    }

    public void setUpSurfaceTexture(final Camera camera, final GYSurfaceView glSurfaceView, ModePicker mModePicker, IBuzzyStrategy mBuzzyStrategy) {
    	this.mModePicker = mModePicker;
    	if(mBuzzyStrategy instanceof DoubleBuzzyStrategy){
    		this.mBuzzyStrategy = (DoubleBuzzyStrategy) mBuzzyStrategy;
    	}
	
		glSurfaceView.queueEvent(new Runnable() {

			@Override

			public void run() {
			        	Log.i(TAG, "[setPreviewTexture]... gyRelease");
					      gyRelease();
						  mnCoreProcessMode = 0;
						 int[] textures = new int[1];
						  GLES20.glGenTextures(1, textures, 0);
						  mGLTextureOESId = textures[0];
						  mSurfaceTexture = new SurfaceTexture(mGLTextureOESId);
						try {
							Log.i(TAG, "[setPreviewTexture]...");
							camera.setPreviewTexture(mSurfaceTexture);
							camera.setPreviewCallback(GYImageRenderer.this);
							//camera.startPreview();
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
			});
    }

	
    public void setGyImagePreviewCallback(final Camera camera, final GYSurfaceView glSurfaceView) {
		Log.i(TAG, "[setGyImagePreviewCallback]  +mSurfaceTexture:"+mSurfaceTexture);

		try {
			//camera.setPreviewTexture(mSurfaceTexture);			
			camera.setPreviewCallback(GYImageRenderer.this);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void setFilter(final GYImageFilter filter) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                final GYImageFilter oldFilter = mFilter;
                mFilter = filter;
                if (oldFilter != null) {
                    oldFilter.destroy();
                }
                mFilter.init();
                GLES20.glUseProgram(mFilter.getProgram());
                mFilter.onOutputSizeChanged(mOutputWidth, mOutputHeight);
            }
        });
    }

    public void stopPreview() {
			Log.i(TAG, "stopPreview");
			GLES20.glDeleteTextures(1, new int[]{
					mGLTextureOESId
			}, 0);
			mSurfaceTexture = null;
			mGLTextureOESId = NO_IMAGE;
			mGLTextureId = NO_IMAGE;
			mnCoreProcessMode = 0;
			gyRelease();
    }

    public void setImageBitmap(final Bitmap bitmap) {
        setImageBitmap(bitmap, true);
    }

    public void setImageBitmap(final Bitmap bitmap, final boolean recycle) {
        if (bitmap == null) {
            return;
        }

        runOnDraw(new Runnable() {

            @Override
            public void run() {
                Bitmap resizedBitmap = null;
                if (bitmap.getWidth() % 2 == 1) {
                    resizedBitmap = Bitmap.createBitmap(bitmap.getWidth() + 1, bitmap.getHeight(),
                            Bitmap.Config.ARGB_8888);
                    Canvas can = new Canvas(resizedBitmap);
                    can.drawARGB(0x00, 0x00, 0x00, 0x00);
                    can.drawBitmap(bitmap, 0, 0, null);
                    mAddedPadding = 1;
                } else {
                    mAddedPadding = 0;
                }

                mGLTextureId = OpenGlUtils.loadTexture(
                        resizedBitmap != null ? resizedBitmap : bitmap, mGLTextureId, recycle);
                if (resizedBitmap != null) {
                    resizedBitmap.recycle();
                }
                mImageWidth = bitmap.getWidth();
                mImageHeight = bitmap.getHeight();
                adjustImageScaling();
            }
        });
    }

    public void setScaleType(GYImage.ScaleType scaleType) {
        mScaleType = scaleType;
    }

    protected int getFrameWidth() {
        return mOutputWidth;
    }

    protected int getFrameHeight() {
        return mOutputHeight;
    }

    private void adjustImageScaling() {
        float outputWidth = mOutputWidth;
        float outputHeight = mOutputHeight;
        if (mRotation == Rotation.ROTATION_270 || mRotation == Rotation.ROTATION_90) {
            outputWidth = mOutputHeight;
            outputHeight = mOutputWidth;
        }
		//Log.i(TAG, "adjustImageScaling mRotation="+mRotation + " outputWidth:"+outputWidth + " outputHeight:"+outputHeight );

        float ratio1 = outputWidth / mImageWidth;
        float ratio2 = outputHeight / mImageHeight;
        float ratioMax = Math.max(ratio1, ratio2);
        int imageWidthNew = Math.round(mImageWidth * ratioMax);
        int imageHeightNew = Math.round(mImageHeight * ratioMax);

        float ratioWidth = imageWidthNew / outputWidth;
        float ratioHeight = imageHeightNew / outputHeight;

        float[] cube = CUBE;
        float[] textureCords = TextureRotationUtil.getRotation(mRotation, mFlipHorizontal, mFlipVertical);
        if (mScaleType == GYImage.ScaleType.CENTER_CROP) {
            float distHorizontal = (1 - 1 / ratioWidth) / 2;
            float distVertical = (1 - 1 / ratioHeight) / 2;
            textureCords = new float[]{
                    addDistance(textureCords[0], distHorizontal), addDistance(textureCords[1], distVertical),
                    addDistance(textureCords[2], distHorizontal), addDistance(textureCords[3], distVertical),
                    addDistance(textureCords[4], distHorizontal), addDistance(textureCords[5], distVertical),
                    addDistance(textureCords[6], distHorizontal), addDistance(textureCords[7], distVertical),
            };
        } else {
            cube = new float[]{
                    CUBE[0] / ratioHeight, CUBE[1] / ratioWidth,
                    CUBE[2] / ratioHeight, CUBE[3] / ratioWidth,
                    CUBE[4] / ratioHeight, CUBE[5] / ratioWidth,
                    CUBE[6] / ratioHeight, CUBE[7] / ratioWidth,
            };
        }

        mGLCubeBuffer.clear();
        mGLCubeBuffer.put(cube).position(0);
        mGLTextureBuffer.clear();
        mGLTextureBuffer.put(textureCords).position(0);
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    public void setRotationCamera(final Rotation rotation, final boolean flipHorizontal,
            final boolean flipVertical) {
        setRotation(rotation, flipVertical, flipHorizontal);
    }

    public void setRotation(final Rotation rotation) {
        mRotation = rotation;
        adjustImageScaling();
    }

    public void setRotation(final Rotation rotation,
                            final boolean flipHorizontal, final boolean flipVertical) {
        mFlipHorizontal = flipHorizontal;
        mFlipVertical = flipVertical;
        setRotation(rotation);
    }

    public Rotation getRotation() {
        return mRotation;
    }

    public boolean isFlippedHorizontally() {
        return mFlipHorizontal;
    }

    public boolean isFlippedVertically() {
        return mFlipVertical;
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.add(runnable);
        }
    }

    protected void runOnDrawEnd(final Runnable runnable) {
        synchronized (mRunOnDrawEnd) {
            mRunOnDrawEnd.add(runnable);
        }
    }


}
