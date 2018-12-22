
package com.prize.qihoo.cleandroid.sdk.plugins;

import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Log;

import com.prize.qihoo.cleandroid.sdk.SDKEnv;
import com.qihoo360.mobilesafe.opti.i.plugins.IPtManager;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 * 清理SDK 不提供Root实现，如需要Root相关的功能，请实现这个接口
 */
public class PtManagerImpl implements IPtManager {
    private static final boolean DEBUG = SDKEnv.DEBUG;

    private static final String TAG = DEBUG ? "PtManagerImpl" : PtManagerImpl.class.getSimpleName();

    public static boolean isRootOk() {
        return false;
    }

    /**
     * （清理SDK依赖）判断Root命令是否可以执行，可在UI线程调用 ；
     *
     * @return Root命令是否可以执行注：如果返回true, getRtConn（）方法一定要返回非Null，否则Root命令会不执行
     */
    @Override
    public boolean isRtServiceRunning() {
        if (DEBUG) {
            Log.d(TAG, "isRtServiceRunning");
        }
        return false;
    }

    /**
     * （清理SDK依赖）Root命令执行（不带环境变量参数），返回值是 int型的执行结果
     *
     * @param binder 执行命令行的IBinde实现（如果命令行不是Service实现，可以不使用）
     * @param command 命令行
     * @param commandArgs 命令行参数
     * @return 命令执行结果 返回值类型是int型
     */
    @Override
    public int execvp(IBinder binder, String command, List<String> commandArgs) {
        if (DEBUG) {
            Log.d(TAG, "execvp  command:" + command + " commandArgs:" + commandArgs);
        }

        return -1;
    }

    /**
     * （清理SDK依赖）Root命令执行（带环境变量参数），返回值是 int型的执行结果
     *
     * @param binder 执行命令行的IBinde实现（如果命令行不是Service实现，可以不使用）
     * @param command 命令行
     * @param commandArgs 命令行参数
     * @param envp 命令行执行依赖的环境变量
     * @return 命令执行结果 返回值类型是int型
     */
    @Override
    public int execve(IBinder binder, String command, List<String> commandArgs, List<String> envp) {
        if (DEBUG) {
            Log.d(TAG, "execvp  command:" + command + " commandArgs:" + commandArgs + " envp:" + envp);
        }
        return -1;
    }

    /**
     * （清理SDK依赖）Root命令执行（不带环境变量参数），返回值是 int型的执行结果
     *
     * @param binder 执行命令行的IBinde实现（如果命令行不是Service实现，可以不使用）
     * @param command 命令行
     * @param commandArgs 命令行参数
     * @return 命令执行结果 返回值类型是byte[]数组
     */
    @Override
    public byte[] execp(IBinder binder, String command, ArrayList<String> commandArgs) {
        if (DEBUG) {
            Log.d(TAG, "execvp  command:" + command + " commandArgs:" + commandArgs);
        }
        return null;
    }

    /**
     * （清理SDK依赖）Root命令执行（带环境变量参数），返回值是 int型的执行结果
     *
     * @param binder 执行命令行的IBinde实现（如果命令行不是Service实现，可以不使用）
     * @param command 命令行
     * @param commandArgs 命令行参数
     * @param envp 命令行执行依赖的环境变量
     * @return 命令执行结果 返回值类型是byte[]数组
     */
    @Override
    public byte[] exec(IBinder binder, String command, List<String> commandArgs, List<String> envp) {
        if (DEBUG) {
            Log.d(TAG, "execvp  command:" + command + " commandArgs:" + commandArgs + " envp:" + envp);
        }
        return null;
    }

    /**
     * 绑定Root命令执行服务（可以不实现）
     *
     * @param context
     * @param serviceConnection
     * @return
     */
    @Override
    public boolean bindRtService(Context context, ServiceConnection serviceConnection) {
        if (DEBUG) {
            Log.d(TAG, "bindRtService serviceConnection:" + serviceConnection);
        }

        return false;
    }

    /**
     * 解除绑定Root命令执行服务（可以不实现）
     *
     * @param context
     * @param serviceConnection
     */
    @Override
    public void unBindRtService(Context context, ServiceConnection serviceConnection) {
        if (DEBUG) {
            Log.d(TAG, "unBindRtService serviceConnection:" + serviceConnection);
        }

    }

    /**
     * 查询手机中是否有su（可以不实现）
     *
     * @return
     */
    @Override
    public boolean isPhoneRted() {
        if (DEBUG) {
            Log.d(TAG, "isPhoneRted");
        }
        return false;
    }

    /**
     * （清理SDK依赖）获取命令行执行IBinder（可以返回空实现的IBinder）
     *
     * @param context
     * @throws Exception
     */
    @Override
    public IBinder getRtConn(Context context) throws Exception {
        if (DEBUG) {
            Log.d(TAG, "getRtConn");
        }
        return mBinderEmprty;
    }

    /**
     * 释放#getRtConn()创建的Connection（可以不实现）
     *
     * @param context
     * @param binder
     */
    @Override
    public void dismissConn(Context context, IBinder binder) {
        if (DEBUG) {
            Log.d(TAG, "dismissConn");
        }
    }

    /**
     * 通过超强模式获取root授权（可以不实现）
     *
     * @param context
     */
    @Override
    public void requestRtAuth(Context context) {
        if (DEBUG) {
            Log.d(TAG, "requestRtAuth");
        }
    }

    // 如果不是用
    private final IBinder mBinderEmprty = new IBinder() {

        @Override
        public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
            return false;
        }

        @Override
        public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return false;
        }

        @Override
        public IInterface queryLocalInterface(String descriptor) {
            return null;
        }

        @Override
        public boolean pingBinder() {
            return false;
        }

        @Override
        public void linkToDeath(DeathRecipient recipient, int flags) throws RemoteException {

        }

        @Override
        public boolean isBinderAlive() {
            return false;
        }

        @Override
        public String getInterfaceDescriptor() throws RemoteException {
            return null;
        }

        @Override
        public void dump(FileDescriptor fd, String[] args) throws RemoteException {

        }

        @Override
        public void dumpAsync(FileDescriptor arg0, String[] arg1) throws RemoteException {
            // TODO Auto-generated method stub

        }

        @Override
        public void shellCommand(FileDescriptor fileDescriptor, FileDescriptor fileDescriptor1, FileDescriptor fileDescriptor2, String[] strings, ResultReceiver resultReceiver) throws RemoteException {

        }
    };
}
