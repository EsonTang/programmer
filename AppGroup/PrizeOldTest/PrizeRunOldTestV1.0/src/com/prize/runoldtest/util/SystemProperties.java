
package com.prize.runoldtest.util;


import java.lang.reflect.Method;

public class SystemProperties {

    public static String get(String key) {
        return (String) invokationHandler(key);
    }

    public static String get(String key, String def) {
        String result = (String) invokationHandler(key, def);
        return result != null ? result : def;
    }

    public static long getLong(String key, long def) {
        Long result = (Long) invokationHandler(key, def);
        return result != null ? result : def;
    }

    public static boolean getBoolean(String key, boolean def) {
        Boolean result = (Boolean) invokationHandler(key, def);
        return result != null ? result : def;
    }

    public static void set(String key, String value) {
        invokationHandler(key, value);
    }

    public static void addChangeCallback(Runnable callback) {
        invokationHandler(callback);
    }

    private static Object invokationHandler(Object... params) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int index = 0;
        for (; index < stackTrace.length; index++) {
            if (stackTrace[index].getMethodName().equals("invokationHandler")) {
                index++;
                break;
            }
        }
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            Class<?>[] paramTypes = params != null ? new Class<?>[params.length] : new Class<?>[] {};
            if (params != null) {
                for (int i = 0; i < paramTypes.length; i++)
                    paramTypes[i] = params[i].getClass();
            }
            Method targeMethod = SystemProperties.getDeclaredMethod(stackTrace[index].getMethodName(), paramTypes);
            targeMethod.setAccessible(true);
            // Log.d("found mehtod [{}]", targeMethod);
            return targeMethod.invoke(null, params);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
