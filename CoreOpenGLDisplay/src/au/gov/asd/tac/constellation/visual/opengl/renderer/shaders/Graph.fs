#version 330 core

in highp vec2 textureCoordinate;
uniform sampler2D graphTexture;

out vec4 fragColor;

void main(void) {
    fragColor = texture(graphTexture, textureCoordinate) /*vec4(0.0, 1.0, 1.0, 1.0)*/;
}