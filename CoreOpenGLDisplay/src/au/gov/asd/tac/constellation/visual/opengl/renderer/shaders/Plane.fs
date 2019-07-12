#version 330 core

uniform sampler2DArray images;

flat in float tex;
in vec2 pointCoord;
flat in vec2 width_height_frac;

out vec4 fragColor;

void main(void) {
    float x = pointCoord.x;
    float y = pointCoord.y;
    vec4 pixel = texture(images, vec3(x * width_height_frac[0], y * width_height_frac[1], tex));
    float alpha = min(pixel.a, 0.75);
    if(alpha > 0) {
        fragColor = vec4(pixel.rgb, alpha);
    } else {
        discard;
    }
}