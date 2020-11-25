#version 450

// === PER TEXEL DATA OUT
layout(location = 0) out vec4 fragColor;

void main(void) {
    fragColor.x = clamp(gl_FragCoord.x / 1000.0, 0.2, 1.0);
    fragColor.y = clamp(gl_FragCoord.y / 1000.0, 0.2, 1.0);
    fragColor.z = clamp((fragColor.x + fragColor.y) / 2500.0, 0.2, 1.0);
    fragColor.w = 1.0;
}
