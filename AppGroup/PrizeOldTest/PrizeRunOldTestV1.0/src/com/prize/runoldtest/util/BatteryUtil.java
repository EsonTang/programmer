package com.prize.runoldtest.util;

import java.io.IOException;
import java.io.InputStream;

public class BatteryUtil {
    public static String getCurrent() {
        String result = "";
        String path = "/sys/class/power_supply/battery/status";
        ProcessBuilder cmd;
        try {
            String[] args = { "/system/bin/cat", path };
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result += new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }
        return result.trim();
    }
}