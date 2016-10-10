package com.qingsongchou.library.photopick.utils;

import android.text.TextUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wsl on 16-4-26.
 */
public final class ValidUtil {

    private static final int LENGTH = 10;
    /**
     * 图片大小限制
     */
    private static final int MAX_IMG_SIZE = 10 * 1024 * 1024;

    public static boolean valid(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
            byte[] data = new byte[LENGTH];
            inputStream.read(data);
            if (sizeValid(inputStream)) return false;
            return binaryValid(data);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 判断当前图片大小是否超过10m
     * @param inputStream
     * @return
     * @throws IOException
     */
    private static boolean sizeValid(InputStream inputStream) throws IOException {
        if (inputStream.available() >= MAX_IMG_SIZE) { // 大于10m不加载
            return true;
        }
        return false;
    }

//    private static boolean binaryValid(byte[] data) {
//        if (data == null || data.length <= 0) {
//            return false;
//        }
//        if (data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
//            return true;
//        }
//        // JPG test:
//        if (data[6] == 'J' && data[7] == 'F' && data[8] == 'I'
//                && data[9] == 'F') {
//            return true;
//        }
//        return false;
//    }

    private static boolean binaryValid(byte[] data) {
        if (data == null || data.length <= 0) {
            return false;
        }

        //jpeg
        if (((data[0] & 0xFF) == 0xFF) && ((data[1] & 0xFF) == 0xD8)) {
            return true;
        }

        //png
        if (((data[0] & 0xFF) == 0x89) &&
                ((data[1] & 0xFF) == 0x50) &&
                ((data[2] & 0xFF) == 0x4E) &&
                ((data[3] & 0xFF) == 0x47) &&
                ((data[4] & 0xFF) == 0x0D) &&
                ((data[5] & 0xFF) == 0x0A) &&
                ((data[6] & 0xFF) == 0x1A) &&
                ((data[7] & 0xFF) == 0x0A)) {
            return true;
        }
        return false;
    }

    private static String getType(byte[] data) {
        String type = null;
        // Png test:
        if (data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
            type = "PNG";
            return type;
        }
        // Gif test:
        if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F') {
            type = "GIF";
            return type;
        }
        // JPG test:
        if (data[6] == 'J' && data[7] == 'F' && data[8] == 'I'
                && data[9] == 'F') {
            type = "JPG";
            return type;
        }
        return type;
    }
}
