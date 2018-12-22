/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: U:\\project\\PRI6750_66_M\\packages\\apps\\PrizeDoubleCamera\\src\\com\\mediatek\\camera\\addition\\remotecamera\\service\\IMtkCameraService.aidl
 */
package com.mediatek.camera.addition.remotecamera.service;
public interface IMtkCameraService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.mediatek.camera.addition.remotecamera.service.IMtkCameraService
{
private static final java.lang.String DESCRIPTOR = "com.mediatek.camera.addition.remotecamera.service.IMtkCameraService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.mediatek.camera.addition.remotecamera.service.IMtkCameraService interface,
 * generating a proxy if needed.
 */
public static com.mediatek.camera.addition.remotecamera.service.IMtkCameraService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.mediatek.camera.addition.remotecamera.service.IMtkCameraService))) {
return ((com.mediatek.camera.addition.remotecamera.service.IMtkCameraService)iin);
}
return new com.mediatek.camera.addition.remotecamera.service.IMtkCameraService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_openCamera:
{
data.enforceInterface(DESCRIPTOR);
this.openCamera();
reply.writeNoException();
return true;
}
case TRANSACTION_releaseCamera:
{
data.enforceInterface(DESCRIPTOR);
this.releaseCamera();
reply.writeNoException();
return true;
}
case TRANSACTION_capture:
{
data.enforceInterface(DESCRIPTOR);
this.capture();
reply.writeNoException();
return true;
}
case TRANSACTION_sendMessage:
{
data.enforceInterface(DESCRIPTOR);
android.os.Message _arg0;
if ((0!=data.readInt())) {
_arg0 = android.os.Message.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.sendMessage(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_registerCallback:
{
data.enforceInterface(DESCRIPTOR);
com.mediatek.camera.addition.remotecamera.service.ICameraClientCallback _arg0;
_arg0 = com.mediatek.camera.addition.remotecamera.service.ICameraClientCallback.Stub.asInterface(data.readStrongBinder());
this.registerCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterCallback:
{
data.enforceInterface(DESCRIPTOR);
com.mediatek.camera.addition.remotecamera.service.ICameraClientCallback _arg0;
_arg0 = com.mediatek.camera.addition.remotecamera.service.ICameraClientCallback.Stub.asInterface(data.readStrongBinder());
this.unregisterCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setFrameRate:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.setFrameRate(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_cameraServerExit:
{
data.enforceInterface(DESCRIPTOR);
this.cameraServerExit();
reply.writeNoException();
return true;
}
case TRANSACTION_getSupportedFeatureList:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getSupportedFeatureList();
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.mediatek.camera.addition.remotecamera.service.IMtkCameraService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void openCamera() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_openCamera, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void releaseCamera() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_releaseCamera, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void capture() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_capture, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void sendMessage(android.os.Message msg) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((msg!=null)) {
_data.writeInt(1);
msg.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_sendMessage, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void registerCallback(com.mediatek.camera.addition.remotecamera.service.ICameraClientCallback cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void unregisterCallback(com.mediatek.camera.addition.remotecamera.service.ICameraClientCallback cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setFrameRate(int frameRate) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(frameRate);
mRemote.transact(Stub.TRANSACTION_setFrameRate, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// add for release

@Override public void cameraServerExit() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_cameraServerExit, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public java.lang.String getSupportedFeatureList() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSupportedFeatureList, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_openCamera = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_releaseCamera = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_capture = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_sendMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_unregisterCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_setFrameRate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_cameraServerExit = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getSupportedFeatureList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
}
public void openCamera() throws android.os.RemoteException;
public void releaseCamera() throws android.os.RemoteException;
public void capture() throws android.os.RemoteException;
public void sendMessage(android.os.Message msg) throws android.os.RemoteException;
public void registerCallback(com.mediatek.camera.addition.remotecamera.service.ICameraClientCallback cb) throws android.os.RemoteException;
public void unregisterCallback(com.mediatek.camera.addition.remotecamera.service.ICameraClientCallback cb) throws android.os.RemoteException;
public void setFrameRate(int frameRate) throws android.os.RemoteException;
// add for release

public void cameraServerExit() throws android.os.RemoteException;
public java.lang.String getSupportedFeatureList() throws android.os.RemoteException;
}
