package com.norman.android.hdrsample.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import com.norman.android.hdrsample.player.source.AssetFileSource;

import java.io.IOException;

public class FileUtil {

   public static AssetFileDescriptor openAssetFileDescriptor(String assetName){
      AssetFileDescriptor assetFileDescriptor = null;
      try {
         Context context = AppUtil.getAppContext();
         AssetManager assetManager = context.getAssets();
         assetFileDescriptor = assetManager.openFd(assetName);
         return assetFileDescriptor;
      } catch (IOException e) {
         try {
            if (assetFileDescriptor != null) {
               assetFileDescriptor.close();
            }
         } catch (IOException ex) {
         }
         ExceptionUtil.throwRuntime(e);
         return null;
      }
   }
}
