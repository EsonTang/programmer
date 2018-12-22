#ifndef __ERR_CODE_H
#define __ERR_CODE_H
typedef enum
{
	FAILURE = -1, SUCCESS = 0,
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
	//[add zhaoyi 20140804
	ERR_REG_BAD_IMAGE,
	ERR_REG_STITCH,
	ERR_REG_NO_STITCH,
	ERR_REG_NO_EXTRA_INFO,
	ERR_REG_LOW_COVER,
	//]add zhaoyi 20140804
	ERR_MATCH_LOW_COVER,
	ERR_REG_DUPLICATE_REG,
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

	//[add hupeng 20141018
	ERR_OPEN_FILE_FAILED,
	//]add hupeng 20141018

	DEC_ERROR_UNKNOWN,
	DEC_ERROR_INVAILD_DATA,	
	DEC_ERROR_INVAILD_GUID,/*zhaoyi add 20141119,for check guid*/
	DEC_ERROR_INVAILD_USERCODE,
} ErrCode;
#endif //__ERR_CODE_H
