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
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Display;
import android.view.WindowManager;
import com.android.camera.Log;
import javax.microedition.khronos.egl.EGLConfig; 
import javax.microedition.khronos.opengles.GL10; 


import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.android.camera.manager.ModePicker;
import com.android.prize.IBuzzyStrategy;
import com.gangyun.camera.GYSurfaceView;

/**
 * The main accessor for GPUImage functionality. This class helps to do common
 * tasks through a simple interface.
 */
public class GYImage {
	 private static final String TAG = "GYLog GYImage";	 
    private final Context mContext;
    private final GYImageRenderer mRenderer;
    private GYSurfaceView mGlSurfaceView;
    private GYImageFilter mFilter;
    private Bitmap mCurrentBitmap;
    private ScaleType mScaleType = ScaleType.CENTER_CROP;

    /**
     * Instantiates a new GPUImage object.
     *
     * @param context the context
     */
    public void SetScanType(final int nType)
    {
    	mRenderer.m_nCurMode = nType;
    }
    
    public void SetCameraID(final int nID)
    {
    	mRenderer.m_nCameraID = nID;
    }

	 public void SetCameraYUVMode(final int nID)
    {
    	mRenderer.mnYUVMode = nID;
    }
		
    public void SetDegree(final int degrees)
    {
    	mRenderer.m_nCurDegree = degrees;
		Rotation rotation = Rotation.NORMAL;
        switch (degrees) {
            case 90:
                rotation = Rotation.ROTATION_90;
                break;
            case 180:
                rotation = Rotation.ROTATION_180;
                break;
            case 270:
                rotation = Rotation.ROTATION_270;
                break;
        }
		mRenderer.setRotation(rotation);
    }
    
    public void setPos(final int nx,final int ny)
    {
    	mRenderer.setPos(nx,ny);    	 
    }
    

    public void setParameter(int radius,int level,int nBoderPower){
	     mRenderer.setParameter(radius,level,nBoderPower);  
	}
	
	 public void gyRelease(){
	 Log.i(TAG,"  gyRelease ");
	  if (mGlSurfaceView != null){
		mRenderer.stopPreview();
		mGlSurfaceView.setRenderer(null);
		mGlSurfaceView = null;
	  }
	}
	
    public GYImage(final Context context) {
        if (!supportsOpenGLES2(context)) {
            throw new IllegalStateException("OpenGL ES 2.0 is not supported on this phone.");
        }
        Log.i(TAG,"  GYImage ");
        mContext = context;
        mFilter = new GYImageFilter();
        mRenderer = new GYImageRenderer(mFilter);
        mRenderer.mContext = mContext;
    }

    /**
     * Checks if OpenGL ES 2.0 is supported on the current device.
     *
     * @param context the context
     * @return true, if successful
     */
    private boolean supportsOpenGLES2(final Context context) {
        final ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }

    /**
     * Sets the GLSurfaceView which will display the preview.
     *
     * @param view the GLSurfaceView
     */
    public void setGLSurfaceView(final GYSurfaceView view) {
		 Log.i(TAG,"  setGLSurfaceView ");
		mGlSurfaceView = view;
		mGlSurfaceView.setEGLContextClientVersion(2);
		mGlSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		mGlSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);

