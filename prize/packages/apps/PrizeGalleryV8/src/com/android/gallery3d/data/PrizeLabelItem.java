package com.android.gallery3d.data;

import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;

import com.android.gallery3d.util.ThreadPool.Job;

public class PrizeLabelItem extends MediaItem {

	public String title;
	
	public int count;
	
	public PrizeLabelItem(Path path, long version) {
		super(path, version);
	}

	@Override
	public Job<Bitmap> requestImage(int type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Job<BitmapRegionDecoder> requestLargeImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMimeType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

}
