/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

//New file added by delong.liu@archermind.com

package com.mediatek.telecom.recording;

import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.SystemProperties;
import android.util.Slog;

import com.android.server.telecom.R;
import com.mediatek.storage.StorageManagerEx;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Recorder implements OnErrorListener {
    private static final String TAG = Recorder.class.getSimpleName();

    static final String SAMPLE_PREFIX = "recording";
    static final String SAMPLE_PATH_KEY = "sample_path";
    static final String SAMPLE_LENGTH_KEY = "sample_length";

    public static final int IDLE_STATE = 0;
    public static final int RECORDING_STATE = 1;

    public static final int NO_ERROR = 0;
    public static final int SDCARD_ACCESS_ERROR = 1;
    public static final int INTERNAL_ERROR = 2;
    public static final int STORAGE_FULL = 3;
    public static final int SUCCESS = 4;
    public static final int STORAGE_UNMOUNTED = 5;

    //TODO For PhoneRecorder is singleton, maybe remove static tag
    static boolean sIsRecording;

    public interface OnStateChangedListener {
        void onStateChanged(int state);
        void onError(int error);
        void onFinished(int cause, String data);
    }

    private long mSampleStart; // time at which latest record or play operation
    private int mState = IDLE_STATE;
    private MediaRecorder mRecorder;
    private OnStateChangedListener mOnStateChangedListener;

    protected long mSampleLength; // length of current sample
    protected File mSampleFile;
    protected String mRecordStoragePath; // the path where saved recording file.

    protected Recorder() {
    }

    public void setOnStateChangedListener(OnStateChangedListener listener) {
        mOnStateChangedListener = listener;
    }

    /**
     * delete the recorded sample file
     */
    protected void deleteSampleFile() {
        if (mSampleFile != null) {
            mSampleFile.delete();
        }
        mSampleFile = null;
        mSampleLength = 0L;
    }

    /**
     * @param outputfileformat
     * @param extension
     * @throws IOException
     */
    public void startRecording(int outputfileformat, String extension) throws IOException {
        log("startRecording");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        String prefix = dateFormat.format(new Date());
        File sampleDir = new File(StorageManagerEx.getDefaultPath());

        if (!sampleDir.canWrite()) {
            Slog.i(TAG, "----- file can't write!! ---");
            // Workaround for broken sdcard support on the device.
            sampleDir = new File("/sdcard/sdcard");
        }

        sampleDir = new File(sampleDir.getAbsolutePath() + "/PhoneRecord");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        log("sampleDir path is " + sampleDir.getAbsolutePath());

        /// For ALPS01000670. @{
        // get the current path where saved recording files.
        mRecordStoragePath = sampleDir.getCanonicalPath();
        /// @}

        try {
            mSampleFile = File.createTempFile(prefix, extension, sampleDir);
        } catch (IOException e) {
            Slog.i(TAG, "----***------- can't access sdcard !! " + e);
            e.printStackTrace();
            throw e;
        }

        log("finish creating temp file, start to record");

        mRecorder = new MediaRecorder();
        mRecorder.setOnErrorListener(this);
        ///M: ALPS02374165
        // change audio source according to system property
        // so that to test different record type @{
        //mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        String recordType = SystemProperties.get("persist.incallrec.audiosource", "-1");
        log("recordType is: " + Integer.parseInt(recordType));
        if (recordType.equals("-1")) {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            mRecorder.setAudioChannels(2);
        } else {
            mRecorder.setAudioSource(Integer.parseInt(recordType));
            if (recordType.equals("4")) {
                mRecorder.setAudioChannels(2);
            } else {
                mRecorder.setAudioChannels(1);
            }
        }
        /// @}

        mRecorder.setOutputFormat(outputfileformat);
        /// ALPS01426963 @{
        // change record encoder format for AMR_NB to ACC, so that improve the record quality.
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioEncodingBitRate(24000);
        mRecorder.setAudioSamplingRate(16000);
        /// @}
        mRecorder.setOutputFile(mSampleFile.getAbsolutePath());

        try {
            mRecorder.prepare();
            mRecorder.start();
            mSampleStart = System.currentTimeMillis();
            setState(RECORDING_STATE);
        } catch (IOException exception) {
            log("startRecording, IOException");
            handleException();
            //throw exception;
        ///M: ALPS03307186
        // avoid JE, show toast to end user @{
        } catch (IllegalStateException exception) {
            log("startRecording, IllegalStateException");
            handleException();
            deleteSampleFile();
        }
        /// @}
    }

    private void handleException() {
        if (sIsRecording) {
            sIsRecording = false;
        }
        setError(INTERNAL_ERROR);
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
        setState(IDLE_STATE);
    }

    public void stopRecording() {
        log("stopRecording");
        if (mRecorder == null) {
            return;
        }
        mSampleLength = System.currentTimeMillis() - mSampleStart;
        try {
            mRecorder.stop();
        } catch (RuntimeException e) {
            // no output, use to delete the file
            e.printStackTrace();
            deleteSampleFile();
        }

        mRecorder.release();
        mRecorder = null;

        setState(IDLE_STATE);
    }

    abstract protected void onMediaServiceError();

    void setState(int state) {
        if (state == mState) {
            return;
        }
        mState = state;
        fireStateChanged(mState);
    }

    private void fireStateChanged(int state) {
        log("fireStateChanged " + state);
        if (mOnStateChangedListener != null) {
            mOnStateChangedListener.onStateChanged(state);
        }
    }

    void fireRecordFinished(int cause, String data) {
        log("fireRecordFinished " + cause + "/" + data);
        if (mOnStateChangedListener != null) {
            mOnStateChangedListener.onFinished(cause, data);
        }
    }

    private void setError(int error) {
        if (mOnStateChangedListener != null) {
            mOnStateChangedListener.onError(error);
        }
    }

    @Override
    public void onError(MediaRecorder mp, int what, int extra) {
        log("onError");
        if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
            onMediaServiceError();
        }
        return;
    }

    /**
     * Get the recording path.
     * @return
     */
    public String getRecordingPath() {
        return mRecordStoragePath;
    }

    private void log(String msg) {
        Slog.d(TAG, msg);
    }
}