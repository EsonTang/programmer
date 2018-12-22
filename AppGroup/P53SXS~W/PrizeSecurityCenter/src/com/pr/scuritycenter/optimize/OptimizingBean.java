package com.pr.scuritycenter.optimize;

/**
 * 
 * @author wangzhong
 *
 */
public class OptimizingBean {

	public final static int STATUS_OPTIMIZATION_BEFORE	= 1;
	public final static int STATUS_OPTIMIZATION			= 2;
	public final static int STATUS_OPTIMIZATION_AFTER	= 3;
	
	String name;
	int status = STATUS_OPTIMIZATION;
	int pic;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getPic() {
		return pic;
	}
	public void setPic(int pic) {
		this.pic = pic;
	}

}
