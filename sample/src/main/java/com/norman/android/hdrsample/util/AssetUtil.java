package com.norman.android.hdrsample.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

public class AssetUtil {

    public static List<String> list(String path) {
        List<String> fileList = new ArrayList<>();
        try {
            Context context = AppUtil.getAppContext();
            Resources resources = context.getResources();
            AssetManager assetManager = resources.getAssets();
            String[] names = assetManager.list(path);
            for (String name : names) {
                fileList.add(path + "/" + name);
            }
        } catch (Exception ignore) {

        }
        return fileList;
    }
}
