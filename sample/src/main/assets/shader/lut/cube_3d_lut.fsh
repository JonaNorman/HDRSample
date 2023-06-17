#version 300 es
#extension GL_OES_EGL_image_external : require
precision highp float;
precision highp sampler3D;
in  vec2 textureCoordinate;
out vec4 outColor;

uniform samplerExternalOES inputImageTexture;
uniform sampler3D cubeLutTexture;
uniform float cubeLutSize;
uniform bool cubeLutEnable;


void main() {

    // Based on \\\\GPU Gems 2 — Chapter 24. Using Lookup Tables to Accelerate Color Transformations\\\\
    // More info and credits @ http://http.developer.nvidia.com/GPUGems2/gpugems2_chapter24.html
    vec4 rawColor = texture(inputImageTexture, textureCoordinate);
    if(cubeLutEnable) {
        // Compute the 3D LUT lookup scale/offset factor
        vec3 scale = vec3((cubeLutSize - 1.0) / cubeLutSize);
        vec3 offset = vec3(1.0 / (2.0 * cubeLutSize));
        outColor.rgb = texture(cubeLutTexture, scale * rawColor.rgb + offset).rgb;
    } else {
        outColor.rgb = rawColor.rgb;
    }
    outColor.a = rawColor.a;
}