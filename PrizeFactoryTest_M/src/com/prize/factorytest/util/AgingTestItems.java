package com.prize.factorytest.util;

import java.io.Serializable;

public class AgingTestItems implements Serializable{

	private static final long serialVersionUID = 1L;
	private static final String TAG = "AgingTestItems";
	
	public static final int VIDEO_SPEAKER = 0;
	public static final int VIDEO_RECEIVER = 1;
	public static final int VIBRATE = 2;
	public static final int MIC_LOOP = 3;
	public static final int FRONT_CAMERA = 4;
	public static final int BACK_CAMERA = 5;
	
	private boolean videoAndSpeaker;
	private boolean videoAndReceiver;
	private boolean vibrate;
	private boolean micLoop;
	private boolean frontCamera;
	private boolean backCamera;
	
	public void setvideoAndSpeaker(boolean videoAndSpeaker){
		this.videoAndSpeaker=videoAndSpeaker;
	}	
	public boolean getvideoAndSpeaker(){
		return videoAndSpeaker;
	}
	
	public void setvideoAndReceiver(boolean videoAndReceiver){
		this.videoAndReceiver=videoAndReceiver;
	}	
	public boolean getvideoAndReceiver(){
		return videoAndReceiver;
	}
	
	public void setVibrate(boolean vibrate){
		this.vibrate=vibrate;
	}	
	public boolean getVibrate(){
		return vibrate;
	}
	
	public void setMicLoop(boolean micLoop){
		this.micLoop=micLoop;
	}	
	public boolean getMicLoop(){
		return micLoop;
	}
	
	public void setFrontCamera(boolean frontCamera){
		this.frontCamera=frontCamera;
	}	
	public boolean getFrontCamera(){
		return frontCamera;
	}
	
	public void setBackCamera(boolean backCamera){
		this.backCamera=backCamera;
	}	
	public boolean getBackCamera(){
		return backCamera;
	}
	public boolean itemIdToValue(int id){
		final int itemId = id;		
		boolean result = false;
		switch(itemId){
		case VIDEO_SPEAKER:
			result = videoAndSpeaker;
			break;
		case VIDEO_RECEIVER:
			result = videoAndReceiver;
			break;
		case VIBRATE:
			result = vibrate;
			break;
		case MIC_LOOP:
			result = micLoop;
			break;
		case FRONT_CAMERA:
			result = frontCamera;
			break;
		case BACK_CAMERA:
			result = backCamera;
			break;
		}
		return result; 
	}
	public int getNextTestItem(int current){
		if(current==BACK_CAMERA)
			return -1;
		int i;
		for(i=current+1;i<6;i++){
			if(itemIdToValue(i))
				break;
			if(i==BACK_CAMERA)
				return -1;
		}
		return i;
	}
}
