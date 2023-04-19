#version 300 es
in vec4 position;
in vec4 inputTextureCoordinate;
uniform mat4 positionMatrix;
uniform mat4 scaleTypeMatrix;
uniform mat4 inputTextureMatrix;
out vec2 textureCoordinate;
void main() {
    gl_Position =scaleTypeMatrix*positionMatrix*position;
    textureCoordinate =(inputTextureMatrix*inputTextureCoordinate).xy;
}