/**
 * 
 */
package com.prize.faceunlock.service;

public interface FaceUnlockCallback
{

    void onFaceVerifyChanged(int resultCode, String msg);
}
