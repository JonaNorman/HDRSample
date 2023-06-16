#version 300 es

precision highp float;
precision highp int;
uniform highp usampler2D yuv_texture;
uniform int strideWidth;
uniform int slideHeight;
uniform int bitDepth;
uniform int bitMask;
uniform int colorFormat;
uniform int colorStandard;
uniform int colorRange;
uniform vec2 viewPortSize;
in  vec2 textureCoordinate;
out vec4 outColor;

#define COLOR_STANDARD_BT709 1
#define COLOR_STANDARD_BT2020 6
#define COLOR_RANGE_FULL 1
#define COLOR_RANGE_LIMITED 2
#define MAX_COLOR_DEPTH  pow(float(2), float(bitDepth)) -1.0


// yuv和rgb互转矩阵 https://github.com/google/skia/blob/main/src/core/SkYUVMath.cpp
vec3 bt709_full_yuv_to_rgb(vec3 yuv){
    return mat3(
    1.000000, 1.00000, 1.0000,
    0.000000, -0.187324, 1.855600,
    1.574800, -0.468124, 0.000000
    )*(yuv-vec3(0, 128, 128));
}

vec3 bt709_limit_yuv_to_rgb(vec3 yuv){
    return mat3(
    1.164384, 1.164384, 1.164384,
    0.000000, -0.213249, 2.112402,
    1.792741, -0.532909, 0.000000
    )*(yuv-vec3(16, 128, 128));
}


vec3 bt2020_10bit_full_yuv_to_rgb(vec3 yuv){
    return mat3(
    1.000000, 1.000000, 1.000000,
    -0.000000, -0.164553, 1.881400,
    1.474600, -0.571353, -0.000000
    )*(yuv-vec3(0.0, 512.0, 512.0));
}

vec3 bt2020_10bit_limit_yuv_to_rgb(vec3 yuv){
    return mat3(
    1.167808, 1.167808, 1.167808,
    -0.000000, -0.187877, 2.148072,
    1.683611, -0.652337, -0.000000
    )*(yuv-vec3(64.0, 512.0, 512.0));
}

vec3 yuv_rgb(vec3 yuv){
    if (colorStandard == COLOR_STANDARD_BT709){
        if (colorRange == COLOR_RANGE_FULL){
            return bt709_full_yuv_to_rgb(yuv);
        } else if (colorRange == COLOR_RANGE_LIMITED){
            return bt709_limit_yuv_to_rgb(yuv);
        }
    } else if (colorStandard == COLOR_STANDARD_BT2020){
        if (colorRange == COLOR_RANGE_FULL){ //full range
            return bt2020_10bit_full_yuv_to_rgb(yuv);
        } else if (colorRange == COLOR_RANGE_LIMITED){ // limit range
            return bt2020_10bit_limit_yuv_to_rgb(yuv);
        }
    }
    return bt709_full_yuv_to_rgb(yuv);
}

uvec3 bit_mask(uvec3 yuv){
    return yuv>>bitMask;
}

vec3 float_yuv(uvec3 yuv){
    return vec3(float(yuv.x), float(yuv.y), float(yuv.z));
}

uvec3 texel_i420(ivec2 pos){
    uint y = texelFetch(yuv_texture, pos, 0).x;
    int index = (pos.y/2)*(strideWidth/2)+pos.x/2;
    int xpos_u = index%strideWidth;
    int ypos_u = index/strideWidth+slideHeight;
    uint  u =  texelFetch(yuv_texture, ivec2(xpos_u, ypos_u), 0).x;
    int xpos_v = index%strideWidth;
    int ypos_v = index/strideWidth+slideHeight*5/4;
    uint  v =  texelFetch(yuv_texture, ivec2(xpos_v, ypos_v), 0).x;
    uvec3 yuv = uvec3(y, u, v);
    return yuv;
}

uvec3 texel_yv12(ivec2 pos){
    uint y = texelFetch(yuv_texture, pos, 0).x;
    int index = (pos.y/2)*(strideWidth/2)+pos.x/2;
    int xpos_u = index%strideWidth;
    int ypos_u = index/strideWidth+slideHeight*5/4;
    uint  u =  texelFetch(yuv_texture, ivec2(xpos_u, ypos_u), 0).x;
    int xpos_v = index%strideWidth;
    int ypos_v = index/strideWidth+slideHeight;
    uint  v =  texelFetch(yuv_texture, ivec2(xpos_v, ypos_v), 0).x;
    uvec3 yuv = uvec3(y, u, v);
    return yuv;
}

uvec3 texel_nv12(ivec2 pos){
    uint y = texelFetch(yuv_texture, pos, 0).x;
    int xpos_u = pos.x/2*2;
    int ypos_u = pos.y/2+slideHeight;
    uint u =  texelFetch(yuv_texture, ivec2(xpos_u, ypos_u), 0).x;
    int xpos_v = pos.x/2*2+1;
    int ypos_v = pos.y/2+slideHeight;
    uint v =  texelFetch(yuv_texture, ivec2(xpos_v, ypos_v), 0).x;
    uvec3 yuv = uvec3(y, u, v);
    return yuv;
}

uvec3 texel_nv21(ivec2 pos){
    uint y = texelFetch(yuv_texture, pos, 0).x;
    int xpos_u = pos.x/2*2+1;
    int ypos_u = pos.y/2+slideHeight;
    uint u =  texelFetch(yuv_texture, ivec2(xpos_u, ypos_u), 0).x;
    int xpos_v = pos.x/2*2;
    int ypos_v = pos.y/2+slideHeight;
    uint v =  texelFetch(yuv_texture, ivec2(xpos_v, ypos_v), 0).x;
    uvec3 yuv = uvec3(y, u, v);
    return yuv;
}

vec3 normalized_color (vec3 color){
    return color/vec3(MAX_COLOR_DEPTH);
}

void main() {
    uvec3 uyuv = uvec3(0u);
    ivec2  pos  = ivec2(int(textureCoordinate.x *float(strideWidth)), int(textureCoordinate.y *float(slideHeight)));
    if (colorFormat == 19){ //i420  Y+U+V
        uyuv = texel_i420(pos);
    } else if (colorFormat == 20){ //YV12 Y+V+U
        uyuv = texel_yv12(pos);
    } else if (colorFormat == 21){ //NV12  Y+UV
        uyuv = texel_nv12(pos);
    } else if (colorFormat == 39){ ///NV21 Y+VU
        uyuv = texel_nv21(pos);
    }
    uyuv = bit_mask(uyuv);
    vec3 yuv = float_yuv(uyuv);
    vec3 rgb = yuv_rgb(yuv);
    vec3 norimalrgb = normalized_color(rgb);

    outColor = vec4(norimalrgb, 1.0);
}