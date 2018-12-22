package com.pr.scuritycenter.setting.blacknum;

/**
 * 
 * @author wangzhong
 *
 */
public class BlackNumberBean {
	
	public final static int STOP_SMS	= 1;
	public final static int STOP_CALL	= 2;
	public final static int STOP_ALL	= 3;
	
    private String number;
    private int mode;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
    
}
