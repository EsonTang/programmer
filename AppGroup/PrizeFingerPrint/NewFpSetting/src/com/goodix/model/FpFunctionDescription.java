package com.goodix.model;

public class FpFunctionDescription {
	private String mShowName;
	private String mShowInstruction;
	private String mDbColumnName;
	
	public FpFunctionDescription() {
		super();
	}

	public FpFunctionDescription(String mShowName, String mShowInstruction,
			String mDbColumnName) {
		super();
		this.mShowName = mShowName;
		this.mShowInstruction = mShowInstruction;
		this.mDbColumnName = mDbColumnName;
	}

	public String getmShowName() {
		return mShowName;
	}

	public void setmShowName(String mShowName) {
		this.mShowName = mShowName;
	}

	public String getmShowInstruction() {
		return mShowInstruction;
	}

	public void setmShowInstruction(String mShowInstruction) {
		this.mShowInstruction = mShowInstruction;
	}

	public String getmDbColumnName() {
		return mDbColumnName;
	}

	public void setmDbColumnName(String mDbColumnName) {
		this.mDbColumnName = mDbColumnName;
	}		
	
	
}
