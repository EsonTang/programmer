package com.prize.appcenter.ui.animation;

import android.view.SurfaceView;

public class DrawThread extends Thread {  
    private SurfaceView surfaceView;  
    private boolean running;  

    public DrawThread(SurfaceView surfaceView) {  
       this.surfaceView = surfaceView;  
    }  

    public void run() {  
       if (surfaceView == null) {  
           return;  
       }  
       if (surfaceView instanceof ParabolaView) {  
           ((ParabolaView) surfaceView).handleThread();  
       }  
    }  

    public void setRunning(boolean b) {  
       running = b;  
    }  

    public boolean isRunning() {  
       return running;  
    }  
}  