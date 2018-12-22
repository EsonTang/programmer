package com.example.longshotscreen.ui;

public class GossipItem  {	
	private String title;
	private int pictureSource;
	private int index;
	public GossipItem (String title, int pictureSource, int index){
		this.title =title;
		this.index = index;
		this.pictureSource = pictureSource;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public int getPrctureSource() {
		return pictureSource;
	}
	
	public void setPrctureSource(int pictureSource) {
		this.pictureSource = pictureSource;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
}
