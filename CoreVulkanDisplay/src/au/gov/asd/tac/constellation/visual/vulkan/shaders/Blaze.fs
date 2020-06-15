#version 330 core

const float ICON_BORDER = 0.125;

flat in vec4 fColor;
//flat in ivec4 fData;
in vec2 pointCoord;
in float fnradius;
flat in int isPointer;

uniform float opacity;

uniform sampler2DArray images;

out vec4 fragColor;

void main() {

    // Blazes don't do icons for now.
    // Comment out the rest of the code to avoid "fData not used" errors when linking on some drivers.
    if(isPointer != 0) {
        fragColor = vec4(fColor.rgb * (1.0 - pointCoord.x * pointCoord.x), opacity);
    }
/*
    else {
        vec4 icon = texture(images, vec3(pointCoord, fData[1]));
        fragColor.rgb = mix(fColor.rgb, icon.rgb, icon.a);
        fragColor.a = 1;
    }
*/
}