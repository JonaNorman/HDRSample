#version 300 es
#extension GL_OES_EGL_image_external : require
#extension GL_EXT_YUV_target : require
precision highp float;
precision highp int;

uniform __samplerExternal2DY2YEXT inputImageTexture;
uniform vec2 inputTextureSize;
uniform float maxMipmapLevel;
uniform float mipmapLevel;
uniform vec2 viewPortSize;
uniform bool inputTexturePreMul;
uniform float renderTime;
uniform float renderDuration;

in  vec2 textureCoordinate;
out vec4 outColor;

vec4 yuv_to_rgb(vec4 yuv){
    mat4 colorMat = mat4(
    1.167808, 1.167808, 1.167808, 0.0,
    0.0, -0.187877, 2.148072, 0.0,
    1.683611, -0.652337, 0.000000, 0.0,
    -0.915688, 0.347458, -1.148145, 1.0
    );
    return colorMat* yuv;
}

void main()
{
    vec4 yuv  = texture(inputImageTexture, textureCoordinate);
    vec4 rgb = yuv_to_rgb(yuv);
    outColor = vec4(rgb.xyz, 1.0);
}

