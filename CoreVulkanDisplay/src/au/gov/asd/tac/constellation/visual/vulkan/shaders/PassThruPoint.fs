#version 450

// === PER TEXEL DATA OUT
layout(location = 0) out vec4 fragColor;

void main(void) {
    fragColor.x = clamp(gl_FragCoord.x + 0.25, 0.0, 1.0);
    fragColor.y = clamp(gl_FragCoord.y + 0.25, 0.0, 1.0);
    fragColor.z = clamp((gl_FragCoord.x + gl_FragCoord.y) * 0.75, 0.0, 1.0);
    fragColor.w = 1.0;
}