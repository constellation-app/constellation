#version 450


// === PER FRAGMENT DATA IN
layout(location = 0) in vec4 fColor;


// === PER FRAGMENT DATA OUT
layout(location = 0) out vec4 fragColor;


void main(void) {
    fragColor = fColor;
}