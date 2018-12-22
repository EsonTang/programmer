package com.android.settings.face;

/**
 * Created by Administrator on 2017/12/20.
 */

public class FaceBean {
    public boolean face_show_ani;
    public String face_name;
    public int faceSize;

    public FaceBean(boolean face_show_ani, String face_name, int faceSize) {
        this.face_show_ani = face_show_ani;
        this.face_name = face_name;
        this.faceSize = faceSize;
    }

    public int getFaceSize() {
        return faceSize;
    }

    public String getFace_name() {
        return face_name;
    }

    public boolean getFace_show_ani() {
        return face_show_ani;
    }
}
