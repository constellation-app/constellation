#version 450


// === UNIFORMS ===
layout(binding = 1) uniform sampler2DArray images;


// === PER FRAGMENT DATA IN ===
layout(location = 0) flat in float tex;
layout(location = 1) in vec2 pointCoord;
layout(location = 2) flat in vec2 width_height_frac;


// === PER FRAGMENT DATA OUT ===
out vec4 fragColor;


void main(void) {
    float x = pointCoord.x;
    float y = pointCoord.y;
    vec4 pixel = texture(images, vec3(x * width_height_frac[0], y * width_height_frac[1], tex));
    float alpha = min(pixel.a, 0.75);
    if (alpha > 0) {
        fragColor = vec4(pixel.rgb, alpha);
    } else {
        discard;
    }
}