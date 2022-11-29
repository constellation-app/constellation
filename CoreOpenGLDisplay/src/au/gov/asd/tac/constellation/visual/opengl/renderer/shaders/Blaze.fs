#version 330 core

const float ICON_BORDER = 0.125;

// Anaglyphic drawing.
//
uniform int greyscale;

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

        if (greyscale==1) {
            // Anaglyphic drawing.
            // Choose whichever "convert to greyscale" algorithm works best.
            //

            // Luminosity
            //
            fragColor.rgb = vec3(0.21*fragColor.r + 0.71*fragColor.g + 0.08*fragColor.b);

            // Average
            //
            //fragColor.rgb = vec3((fragColor.r + fragColor.g + fragColor.b)/3);

            // Lightness
            //
            //fragColor.rgb = vec3((max(fragColor.r, max(fragColor.g, fragColor.b))+min(fragColor.r, min(fragColor.g, fragColor.b)))/2);
        }
    }
/*
    else {
        vec4 icon = texture(images, vec3(pointCoord, fData[1]));
        fragColor.rgb = mix(fColor.rgb, icon.rgb, icon.a);
        fragColor.a = 1;
    }
*/
}