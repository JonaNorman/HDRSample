package com.norman.android.hdrsample.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import com.norman.android.hdrsample.player.source.AssetFileSource;

import java.io.IOException;

public class FileUtil {

    public static AssetFileDescriptor openAssetFileDescriptor(String assetName) {
        try {
            Context context = AppUtil.getAppContext();
            AssetManager assetManager = context.getAssets();
            return assetManager.openFd(assetName);
        } catch (IOException e) {
            ExceptionUtil.throwRuntime(e);
            return null;
        }
    }
}
