#ifndef __ERR_CODE_H
#define __ERR_CODE_H
typedef enum {
	FAILURE = -1,
	SUCCESS = 0,
	/*default err*/
	ERR_DEFAULT = 0x80,
	/*no device*/
	ERR_NODEV,
	/*no memory for allocate*/
	ERR_NOMEM,
	/*Timeout*/
	ERR_TIMEOUT,
	/*no space for use.*/
	ERR_NOSPACE,
	/*wrong input parameter*/
	ERR_BADPARAM,
	/*not alignment*/
	ERR_NOALIGN,
	/*device is busying*/
    //[add zhaoyi 20140714
    	/*read chip failed*/
	ERR_READ_CHIP_FAILED,
	 /*write chip failed*/
	ERR_WRITE_CHIP_FAILED,
	/*password invaild*/
	ERR_PWD_INVAILD,
    //]add zhaoyi 20140714	
	ERR_BUSYING,
	ERR_NEEDRETRY,
	ERR_NOPERMISSION,
	ERR_WRONGPASSWORD,
	ERR_PREMISSION_LIMIT,
	/*Unknown error*/
	ERR_UNKNOWN,
	ENC_ERROR_SHA1,
	ENC_ERROR_ILLEGAL_ADDR,
	ENC_ERROR_UNKNOWN,
	DEC_ERROR_ILLEGAL_ADDR,
	DEC_ERROR_SHA1,
	DEC_ERROR_UNKNOWN,
}ErrCode;
#endif //__ERR_CODE_H

