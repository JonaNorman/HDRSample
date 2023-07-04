package com.norman.android.hdrsample.player.view;

public abstract class ScaleMode {


    public abstract float getWidth(float sourceWidth, float sourceHeight,
                                   float targetWidth, float targetHeight);

    public abstract float getHeight(float sourceWidth, float sourceHeight,
                                    float targetWidth, float targetHeight);


    public static final ScaleMode FIT = new ScaleMode() {


        @Override
        public float getWidth(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            float targetRatio = targetHeight / targetWidth;
            float sourceRatio = sourceHeight / sourceWidth;
            float scale = targetRatio > sourceRatio ? targetWidth / sourceWidth : targetHeight / sourceHeight;
            return sourceWidth * scale;
        }

        @Override
        public float getHeight(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            float targetRatio = targetHeight / targetWidth;
            float sourceRatio = sourceHeight / sourceWidth;
            float scale = targetRatio > sourceRatio ? targetWidth / sourceWidth : targetHeight / sourceHeight;
            return sourceHeight * scale;
        }
    };

    public static final ScaleMode CONTAIN = new ScaleMode() {
        @Override
        public float getWidth(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            float targetRatio = targetHeight / targetWidth;
            float sourceRatio = sourceHeight / sourceWidth;
            float scale = targetRatio > sourceRatio ? targetWidth / sourceWidth : targetHeight / sourceHeight;
            if (scale > 1) scale = 1;
            return sourceWidth * scale;
        }

        @Override
        public float getHeight(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            float targetRatio = targetHeight / targetWidth;
            float sourceRatio = sourceHeight / sourceWidth;
            float scale = targetRatio > sourceRatio ? targetWidth / sourceWidth : targetHeight / sourceHeight;
            if (scale > 1) scale = 1;
            return sourceHeight * scale;
        }
    };

    public static final ScaleMode FILL = new ScaleMode() {
        @Override
        public float getWidth(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            float targetRatio = targetHeight / targetWidth;
            float sourceRatio = sourceHeight / sourceWidth;
            float scale = targetRatio < sourceRatio ? targetWidth / sourceWidth : targetHeight / sourceHeight;
            return sourceWidth * scale;
        }

        @Override
        public float getHeight(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            float targetRatio = targetHeight / targetWidth;
            float sourceRatio = sourceHeight / sourceWidth;
            float scale = targetRatio < sourceRatio ? targetWidth / sourceWidth : targetHeight / sourceHeight;
            return sourceHeight * scale;
        }
    };


    public static final ScaleMode FILL_X = new ScaleMode() {
        @Override
        public float getWidth(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            float scale = targetWidth / sourceWidth;
            return sourceWidth * scale;
        }

        @Override
        public float getHeight(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            float scale = targetWidth / sourceWidth;
            return sourceHeight * scale;
        }
    };

    public static final ScaleMode FILL_Y = new ScaleMode() {
        @Override
        public float getWidth(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            float scale = targetHeight / sourceHeight;
            return sourceWidth * scale;
        }

        @Override
        public float getHeight(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            float scale = targetHeight / sourceHeight;
            return sourceHeight * scale;
        }
    };

    public static final ScaleMode STRETCH = new ScaleMode() {
        @Override
        public float getWidth(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            return targetWidth;
        }

        @Override
        public float getHeight(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            return targetHeight;
        }
    };


    public static final ScaleMode STRETCH_X = new ScaleMode() {
        @Override
        public float getWidth(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            return targetWidth;
        }

        @Override
        public float getHeight(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            return sourceHeight;
        }
    };

    public static final ScaleMode STRETCH_Y = new ScaleMode() {
        @Override
        public float getWidth(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            return sourceWidth;
        }

        @Override
        public float getHeight(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            return targetHeight;
        }
    };


    public static final ScaleMode NONE = new ScaleMode() {
        @Override
        public float getWidth(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            return sourceWidth;
        }

        @Override
        public float getHeight(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
            return sourceHeight;
        }
    };
}
