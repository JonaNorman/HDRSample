#version 300 es
#extension GL_OES_EGL_image_external : require
#extension GL_EXT_YUV_target : require
precision highp float;
uniform __samplerExternal2DY2YEXT inputImageTexture;
uniform mat4 yuvToRgbMatrix;
in  vec2 textureCoordinate;
out vec4 outColor;

#define YUV_TO_RGB_MAT4    mat4(1.167808, 1.167808, 1.167808, 0.0, \
                                                0.0, -0.187877, 2.148072, 0.0, \
                                                1.683611, -0.652337, 0.000000, 0.0, \
                                                -0.915688, 0.347458, -1.148145, 1.0)

vec4 textureColor(){
    vec4 yuv =  texture(inputImageTexture, textureCoordinate);
    vec4 color = yuvToRgbMatrix *yuv;
    return vec4(color.rgb, 1.0);
}

void setOutColor(vec4 color){
    outColor = color;
}

