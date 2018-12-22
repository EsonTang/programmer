package com.android.launcher3;

import com.nostra13.universalimageloader.core.ImageLoader;

public class PauseOnListener implements ShowList.OnPauseListener {

	private ImageLoader imageLoader;
	private boolean pauseOnScroll;

	public PauseOnListener(ImageLoader imageLoad, boolean pauseOnScroll) {
		super();
		this.imageLoader = imageLoad;
		this.pauseOnScroll = pauseOnScroll;
	}

	@Override
	public void resume() {

		if (pauseOnScroll) {
			imageLoader.resume();
		}
	}

	@Override
	public void pause() {

		if (pauseOnScroll) {
			imageLoader.pause();
		}

	}

}
