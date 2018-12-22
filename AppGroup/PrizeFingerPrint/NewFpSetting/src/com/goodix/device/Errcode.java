package com.goodix.device;

public class Errcode
{
    public static final int FAILURE = -1;
    public static final int SUCCESS = 0;
    /*default err*/
    public static final int ERR_DEFAULT = 0x80;
    /*no device*/
    public static final int ERR_NODEV = ERR_DEFAULT+1;
    /*no memory for allocate*/
    public static final int ERR_NOMEM = ERR_DEFAULT+2;
    /*Timeout*/
    public static final int ERR_TIMEOUT = ERR_DEFAULT+3;
    /*no space for use.*/
    public static final int ERR_NOSPACE = ERR_DEFAULT+4;
    /*wrong input parameter*/
    public static final int ERR_BADPARAM = ERR_DEFAULT+5;
    /*not alignment*/
    public static final int ERR_NOALIGN = ERR_DEFAULT+6;
    /*device is busying*/
    public static final int ERR_BUSYING = ERR_DEFAULT+7;
    public static final int ERR_NEEDRETRY = ERR_DEFAULT+8;
    public static final int ERR_NOPERMISSION = ERR_DEFAULT+9;
    public static final int ERR_WRONGPASSWORD = ERR_DEFAULT+10;
    /*Unknown error*/
    public static final int ERR_UNKNOWN = ERR_DEFAULT+11;
    public static final int ENC_ERROR_SHA1 = ERR_DEFAULT+12;
    public static final int ENC_ERROR_ILLEGAL_ADDR = ERR_DEFAULT+13;
    public static final int ENC_ERROR_UNKNOWN = ERR_DEFAULT+14;
    public static final int DEC_ERROR_ILLEGAL_ADDR = ERR_DEFAULT+15;
    public static final int DEC_ERROR_SHA1 = ERR_DEFAULT+16;
    public static final int DEC_ERROR_UNKNOWN = ERR_DEFAULT+17;
    public static String getString(int errcode)
    {
        switch(errcode)
        {
            case FAILURE:
                return "";
            case SUCCESS:
                return "SUCCESS";
            case ERR_DEFAULT:
                return "";
            case ERR_NODEV:
                return "";
            case ERR_NOMEM:
                return "";
            case ERR_TIMEOUT:
                return "ERR_TIMEOUT";
            case ERR_NOSPACE:
                return "";
            case ERR_BADPARAM:
                return "ERR_BADPARAM";
            case ERR_NOALIGN:
                return "";
            case ERR_BUSYING:
                return "";
            case ERR_NEEDRETRY:
                return "";
            case ERR_NOPERMISSION:
                return "ERR_NOPERMISSION";
            case ERR_WRONGPASSWORD:
                return "ERR_WRONGPASSWORD";
            case ERR_UNKNOWN:
                return "";
            case ENC_ERROR_SHA1:
                return "";
            case ENC_ERROR_ILLEGAL_ADDR:
                return "";
            case ENC_ERROR_UNKNOWN:
                return "";
            case DEC_ERROR_ILLEGAL_ADDR:
                return "";
            case DEC_ERROR_SHA1:
                return "";
            case DEC_ERROR_UNKNOWN:
                return "";
                default:
                    return ""+errcode;
        }
    }
}
