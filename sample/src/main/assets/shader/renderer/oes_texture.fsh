#extension GL_OES_EGL_image_external : require
precision highp float;
varying highp vec2 textureCoordinate;
uniform samplerExternalOES inputImageTexture;


vec4 textureColor(){
    return texture2D(inputImageTexture, textureCoordinate);
}

void setOutColor(vec4 color){
    gl_FragColor = color;
}