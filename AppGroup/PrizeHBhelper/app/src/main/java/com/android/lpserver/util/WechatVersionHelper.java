package com.android.lpserver.util;

/**
 * Created by prize on 2017/2/5.
 * 6.5.10——1080
 */

public class WechatVersionHelper {
    public static final String MONEY_RECEIVE_ACTIVITY_NAME = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";   //prize modify public by zj 20180105
    private static final String MONEY_RECEIVE_ACTIVITY_NAME_1020 = "com.tencent.mm.plugin.luckymoney.ui.En_fba4b94f";

    //
    private static final String MONEY_TEXT_ID_1100 = "com.tencent.mm:id/bli";
    private static final String MONEY_TEXT_ID_1080 = "com.tencent.mm:id/bk6";
    private static final String MONEY_TEXT_ID_1060 = "com.tencent.mm:id/bif";
    private static final String MONEY_TEXT_ID_1020 = "com.tencent.mm:id/bfw";
    private static final String MONEY_TEXT_ID_1000 = "com.tencent.mm:id/bek";
    private static final String MONEY_TEXT_ID_980 = "com.tencent.mm:id/bbe";
    private static final String MONEY_TEXT_ID_960 = "com.tencent.mm:id/bam";
    private static final String MONEY_TEXT_ID_940 = "com.tencent.mm:id/bdq";
    private static final String MONEY_TEXT_ID_920 = "com.tencent.mm:id/bdq";
    private static final String MONEY_TEXT_ID_900 = "com.tencent.mm:id/bd2";

    //
    private static final String SINGLE_CHAT_MESSAGE_ID_1100 = "com.tencent.mm:id/bm0";
    private static final String SINGLE_CHAT_MESSAGE_ID_1080 = "com.tencent.mm:id/bkn";
    private static final String SINGLE_CHAT_MESSAGE_ID_1060 = "com.tencent.mm:id/bix";
    private static final String SINGLE_CHAT_MESSAGE_ID_1020 = "com.tencent.mm:id/bgd";
    private static final String SINGLE_CHAT_MESSAGE_ID_1000 = "com.tencent.mm:id/bf2";
    private static final String SINGLE_CHAT_MESSAGE_ID_980 = "com.tencent.mm:id/bbw";
    private static final String SINGLE_CHAT_MESSAGE_ID_960 = "com.tencent.mm:id/bb4";
    private static final String SINGLE_CHAT_MESSAGE_ID_940 = "com.tencent.mm:id/be8";
    private static final String SINGLE_CHAT_MESSAGE_ID_920 = "com.tencent.mm:id/be8";
    private static final String SINGLE_CHAT_MESSAGE_ID_900 = "com.tencent.mm:id/bdj";

    //
    private static final String OPEN_BUTTON_ID_1100 = "com.tencent.mm:id/bp6";
    private static final String OPEN_BUTTON_ID_1080 = "com.tencent.mm:id/bnr";
    private static final String OPEN_BUTTON_ID_1060 = "com.tencent.mm:id/bm1";
    private static final String OPEN_BUTTON_ID_1020 = "com.tencent.mm:id/bjj";
    private static final String OPEN_BUTTON_ID_1000 = "com.tencent.mm:id/bi3";
    private static final String OPEN_BUTTON_ID_980 = "com.tencent.mm:id/be_";
    private static final String OPEN_BUTTON_ID_960 = "com.tencent.mm:id/bdh";
    private static final String OPEN_BUTTON_ID_940 = "com.tencent.mm:id/bg7";
    private static final String OPEN_BUTTON_ID_920 = "com.tencent.mm:id/bg7";
    private static final String OPEN_BUTTON_ID_900 = "com.tencent.mm:id/bfi";

    //
    private static final String CHAT_NAME_ID_1100  = "com.tencent.mm:id/gz";
    private static final String CHAT_NAME_ID_1080  = "com.tencent.mm:id/gs";
    private static final String CHAT_NAME_ID_1060  = "com.tencent.mm:id/gr";
    private static final String CHAT_NAME_ID_1020 = "com.tencent.mm:id/gh";
    private static final String CHAT_NAME_ID_1000 = "com.tencent.mm:id/gh";
    private static final String CHAT_NAME_ID_980 = "com.tencent.mm:id/gd";
    private static final String CHAT_NAME_ID_960 = "com.tencent.mm:id/gc";
    private static final String CHAT_NAME_ID_940 = "com.tencent.mm:id/g1";
    private static final String CHAT_NAME_ID_920 = "com.tencent.mm:id/g1";
    private static final String CHAT_NAME_ID_900 = "com.tencent.mm:id/g0";

    private static final String SENDER_NAME_ID_1100 = "com.tencent.mm:id/ble";
    private static final String SENDER_NAME_ID_1120 = "com.tencent.mm:id/bo2";

