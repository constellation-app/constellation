#version 330 core

in highp vec2 textureCoordinate;
uniform sampler2D graphTexture;
uniform int greyscale;

out vec4 fragColor;

void main(void) {
    vec4 texColor = texture2D(graphTexture, textureCoordinate);
    if (greyscale != 0) {
        fragColor = vec4(mix(vec3(dot(texColor.rgb, vec3(0.2125, 0.7154, 0.0721))), texColor.rgb, 0.05), texColor.a);
    } else {
        fragColor = texColor;
    }
}