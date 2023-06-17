#version 300 es
in vec4 position;
in vec4 inputTextureCoordinate;
out vec2 textureCoordinate;
void main() {
    gl_Position =position;
    textureCoordinate =inputTextureCoordinate.xy;
}