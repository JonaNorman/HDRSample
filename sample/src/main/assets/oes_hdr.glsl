#extension GL_OES_EGL_image_external : require
precision highp float;
precision highp int;

varying highp vec2 textureCoordinate;
uniform samplerExternalOES inputImageTexture;
uniform vec2 inputTextureSize;
uniform float maxMipmapLevel;
uniform float mipmapLevel;
uniform vec2 viewPortSize;
uniform bool inputTexturePreMul;
uniform float renderTime;
uniform float renderDuration;

vec2 pV[4];
// |0  |1
//
// |2  |3

vec2 pH[3];
//	- 2
//	- 1
//	- 0

vec2 uv;
vec2 pixel;
int SIZE = 30;
vec2 SEGMENT;
float KERNING = 1.3;
const ivec2 DIGITS = ivec2(4, 4);

void globalInit(){
    pixel = 2.0/viewPortSize.xy;
    SEGMENT = pixel * vec2(SIZE, 1.0);
}

void fillNumbers(){
    pV[0] = vec2(0, SIZE);  pV[1] = vec2(SIZE - 1, SIZE);
    pV[2] = vec2(0, 0); 	pV[3] = vec2(SIZE - 1, 0);

    for (int i = 0; i < 3; i++)
    pH[i] = vec2(0, SIZE * i);

}

vec2 digitSegments(int d){
    vec2 v;
    if (d == 0) v = vec2(.11115, .1015);
    if (d == 1) v = vec2(.01015, .0005);
    if (d == 2) v = vec2(.01105, .1115);
    if (d == 3) v = vec2(.01015, .1115);
    if (d == 4) v = vec2(.11015, .0105);
    if (d == 5) v = vec2(.10015, .1115);
    if (d == 6) v = vec2(.10115, .1115);
    if (d == 7) v = vec2(.01015, .0015);
    if (d == 8) v = vec2(.11115, .1115);
    if (d == 9) v = vec2(.11015, .1115);
    return v;
}

vec2 step2(vec2 edge, vec2 v){
    return vec2(step(edge.x, v.x), step(edge.y, v.y));
}

float segmentH(vec2 pos){
    vec2 sv = step2(pos, uv) - step2(pos + SEGMENT.xy, uv);
    return step(1.1, length(sv));
}

float segmentV(vec2 pos){
    vec2 sv = step2(pos, uv) - step2(pos + SEGMENT.yx, uv);
    return step(1.1, length(sv));
}

float nextDigit(inout float f){
    f = fract(f) * 10.0;
    return floor(f);
}

float drawDigit(int d, vec2 pos){
    vec4 sv = vec4(1.0, 0.0, 1.0, 0.0);
    vec3 sh = vec3(1.0);
    float c = 0.0;

    vec2 v = digitSegments(d);

    for (int i = 0; i < 4; i++)
    c += segmentV(pos + pixel.x * pV[i]) * nextDigit(v.x);

    for (int i = 0; i < 3; i++)
    c += segmentH(pos + pixel.x * pH[i]) * nextDigit(v.y);

    return c;
}

float printNumber(float f, vec2 pos){
    float c = 0.0;
    f /= pow(10.0, float(DIGITS.x));

    for (int i = 0; i < DIGITS.x; i++){
        c += drawDigit(int(nextDigit(f)), pos);
        pos += KERNING * pixel * vec2(SIZE, 0.0);
    }

    for (int i = 0; i < DIGITS.y; i++){
        pos += KERNING * pixel * vec2(SIZE, 0.0);
        c += drawDigit(int(nextDigit(f)), pos);
    }
    return c;
}


vec3 bt2020_10bit_limit_yuv_to_rgb(vec3 yuv){
    return mat3(
    1.000000, 1.000000, 1.000000,
    -0.000000, -0.164553, 1.881400,
    1.474600, -0.571353, -0.000000
    )*mat3(
    1023.0/(235.0*4.0-16.0*4.0), 0.0, 0.0,
    0.0, 1023.0/(240.0*4.0-16.0*4.0), 0.0,
    0.0, 0.0, 1023.0/(240.0*4.0-16.0*4.0)
    )*(yuv-vec3(64.0, 512.0, 512.0));
}

void main()
{

    vec4 color  = texture2D(inputImageTexture, textureCoordinate);

//    mat3 tonemap = mat3(1.6605, -0.1246, -0.0182,
//    -0.5876,  1.1329, -0.1006,
//    -0.0728, -0.0083,  1.1187);
//    vec3 tonemapColor = tonemap * color.rgb;
    gl_FragColor = vec4(color.rgb,1.0);
//    globalInit();
//
//    uv = vec2(textureCoordinate.x,1.0-textureCoordinate.y);
//
//    fillNumbers();
//    vec3 aaa = bt2020_10bit_limit_yuv_to_rgb(vec3(188,444,752));
//
//    gl_FragColor = vec4(0.1)+vec4(printNumber(aaa.g, vec2(0.4)));

//    if(aaa.g<3.0){
//        gl_FragColor = vec4(1.0,0.0,0.0,1.0);
//    }else{
//        gl_FragColor = vec4(0.0,1.0,0.0,1.0);
//    }
}

