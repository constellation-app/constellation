#version 450

// === PER TEXEL DATA IN
layout(location = 0) in vec4 fColor;

// === PER TEXEL DATA OUT
layout(location = 0) out vec4 fragColor;

void main(void) {
    fragColor = fColor;
}