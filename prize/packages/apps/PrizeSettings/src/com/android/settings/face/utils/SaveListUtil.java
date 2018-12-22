package com.android.settings.face.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/25.
 */

public class SaveListUtil {
    public static void saveList(Context context, List<String> list) {
        SpUtil.saveData(context, "ListSize", list.size());
        for (int i = 0; i < list.size(); i++) {
            SpUtil.saveData(context, "" + i, list.get(i));
        }
    }

    public static List<String> getList(Context context) {
        List<String> list = new ArrayList<>();
        int listSize = (int) SpUtil.getData(context, "ListSize", 0);
        if (listSize != 0) {
            for (int i = 0; i < listSize; i++) {
                String data = (String) SpUtil.getData(context, "" + i, "");
                list.add(data);
            }
        }
        return list;
    }
}
