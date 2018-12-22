package com.prize.faceunlock.service;
interface IFaceVerifyServiceCallback
{
    void sendRecognizeResult(int resultId, String commandStr);
}