package com.example.longshotscreen.utils;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class ImageRangInfo
{
	public PixelCoor mBegin = new PixelCoor(0.0F, 0.0F);
	public int mBottom;
	public PixelCoor mEnd = new PixelCoor(0.0F, 0.0F);
	protected int mGap;
	public int mLeft;
	public ArrayList<pixelCo> mPathList;
	public int mRight;
	public int mTop;

	public ImageRangInfo()
	{
		if (mPathList == null)
		{
			mPathList = new ArrayList();
		}
	}

	public boolean addPathList(int paramInt1, int paramInt2)
	{
		return mPathList.add(new pixelCo(paramInt1, paramInt2));
	}

	public void checkRang(int x, int y)
	{
		if (x < mLeft)
			mLeft = x;
		if (x > mRight)
			mRight = x;
		if (y < mTop)
			mTop = y;
		if (y > mBottom)
		{
			mBottom = y;
		}
	}

	public ArrayList<pixelCo> getFourUnion(Bitmap bitmap, ArrayList<ImageRangInfo.pixelCo>arrayList)
	{
		ArrayList<ImageRangInfo.pixelCo> list = new ArrayList<ImageRangInfo.pixelCo>();
		for (int i = 0; i < arrayList.size(); i = i + 1)
		{
			int pixelCoX = ((pixelCo)arrayList.get(i)).mx;
			int pixelCoY = ((pixelCo)arrayList.get(i)).my;
			if(pixelCoX >= bitmap.getWidth()) {
				pixelCoX = bitmap.getWidth() - 1;
			}
			if(pixelCoY >= bitmap.getHeight()) {
				pixelCoY = bitmap.getHeight() - 1;
			}

			if ((pixelCoY - mGap >= 0) && (bitmap.getPixel(pixelCoX, pixelCoY - mGap) != 0)){
				list.add(new ImageRangInfo.pixelCo(pixelCoX, pixelCoY));
			}
			else if(((mGap + pixelCoY) < bitmap.getHeight()) && (bitmap.getPixel(pixelCoX, (mGap + pixelCoY)) != 0)) {
				list.add(new ImageRangInfo.pixelCo(pixelCoX, pixelCoY));
			} else if(((pixelCoX - mGap) >= 0) && (bitmap.getPixel((pixelCoX - mGap), pixelCoY) != 0)) {
				list.add(new ImageRangInfo.pixelCo(pixelCoX, pixelCoY));
			}else if(((mGap + pixelCoX) < bitmap.getWidth()) && (bitmap.getPixel((mGap + pixelCoX), pixelCoY) != 0)) {
				list.add(new ImageRangInfo.pixelCo(pixelCoX, pixelCoY));
			}
		}
		return list;
	}

	public void getGap(int gap)
	{
		mGap = gap;
	}

	public void initRang(int x, int y)
	{
		mRight = x;
		mLeft = x;
		mBottom = y;
		mTop = y;
	}

	public class PixelCoor
	{
		public float mx;
		public float my;

		public PixelCoor(float paramFloat1, float arg3)
		{
			this.mx = paramFloat1;
			//Object localObject;
			float localObject = 0;
			my = localObject;
		}

		public void setPixelCoor(float paramFloat1, float paramFloat2)
		{
			mx = paramFloat1;
			my = paramFloat2;
		}
	}

	public class pixelCo
	{
		public int mx;
		public int my;

		public pixelCo(int paramInt1, int arg3)
		{
			mx = paramInt1;
			int i = 0;
			my = i;
		}
	}
}
