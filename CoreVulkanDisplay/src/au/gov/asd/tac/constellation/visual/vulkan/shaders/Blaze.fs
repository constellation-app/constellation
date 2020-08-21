#version 450


// === CONSTANTS ===
const float ICON_BORDER = 0.125;


// === UNIFORMS ===
layout(binding = 2) uniform sampler2DArray images;
layout(std140, binding = 3) uniform UniformBlock {
    float opacity;
} ub;


// === PER FRAGMENT DATA IN ===
layout(location = 0) flat in vec4 fColor;
layout(location = 1) in vec2 pointCoord;
layout(location = 2) in float fnradius;
layout(location = 3) flat in int isPointer;


// === PER FRAGMENT DATA OUT ===
out vec4 fragColor;


void main() {

    // Blazes don't do icons for now.
    // Comment out the rest of the code to avoid "fData not used" errors when linking on some drivers.
    if(isPointer != 0) {
        fragColor = vec4(fColor.rgb * (1.0 - pointCoord.x * pointCoord.x), ub.opacity);
    }
/*
    else {
        vec4 icon = texture(images, vec3(pointCoord, fData[1]));
        fragColor.rgb = mix(fColor.rgb, icon.rgb, icon.a);
        fragColor.a = 1;
    }
*/
}