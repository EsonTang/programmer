/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: U:\\project\\PRI6750_66_M\\packages\\apps\\PrizeDoubleCamera\\src\\com\\mediatek\\camera\\addition\\remotecamera\\service\\ICameraClientCallback.aidl
 */
package com.mediatek.camera.addition.remotecamera.service;
public interface ICameraClientCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.mediatek.camera.addition.remotecamera.service.ICameraClientCallback
{
private static final java.lang.String DESCRIPTOR = "com.mediatek.camera.addition.remotecamera.service.ICameraClientCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.mediatek.camera.addition.remotecamera.service.ICameraClientCallback interface,
 * generating a proxy if needed.
 */
public static com.mediatek.camera.addition.remotecamera.service.ICameraClientCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.mediatek.camera.addition.remotecamera.service.ICameraClientCallback))) {
return ((com.mediatek.camera.addition.remotecamera.service.ICameraClientCallback)iin);
}
return new com.mediatek.camera.addition.remotecamera.service.ICameraClientCallback.Stub.Proxy(obj);
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
case TRANSACTION_onPreviewFrame:
{
data.enforceInterface(DESCRIPTOR);
byte[] _arg0;
_arg0 = data.createByteArray();
this.onPreviewFrame(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onPictureTaken:
{
data.enforceInterface(DESCRIPTOR);
byte[] _arg0;
_arg0 = data.createByteArray();
this.onPictureTaken(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_cameraServerApExit:
{
data.enforceInterface(DESCRIPTOR);
this.cameraServerApExit();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.mediatek.camera.addition.remotecamera.service.ICameraClientCallback
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
@Override public void onPreviewFrame(byte[] previewData) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByteArray(previewData);
mRemote.transact(Stub.TRANSACTION_onPreviewFrame, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onPictureTaken(byte[] pictureData) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByteArray(pictureData);
mRemote.transact(Stub.TRANSACTION_onPictureTaken, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void cameraServerApExit() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_cameraServerApExit, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onPreviewFrame = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onPictureTaken = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_cameraServerApExit = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public void onPreviewFrame(byte[] previewData) throws android.os.RemoteException;
public void onPictureTaken(byte[] pictureData) throws android.os.RemoteException;
public void cameraServerApExit() throws android.os.RemoteException;
}
