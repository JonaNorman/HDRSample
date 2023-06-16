#version 300 es


uniform sampler2D tex;
uniform sampler3D lutTexture;

varying vec2 texCoordVarying;

uniform float lutSize;
uniform vec2 mouse;

void main() {

    // Based on "GPU Gems 2 — Chapter 24. Using Lookup Tables to Accelerate Color Transformations"
    // More info and credits @ http://http.developer.nvidia.com/GPUGems2/gpugems2_chapter24.html

    vec3 rawColor = texture2D(tex, texCoordVarying).rgb;

    // Compute the 3D LUT lookup scale/offset factor
    vec3 scale = vec3((lutSize - 1.0) / lutSize);
    vec3 offset = vec3(1.0 / (2.0 * lutSize));

    // ****** Apply 3D LUT color transform! **************
    // This is our dependent texture read; The 3D texture's
    // lookup coordinates are dependent on the
    // previous texture read's result


    vec3 applyLut = texture3D(lutTexture, scale * rawColor + offset).rgb;


    gl_FragColor = vec4(applyLut, rawColor.a);
}