
package com.gangyun.camerabox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.mediatek.camera.util.Log;

public class GotoCameraBoxActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        try {       
		Intent intent = new Intent();
		intent.setClassName("com.gangyun.beautysnap", "com.gangyun.camerasdk.CameraActivity");
		intent.putExtra("from_sys_camera", 1);
		this.startActivity(intent);
		this.finish();
        }
        catch(Exception th){
             Intent intent = new Intent();
             intent.setClassName("com.mediatek.camera", "com.android.camera.CameraActivity");
             this.startActivity(intent);
             this.finish();
        }
   
    }

}
