package com.goodix.device;

public class CommandType {
	
	public static final int GOODIX_CMD_SET_BASE  = 3;
	public static final int GOODIX_ENGTEST_CMD_SET_MODE_KEY  = GOODIX_CMD_SET_BASE;
	public static final int GOODIX_ENGTEST_CMD_SET_MODE_IMG  = GOODIX_CMD_SET_BASE + 1;
	public static final int GOODIX_ENGTEST_CMD_SET_MODE_FF  = GOODIX_CMD_SET_BASE + 2;
	public static final int GOODIX_ENGTEST_CMD_MODE_CHECK = GOODIX_CMD_SET_BASE + 3;
	public static final int GOODIX_ENGTEST_CMD_MODE_FORBID = GOODIX_CMD_SET_BASE + 4;
    public static final int GOODIX_ENGTEST_CMD_UPDATE_BASE = 15;

	public static String getString(int cmd) {
		String commdinfo = null;
		switch (cmd) {
		case GOODIX_ENGTEST_CMD_SET_MODE_KEY:
			commdinfo = "GOODIX_ENGTEST_CMD_SET_MODE_KEY" ;
			break;
		case GOODIX_ENGTEST_CMD_SET_MODE_IMG:
			commdinfo = "GOODIX_ENGTEST_CMD_SET_MODE_IMG";
			break;
		case GOODIX_ENGTEST_CMD_SET_MODE_FF:
			commdinfo = "GOODIX_ENGTEST_CMD_SET_MODE_FF";
			break;
		case GOODIX_ENGTEST_CMD_MODE_CHECK:
			commdinfo = "GOODIX_ENGTEST_CMD_MODE_CHECK";
			break;
		case GOODIX_ENGTEST_CMD_MODE_FORBID:
			commdinfo = "GOODIX_ENGTEST_CMD_MODE_FORBID";

			break;
        case GOODIX_ENGTEST_CMD_UPDATE_BASE:
            commdinfo = "GOODIX_ENGTEST_CMD_UPDATE_BASE";
            break;
		default:
			commdinfo = "unkown command";
			break;
		}
		return commdinfo;
	}

}