    public static String getMoneyTextId(int versionCode) {
        String moneyTextId = null;
        switch(versionCode) {
            case 1100:
                moneyTextId = MONEY_TEXT_ID_1100;
                break;
            case 1080:
                moneyTextId = MONEY_TEXT_ID_1080;
                break;
            case 1060:
                moneyTextId = MONEY_TEXT_ID_1060;
                break;
            case 1041:
                moneyTextId = MONEY_TEXT_ID_1020;
                break;
            case 1020:
                moneyTextId = MONEY_TEXT_ID_1020;
                break;
            case 1000:
                moneyTextId = MONEY_TEXT_ID_1000;
                break;
            case 980:
                moneyTextId = MONEY_TEXT_ID_980;
                break;
            case 960:
                moneyTextId = MONEY_TEXT_ID_960;
                break;
            case 940:
                moneyTextId = MONEY_TEXT_ID_940;
                break;
            case 920:
                moneyTextId = MONEY_TEXT_ID_920;
                break;
            case 900:
                moneyTextId = MONEY_TEXT_ID_900;
                break;
            default:
                break;
        }
        return moneyTextId;
    }

    public static String getSingleChatMessageId(int versionCode) {
        String singleChatMessageId = null;
        switch (versionCode) {
            case 1100:
                singleChatMessageId = SINGLE_CHAT_MESSAGE_ID_1100;
                break;
            case 1080:
                singleChatMessageId = SINGLE_CHAT_MESSAGE_ID_1080;
                break;
            case 1060:
                singleChatMessageId = SINGLE_CHAT_MESSAGE_ID_1060;
                break;
            case 1041:
                singleChatMessageId = SINGLE_CHAT_MESSAGE_ID_1020;
                break;
            case 1020:
                singleChatMessageId = SINGLE_CHAT_MESSAGE_ID_1020;
                break;
            case 1000:
                singleChatMessageId = SINGLE_CHAT_MESSAGE_ID_1000;
                break;
            case 980:
                singleChatMessageId = SINGLE_CHAT_MESSAGE_ID_980;
                break;
            case 960:
                singleChatMessageId = SINGLE_CHAT_MESSAGE_ID_960;
                break;
            case 940:
                singleChatMessageId = SINGLE_CHAT_MESSAGE_ID_940;
                break;
            case 920:
                singleChatMessageId = SINGLE_CHAT_MESSAGE_ID_920;
                break;
            case 900:
                singleChatMessageId = SINGLE_CHAT_MESSAGE_ID_900;
                break;
            default:
                break;
        }
        return singleChatMessageId;
    }

    public static String getOpenButtonId(int versionCode) {
        String openButtonId = null;
        switch (versionCode) {
            case 1100:
                openButtonId = OPEN_BUTTON_ID_1100;
                break;
            case 1080:
                openButtonId = OPEN_BUTTON_ID_1080;
                break;
            case 1060:
                openButtonId = OPEN_BUTTON_ID_1060;
                break;
            case 1041:
                openButtonId = OPEN_BUTTON_ID_1020;
                break;
            case 1020:
                openButtonId = OPEN_BUTTON_ID_1020;
                break;
            case 1000:
                openButtonId = OPEN_BUTTON_ID_1000;
                break;
            case 980:
                openButtonId = OPEN_BUTTON_ID_980;
                break;
            case 960:
                openButtonId = OPEN_BUTTON_ID_960;
                break;
            case 940:
                openButtonId = OPEN_BUTTON_ID_940;
                break;
            case 920:
                openButtonId = OPEN_BUTTON_ID_920;
                break;
            case 900:
                openButtonId = OPEN_BUTTON_ID_900;
                break;
            default:
                break;
        }
        return openButtonId;
    }

    public static String getChatNameId(int versionCode) {
        String chatNameId = null;
        switch (versionCode) {
            case 1100:
                chatNameId = CHAT_NAME_ID_1100;
                break;
            case 1080:
                chatNameId = CHAT_NAME_ID_1080;
                break;
            case 1060:
                chatNameId = CHAT_NAME_ID_1060;
                break;
            case 1041:
                chatNameId = CHAT_NAME_ID_1020;
                break;
            case 1020:
                chatNameId = CHAT_NAME_ID_1020;
                break;
            case 1000:
                chatNameId = CHAT_NAME_ID_1000;
                break;
            case 980:
                chatNameId = CHAT_NAME_ID_980;
                break;
            case 960:
                chatNameId = CHAT_NAME_ID_960;
                break;
            case 940:
                chatNameId = CHAT_NAME_ID_940;
                break;
            case 920:
                chatNameId = CHAT_NAME_ID_920;
                break;
            case 900:
                chatNameId = CHAT_NAME_ID_900;
                break;
            default:
                break;
        }
        return chatNameId;
    }

    public static String getMoneyReceiveActivityName(int versionCode){
        if(versionCode >= 1020){
            return MONEY_RECEIVE_ACTIVITY_NAME_1020;
        }
        return MONEY_RECEIVE_ACTIVITY_NAME;
    }

    public static String getSenderNameId(int versionCode) {
        String senderNameId = null;
        switch (versionCode){
            case 1100:
                senderNameId = SENDER_NAME_ID_1100;
                break;
//            case 1120:
//                senderNameId = SENDER_NAME_ID_1120;
            default:
                break;
        }
        return senderNameId;
    }
}
