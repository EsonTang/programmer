package com.prize.smartcleaner;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * Created by xiarui on 2018/1/16.
 */

public class MyFileFilter implements FileFilter {

    @Override
    public boolean accept(File file) {
        if (Pattern.matches("^[0-9]*$", file.getName())) {
            return true;
        }
        return false;
    }
}
