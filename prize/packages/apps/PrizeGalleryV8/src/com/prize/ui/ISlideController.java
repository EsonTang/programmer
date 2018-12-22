package com.prize.ui;

public interface ISlideController {
	
	/** ADJUST progress**/
    public static final int GESTURE_SLIDE_PROGRESS = 0;
    /** adjust volume**/
    public static final int GESTURE_SLIDE_VOLUME = 1;
    /** adjust light**/
    public static final int GESTURE_SLIDE_BRIGHTNESS = 2;
	void onSlideStart(int type);

    void onSlideMove(int type, boolean inc);

    void onSlideEnd(int type);
}