		mGlSurfaceView.setRenderer(mRenderer);
		mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		mGlSurfaceView.requestRender();
    }

    /**
     * Request the preview to be rendered again.
     */
    public void requestRender() {
        if (mGlSurfaceView != null) {
            mGlSurfaceView.requestRender();
        }
    }

    /**
     * Sets the up camera to be connected to GPUImage to get a filtered preview.
     *
     * @param camera the camera
     */
    public void setUpCamera(final Camera camera, ModePicker mModePicker, IBuzzyStrategy mBuzzyStrategy) {
        setUpCamera(camera, 0, false, false, mModePicker, mBuzzyStrategy);
    }

    /**
     * Sets the up camera to be connected to GPUImage to get a filtered preview.
     *
     * @param camera the camera
     * @param degrees by how many degrees the image should be rotated
     * @param flipHorizontal if the image should be flipped horizontally
     * @param flipVertical if the image should be flipped vertically
     */
    public void setUpCamera(final Camera camera, final int degrees, final boolean flipHorizontal,
            final boolean flipVertical, ModePicker mModePicker, IBuzzyStrategy mBuzzyStrategy) {
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
			Log.i(TAG,"  setUpCamera ");
            setUpCameraGingerbread(camera, mModePicker, mBuzzyStrategy);
        } else {
            //camera.setPreviewCallback(mRenderer);
            //camera.startPreview();
        }
        Rotation rotation = Rotation.NORMAL;	
        switch (degrees) {
            case 90:
                rotation = Rotation.ROTATION_90;
                break;
            case 180:
                rotation = Rotation.ROTATION_180;
                break;
            case 270:
                rotation = Rotation.ROTATION_270;
                break;
        }
        mRenderer.setRotationCamera(rotation, flipHorizontal, flipVertical);
    }

	public void setGyImagePreviewCallback(final Camera camera){
		 mRenderer.setGyImagePreviewCallback(camera, mGlSurfaceView);
	}

    @TargetApi(11)
    private void setUpCameraGingerbread(final Camera camera, ModePicker mModePicker, IBuzzyStrategy mBuzzyStrategy) {
        Log.i(TAG,"  setUpCameraGingerbread ");
        mRenderer.setUpSurfaceTexture(camera, mGlSurfaceView, mModePicker, mBuzzyStrategy);
    }

    /**
     * Sets the filter which should be applied to the image which was (or will
     * be) set by setImage(...).
     *
     * @param filter the new filter
     */
    public void setFilter(final GYImageFilter filter) {
        mFilter = filter;
        mRenderer.setFilter(mFilter);
        requestRender();
    }

    /**
     * Sets the image on which the filter should be applied.
     *
     * @param bitmap the new image
     */
    public void setImage(final Bitmap bitmap) {
        mCurrentBitmap = bitmap;
        mRenderer.setImageBitmap(bitmap, false);
        requestRender();
    }


    /**
     * Sets the rotation of the displayed image.
     *
     * @param rotation new rotation
     */
    public void setRotation(Rotation rotation) {
        mRenderer.setRotation(rotation);
    }

    /**
     * Sets the rotation of the displayed image with flip options.
     *
     * @param rotation new rotation
     */
    public void setRotation(Rotation rotation, boolean flipHorizontal, boolean flipVertical) {
        mRenderer.setRotation(rotation, flipHorizontal, flipVertical);
    }


    public void gyImagestop() {
		Log.i(TAG, "gyImagestop");
        mRenderer.stopPreview();
	   try
		{
		   mGlSurfaceView.removeCallbacks(null);
		}catch(RuntimeException e){
			//
		}
    }
	
	
	public int getPosX(){
		return mRenderer.getPosX();
	}
	
	public int getPosY(){
		return mRenderer.getPosY();
	}



    /**
     * Runs the given Runnable on the OpenGL thread.
     *
     * @param runnable The runnable to be run on the OpenGL thread.
     */
    void runOnGLThread(Runnable runnable) {
        mRenderer.runOnDrawEnd(runnable);
    }

    public int getOutputWidth() {
        if (mRenderer != null && mRenderer.getFrameWidth() != 0) {
            return mRenderer.getFrameWidth();
        } else if (mCurrentBitmap != null) {
            return mCurrentBitmap.getWidth();
        } else {
            WindowManager windowManager =
                    (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            return display.getWidth();
        }
    }

    public int getOutputHeight() {
        if (mRenderer != null && mRenderer.getFrameHeight() != 0) {
            return mRenderer.getFrameHeight();
        } else if (mCurrentBitmap != null) {
            return mCurrentBitmap.getHeight();
        } else {
            WindowManager windowManager =
                    (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            return display.getHeight();
        }
    }

    @Deprecated
    private class SaveTask extends AsyncTask<Void, Void, Void> {

        private final Bitmap mBitmap;
        private final String mFolderName;
        private final String mFileName;
        //private final OnPictureSavedListener mListener;
        private final Handler mHandler;

        public SaveTask(final Bitmap bitmap, final String folderName, final String fileName) {
            mBitmap = bitmap;
            mFolderName = folderName;
            mFileName = fileName;
            //mListener = listener;
            mHandler = new Handler();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            Bitmap result = null;//getBitmapWithFilterApplied(mBitmap);
            saveImage(mFolderName, mFileName, result);
            return null;
        }

        private void saveImage(final String folderName, final String fileName, final Bitmap image) {
            File path = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File file = new File(path, folderName + "/" + fileName);
            try {
                file.getParentFile().mkdirs();
                image.compress(CompressFormat.JPEG, 80, new FileOutputStream(file));
                MediaScannerConnection.scanFile(mContext,
                        new String[] {
                            file.toString()
                        }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(final String path, final Uri uri) {
								/*
                                if (mListener != null) {
                                    mHandler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            mListener.onPictureSaved(uri);
                                        }
                                    });
                                }
								*/
                            }
                        });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public enum ScaleType { CENTER_INSIDE, CENTER_CROP }
}
