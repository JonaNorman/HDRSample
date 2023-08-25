package com.norman.android.hdrsample.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import com.norman.android.hdrsample.exception.IORuntimeException;

import java.io.IOException;

public class FileUtil {

    /**
     * 获取asset目录文件的文件描述符
     * @param assetName
     * @return
     */
    public static AssetFileDescriptor openAssetFileDescriptor(String assetName) {
        try {
            Context context = AppUtil.getAppContext();
            AssetManager assetManager = context.getAssets();
            return assetManager.openFd(assetName);
        } catch (IOException e) {
            throw  new IORuntimeException(e);
        }
    }
}
