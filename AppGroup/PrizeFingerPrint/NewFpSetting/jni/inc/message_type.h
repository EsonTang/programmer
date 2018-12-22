#ifndef __MESSAGE_TYEP_H
#define __MESSAGE_TYEP_H


#undef  MSG_TYPE_COMMON_BASE
#define MSG_TYPE_COMMON_BASE 0L
#undef  MSG_TYPE_COMMON_TOUCH
#define MSG_TYPE_COMMON_TOUCH 1L
#undef  MSG_TYPE_COMMON_UNTOUCH
#define MSG_TYPE_COMMON_UNTOUCH 2L
//[wangbo add 20150413, for GF818 Heart beat demo
#undef  MSG_TYPE_COMMON_HB_DATA
#define MSG_TYPE_COMMON_HB_DATA 3L
//]wangbo add 20150413, for GF818 Heart beat demo
#undef  MSG_TYPE_COMMON_NOTIFY_INFO
#define MSG_TYPE_COMMON_NOTIFY_INFO 7L
#undef  MSG_TYPE_COMMON_NOTIFY_UPDATE
#define MSG_TYPE_COMMON_NOTIFY_UPDATE 8L
#undef  MSG_TYPE_REGISTER_BASE
#define MSG_TYPE_REGISTER_BASE 16L
#undef  MSG_TYPE_REGISTER_PIECE
#define MSG_TYPE_REGISTER_PIECE 17L
#undef  MSG_TYPE_REGISTER_NO_PIECE
#define MSG_TYPE_REGISTER_NO_PIECE 18L
#undef  MSG_TYPE_REGISTER_NO_EXTRAINFO
#define MSG_TYPE_REGISTER_NO_EXTRAINFO 19L
#undef  MSG_TYPE_REGISTER_LOW_COVER
#define MSG_TYPE_REGISTER_LOW_COVER 20L
#undef  MSG_TYPE_REGISTER_BAD_IMAGE
#define MSG_TYPE_REGISTER_BAD_IMAGE 21L
#undef  MSG_TYPE_REGISTER_GET_DATA_FAILED
#define MSG_TYPE_REGISTER_GET_DATA_FAILED 22L
#undef  MSG_TYPE_REGISTER_TIMEOUT
#define MSG_TYPE_REGISTER_TIMEOUT 23L
#undef  MSG_TYPE_REGISTER_COMPLETE
#define MSG_TYPE_REGISTER_COMPLETE 24L
#undef  MSG_TYPE_REGISTER_CANCEL
#define MSG_TYPE_REGISTER_CANCEL 25L
#undef  MSG_TYPE_REGISTER_DUPLICATE_REG
#define MSG_TYPE_REGISTER_DUPLICATE_REG 26L
#undef  MSG_TYPE_RECONGNIZE_BASE
#define MSG_TYPE_RECONGNIZE_BASE 256L
#undef  MSG_TYPE_RECONGNIZE_SUCCESS
#define MSG_TYPE_RECONGNIZE_SUCCESS 257L
#undef  MSG_TYPE_RECONGNIZE_TIMEOUT
#define MSG_TYPE_RECONGNIZE_TIMEOUT 258L
#undef  MSG_TYPE_RECONGNIZE_FAILED
#define MSG_TYPE_RECONGNIZE_FAILED 259L
#undef  MSG_TYPE_RECONGNIZE_BAD_IMAGE
#define MSG_TYPE_RECONGNIZE_BAD_IMAGE 260L
#undef  MSG_TYPE_RECONGNIZE_GET_DATA_FAILED
#define MSG_TYPE_RECONGNIZE_GET_DATA_FAILED 261L
#undef  MSG_TYPE_RECONGNIZE_NO_REGISTER_DATA
#define MSG_TYPE_RECONGNIZE_NO_REGISTER_DATA 262L
#undef 	MSG_TYPE_SERVER_ERROR_DIED
#define MSG_TYPE_SERVER_ERROR_DIED 111L
#undef 	MSG_TYPE_SERVER_RECONNCECT
#define MSG_TYPE_SERVER_RECONNCECT 112L
#undef  MSG_TYPE_DELETE_BASE
#define MSG_TYPE_DELETE_BASE 4096L
#undef  MSG_TYPE_DELETE_SUCCESS
#define MSG_TYPE_DELETE_SUCCESS 4097L
#undef  MSG_TYPE_DELETE_NOEXIST
#define MSG_TYPE_DELETE_NOEXIST 4098L
#undef  MSG_TYPE_DELETE_TIMEOUT
#define MSG_TYPE_DELETE_TIMEOUT 4099L
#undef  MSG_TYPE_ERROR
#define MSG_TYPE_ERROR 65536L



#endif //__ERR_CODE_H
