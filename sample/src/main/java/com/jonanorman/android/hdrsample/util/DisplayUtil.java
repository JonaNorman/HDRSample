package com.jonanorman.android.hdrsample.util;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

public class DisplayUtil {

    public static boolean aa(Context context){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Display.HdrCapabilities hdrCapabilities = display.getHdrCapabilities();

            hdrCapabilities.getSupportedHdrTypes();

        }
        return true;
    }
}
