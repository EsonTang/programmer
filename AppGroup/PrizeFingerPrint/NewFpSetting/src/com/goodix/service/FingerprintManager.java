package com.goodix.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.goodix.aidl.IEnrollCallback;
import com.goodix.aidl.IFingerprintManager;
import com.goodix.aidl.IVerifyCallback;
import com.goodix.aidl.IUpdateBaseCallback;

public class FingerprintManager{
    private static final String TAG = "FpSetting";
    
    
    /**
     * @Fields mService : remote fingerprint manager service .
     */
    private static IFingerprintManager mService;
    
    /**
     * @Title: query   
     * @Description: get fingerprint items status 
     * @param @return
     * @return int : fingerprint items status,first 16bit is count,next 16bit is the flag
     * that fingerprint has data or not.
     * @throws 
     */
    public int query(){
        try{
            return mService.query();
        }catch (RemoteException e){
            e.printStackTrace();
        }
        return 0;
    }
    
    
    /**
     * @Title: delete   
     * @Description: delete fingerprint item.
     * @param @param i : index of item
     * @param @return
     * @return int : error code,like bad param , no permission .
     * @throws 
     */
    public int delete(int i){
        try{
            return mService.delete(i);
        }catch (RemoteException e){
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * 
     *  get information about fingerFrint
     *
     */
    public String getInformation() {
        try{
            return mService.getInfo();
        }catch (RemoteException e){
            e.printStackTrace();
        }
        return null;
    }
	
    public boolean checkPassword(String psw){
        if (null != psw){
            return psw.equals("1234");
        }
        return false;
    }
    
    public boolean changePassword(String oldPassword,String newPassword){
        return true;
    }
    
    public FingerprintManager(IFingerprintManager service){
        mService = service;
    }
    
    
    /**
     * @Title: newVerifySession   
     * @Description: verify session
     * @param @param callback
     * @param @return Verify session object
     * @return VerifySession 
     * @throws 
     */
    public VerifySession newVerifySession(IVerifyCallback callback){
        return new VerifySession(callback);
    }
    
    
    /**
     * @Title: newEnrollSession   
     * @Description: get a new Enroll session object
     * @param @param callback 
     * @param @return
     * @return EnrollSession : Enroll session object
     * @throws 
     */
    public EnrollSession newEnrollSession(IEnrollCallback callback){
        return new EnrollSession(callback);
    }
    

    public void initMode() {
        try {
            mService.initMode();
        } catch (RemoteException e) {
            Log.e(TAG, "cannot execute updateBase cmd", e);
        }
    }

    public void setUpdateBaseCallback(IUpdateBaseCallback callback) {
        try {
            mService.setUpdateBaseCallback(callback);
        } catch (RemoteException e) {
            Log.e(TAG, "cannot set update base callback", e);
        }
    }
    /**
     * <p>Title: FingerprintManager.java</p>
     * <p>Description: Verify Session</p>
     */
    public static final class VerifySession {
        
        
        /**
         * @Fields mToken : session's ID , difference other session.
         */
        private IBinder mToken;
        /**
         * @Fields mCallback : FingerprintManagerService's message handler.
         */
        private IVerifyCallback mCallback;
        
        public static boolean isEntry = false;
        
        public VerifySession(IVerifyCallback callback) {
            Log.v(TAG,"new VerifySession.");
            Log.d("ServiceStartReceiver", "newVerifySession New");
            mCallback = callback;
            mToken = new Binder();
        }
        
        
        /**
         * @Title: enter   
         * @Description: session begin
         * @param @return
         * @return int errcode.
         * @throws 
         */
        public int enter() {
            try {
                Log.v(TAG,"verify session enter.");
                isEntry = true;
                return mService.verify(mToken, mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return -1;
        }
        
        
        /**
         * @Title: exit   
         * @Description: session exit
         * @param @return
         * @return int 
         * @throws 
         */
        public int exit() {
            try {
                Log.v(TAG,"verify session exit.");
                isEntry = false;
                return mService.cancelVerify(mToken);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return -1; 
        }
    }
    

    public static final class EnrollSession {
        private IBinder mToken;
        private IEnrollCallback mCallback;
        public EnrollSession(IEnrollCallback callback){
            mCallback = callback;
            mToken = new Binder();
        }

        public int enter() {
            try {
                Log.v(TAG,"new enroll session.");
                return mService.enroll(mToken, mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return -1;
        }

        public int exit() {
            try {
                Log.v(TAG,"enroll session exit.");
                return mService.cancelEnroll(mToken);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return -1;
        }
        
        public int reset() {
            try {
                Log.v(TAG,"enroll session reset.");
                return mService.resetEnroll(mToken);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return -1;
        }
        
        public int save(int index){
            try{
                Log.v(TAG,"enroll session save.");
                return mService.saveEnroll(mToken,index);
            }catch (RemoteException e){
                e.printStackTrace();
            }
            return -1;
        }
    }
    
}
