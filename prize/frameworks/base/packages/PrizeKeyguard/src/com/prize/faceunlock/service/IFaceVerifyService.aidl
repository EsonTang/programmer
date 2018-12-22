package com.prize.faceunlock.service;
import com.prize.faceunlock.service.IFaceVerifyServiceCallback;
interface IFaceVerifyService
{
    void unregisterCallback(IFaceVerifyServiceCallback mCallback);
    void registerCallback(IFaceVerifyServiceCallback mCallback);
    void startVerify();
    void stopVerify();
}