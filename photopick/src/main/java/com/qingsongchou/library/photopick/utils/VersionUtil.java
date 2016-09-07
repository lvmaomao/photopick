package com.qingsongchou.library.photopick.utils;

import android.os.Build;

/**
 * Created by admin on 16/3/28.
 */
public class VersionUtil {



    public static boolean isMNC(){
        return Build.VERSION.SDK_INT >= 23;
    }

}
