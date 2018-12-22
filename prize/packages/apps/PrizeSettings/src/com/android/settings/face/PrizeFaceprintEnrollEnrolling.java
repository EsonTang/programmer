package com.android.settings.face;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import pingan.ai.paverify.widget.PaDtcSurfaceView;
import pingan.ai.paverify.mananger.PaFaceDetectorManager;
import pingan.ai.paverify.mananger.impl.IPaFaceDetector;
import pingan.ai.paverify.entity.PAFaceDetectorFrame;
import pingan.ai.paverify.camera.impl.IOpenCameraInfo;


import com.android.settings.R;
import com.android.settings.face.utils.FaceXmlData;
import com.android.settings.face.utils.SaveBitmapUtil;
import com.android.settings.face.utils.SaveListUtil;
import com.android.settings.face.utils.SpUtil;
import com.mediatek.common.prizeoption.PrizeOption;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/17.
 */

public class PrizeFaceprintEnrollEnrolling extends Activity implements IPaFaceDetector, IOpenCameraInfo {

    private ActionBar mActionBar;
    private Button mButton;
    private LinearLayout inputFace, addFace;
    private ImageView scanImageView, scanFrame, face_photo;
    private TextView faceText, faceText2, fail_text;
    private FrameLayout frameLayout;
    private PaDtcSurfaceView mSurfaceView;
    private PaFaceDetectorManager manager;
    private int isAddFace = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_SubSettings);

        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
        }

        setContentView(R.layout.prize_faceprint_enroll_enrolling);

        CharSequence msg = getText(R.string.faceprint_add_title);
        setTitle(msg);

        inputFace = (LinearLayout) findViewById(R.id.input_face);
        addFace = (LinearLayout) findViewById(R.id.adding_face);
        scanImageView = (ImageView) findViewById(R.id.scan_face);
        scanFrame = (ImageView) findViewById(R.id.scan_frame);
        face_photo = (ImageView) findViewById(R.id.face_photo);
        frameLayout = (FrameLayout) findViewById(R.id.face_preview);
        faceText = (TextView) findViewById(R.id.face_camera);
        faceText2 = (TextView) findViewById(R.id.face_camera2);
        fail_text = (TextView) findViewById(R.id.input_fail);

        mButton = (Button) findViewById(R.id.addface_bt);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (isAddFace == 0 || isAddFace == 2) {
                    koobee_detectNum = 0;
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    mHandle.sendEmptyMessageDelayed(6, 120000);
                    scanFrame.setImageResource(R.drawable.circle_input_face);
                    addFace.setVisibility(View.INVISIBLE);
                    inputFace.setVisibility(View.VISIBLE);
                    fail_text.setVisibility(View.INVISIBLE);

                    mButton.setText(getString(R.string.cancle));

                    if (isAddFace == 0) {
                        startScanAnimation();
                    }else {
                        initDetector(0);
                        startDetector();
                    }
                    isAddFace = 1;
                } else if (isAddFace == 1) {
                    isAddFace = 0;
                    finish();
                }*/
            	Intent intent = new Intent();
                intent.setAction("sensetime.intent.action.register.activity");
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /*if (manager != null) {
            manager.onPause();
            manager.onDestory();
            finish();
        }*/
        finish();
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        if (!isAddFace) {
            initDetector();
            startDetector();
        }
    }*/

    @Override
    protected void onStart() {
        super.onStart();

    }

   /* @Override
    protected void onRestart() {
        super.onRestart();
        if (!isAddFace) {
            manager.startPreview();
            mSurfaceView.setCameraInfoCallback(this);
        }
    }*/

    ObjectAnimator scanAnimator;

    public void startScanAnimation() {
        int left = scanFrame.getLeft();
        int top = scanFrame.getTop();
        int bottom = scanFrame.getBottom();
        int right = scanFrame.getRight();

        scanAnimator = ObjectAnimator.ofFloat(scanImageView, "translationY", bottom, -bottom);
        scanAnimator.setDuration(4000);
        scanAnimator.setInterpolator(new LinearInterpolator());
        scanAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scanAnimator.setRepeatMode(ValueAnimator.INFINITE);
        scanAnimator.start();

        initSurfaceView(right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initSurfaceView(int x) {
        mSurfaceView = new PaDtcSurfaceView(PrizeFaceprintEnrollEnrolling.this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(x, x / 3 * 4);
        mSurfaceView.setLayoutParams(layoutParams);
        frameLayout.addView(mSurfaceView);
        initDetector(0);
        startDetector();
    }

    private String[] name = new String[]{"人脸 1", "人脸 2", "人脸 3", "人脸 4", "人脸 5"};
    private String[] facename = new String[]{"face1", "face2", "face3", "face4", "face5"};
    private int[] imageviewbg = new int[]{R.drawable.circle_input_face1, R.drawable.circle_input_face2, R.drawable.circle_input_face3, R.drawable.circle_input_face4, R.drawable.circle_input_face5};
    private int iIndex, jIndex;
    private List<String> facelist, namelist;

    private void initDetector(int num) {
        manager = new PaFaceDetectorManager(this, mSurfaceView, 0);
        manager.setCameraMode(Camera.CameraInfo.CAMERA_FACING_FRONT);
        manager.seeLog(true);

        try {
            File dir = new File("/data/system/users/faceid/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        manager.setPath("/data/system/users/faceid/"); //  /data/system/users/faceid
        manager.setDetectType(0);

        /*facelist = SaveListUtil.getList(this);
        namelist = new ArrayList<String>();
        for (int i = 0; i < facelist.size(); i++) {
            namelist.add((String) SpUtil.getData(this, facelist.get(i), ""));
        }

        for (int i = 0; i < facename.length; i++) {
            if (!facelist.contains(facename[i])) {
                iIndex = i;
                //facelist.add(facename[i]);
                for (int j = 0; j < name.length; j++) {
                    if (!namelist.contains(name[j])) {
                        jIndex = j;
                        //SpUtil.saveData(this, facename[i], name[j]);
                        break;
                    }
                }
                break;
            }
        }*/

        //SaveListUtil.saveList(this, facelist);
        manager.setUserName(facename[num]);
    }

    private void startDetector() {
        manager.startPreview();
        manager.startDetector();
        mSurfaceView.setCameraInfoCallback(this);
        manager.setOnFaceDetectorListener(this);
    }

    @Override
    public void setOpenCameraInfo(boolean isOpen) {
        if (!isOpen) {
            manager.stopDetector();
            mHandle.sendEmptyMessage(21);
        } else {
            mHandle.sendEmptyMessage(31);
        }
    }

    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 11:
                    faceText.setText(getString(R.string.face_input_success));
                    faceText2.setText(getString(R.string.face_unlock));
                    mButton.setText(getString(R.string.done));
                    mButton.setVisibility(View.INVISIBLE);
                    if (scanAnimator != null) {
                        scanAnimator.end();
                    }
                    scanImageView.setVisibility(View.INVISIBLE);
                    face_photo.setVisibility(View.VISIBLE);
                    face_photo.setImageBitmap(faceBitmap);
                    ViewGroup.LayoutParams layoutParams = face_photo.getLayoutParams();
                    layoutParams.width = scanFrame.getRight();
                    layoutParams.height = scanFrame.getRight() / 3 * 4;
                    face_photo.setLayoutParams(layoutParams);
                    mHandle.sendEmptyMessageDelayed(6, 1500);
                    //face_photo.setScaleX(4 / 3f);
                    break;
                case 21:
                    faceText.setText(getString(R.string.camera_unavail));
                    break;
                case 31:
                    faceText.setText(getString(R.string.face_camera_detecting));
                    break;
                case 0:
                    initDetector(0);
                    startDetector();
                    break;
                case 1:
                    scanFrame.setImageResource(R.drawable.circle_input_face1);
                    initDetector(1);
                    startDetector();
                    break;
                case 2:
                    scanFrame.setImageResource(R.drawable.circle_input_face2);
                    initDetector(2);
                    startDetector();
                    break;
                case 3:
                    scanFrame.setImageResource(R.drawable.circle_input_face3);
                    initDetector(3);
                    startDetector();
                    break;
                case 4:
                    scanFrame.setImageResource(R.drawable.circle_input_face4);
                    initDetector(4);
                    startDetector();
                    break;
                case 5:
                    scanFrame.setImageResource(R.drawable.circle_input_face5);
                    faceBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), srcBitmap.getConfig());
                    Paint paint = new Paint();
                    Canvas canvas = new Canvas(faceBitmap);
                    Matrix mt = new Matrix();
                    mt.setScale(-1, 1);
                    mt.postTranslate(faceBitmap.getWidth(), 0);
                    canvas.drawBitmap(srcBitmap, mt, paint);
                    mHandle.sendEmptyMessage(11);

                    /*facelist = new ArrayList<String>();
                    facelist.add(facename[0]);
                    SaveListUtil.saveList(PrizeFaceprintEnrollEnrolling.this, facelist);
                    SpUtil.saveData(PrizeFaceprintEnrollEnrolling.this, facename[0], name[0]);*/

                    FaceBean faceBean = FaceXmlData.readCheckRootDataFile(FaceXmlData.FACEBEAN_PHTH);
                    faceBean.faceSize = 1;
                    SystemProperties.set("persist.sys.pafd", "1");
                    faceBean.face_name = name[0];
                    FaceXmlData.writeCheckRootDataFile(faceBean, FaceXmlData.FACEBEAN_PHTH);
                    break;
                case 6:
                    finish();
                    break;
                case 9:
                    scanFrame.setImageResource(imageviewbg[koobee_detectNum]);
                    koobee_detectNum += 1;
                    if (koobee_detectNum == 5) {
                        mHandle.sendEmptyMessage(5);
                    }
                    break;
                case 10:
                    isAddFace = 2;
                    addFace.setVisibility(View.INVISIBLE);
                    inputFace.setVisibility(View.INVISIBLE);
                    fail_text.setVisibility(View.VISIBLE);

                    mButton.setText(getString(R.string.face_retry));
                    break;
                default:
                    break;
            }
        }
    };

    Bitmap faceBitmap, srcBitmap;
    int detectNum = 0;
    int koobee_detectNum = 0;

    @Override
    public void detectSuccess(PAFaceDetectorFrame paFaceDetectorFrame) {
        if (!PrizeOption.PRIZE_FACE_ID_KOOBEE) {
            detectNum += 1;
            Log.i("tzm", "num=" + detectNum);
            mHandle.sendEmptyMessageDelayed(detectNum, 50);
        }

        srcBitmap = paFaceDetectorFrame.getLivnessBitmap();
        /*Bitmap srcBitmap = paFaceDetectorFrame.getLivnessBitmap();
        faceBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), srcBitmap.getConfig());
        Paint paint = new Paint();
        Canvas canvas = new Canvas(faceBitmap);
        Matrix mt = new Matrix();
        mt.setScale(-1, 1);
        mt.postTranslate(faceBitmap.getWidth(), 0);
        canvas.drawBitmap(srcBitmap, mt, paint);*/
        //mHandle.sendEmptyMessage(1);
        //saveSuccessData(paFaceDetectorFrame);

        /*facelist.add(facename[iIndex]);
        SaveListUtil.saveList(this, facelist);
        SpUtil.saveData(this, facename[iIndex], name[jIndex]);*/

        Log.i("tzm", "detectSuccess");
    }

    @Override
    public void registerSuccess(boolean isReg) {
        Log.i("tzm", "registerSuccess=" + isReg + "   num=" + detectNum);
        if (PrizeOption.PRIZE_FACE_ID_KOOBEE) {
            mHandle.sendEmptyMessage(31);
            if (isReg) {
                mHandle.sendEmptyMessageDelayed(9,100);
            } else {
                manager.onPause();
                mHandle.sendEmptyMessageDelayed(10,100);
            }
        } else {
            if (!isReg) {
                mHandle.sendEmptyMessageDelayed(detectNum, 100);
            }
        }
    }

    @Override
    public void compareSuccess(boolean isFace, float score, String compareName) {
        Log.i("tzm", "compareSuccess");
    }

    @Override
    public void liveSuccess(boolean isLive, float liveScore) {
        Log.i("tzm", "liveSuccess="+isLive);
    }

    private void saveSuccessData(PAFaceDetectorFrame paFaceDetectorFrame) {
        /*List<String> list = SaveListUtil.getList(this);
        String faceName = "face" + (list.size() + 1);
        list.add(faceName);
        SaveListUtil.saveList(this, list);*/
        //SaveBitmapUtil.saveBitmap2Sdcard(faceName, paFaceDetectorFrame.getLivnessHeadBitmap());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        faceBitmap = null;
        srcBitmap = null;
        /*if (manager != null) {
            manager.onDestory();
        }*/
    }
}
