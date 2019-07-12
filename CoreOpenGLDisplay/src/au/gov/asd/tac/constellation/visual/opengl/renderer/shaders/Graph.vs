#version 330 core

in vec4 position;
in vec4 inputTextureCoordinates;

out vec2 textureCoordinate;

uniform sampler2D depthTexture;

void main(void) {
    textureCoordinate = inputTextureCoordinates.xy;
    gl_Position = position; /*vec4(position.xy, texture2D(graphTexture, textureCoordinate));*/
}