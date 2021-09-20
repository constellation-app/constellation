#version 330 core

const int LOOP_DIRECTED_INDEX = 2;
const int LOOP_UNDIRECTED_INDEX = 3;

uniform sampler2DArray images;

// If non-zero, use the texture to color the icon.
// Otherwise, use a unique color for hit testing.
uniform int drawHitTest;

// Anaglyphic drawing.
//
uniform int greyscale;

in vec4 pointColor;
flat in ivec4 fData;
in vec2 pointCoord;

out vec4 fragColor;

void main() {
    int imgIx = fData.q;

    float iconOffsetX = float(imgIx % 8) / 8;
    float iconOffsetY = float((imgIx / 8) % 8) / 8;

    vec4 pixel = texture(images, vec3(pointCoord.x + iconOffsetX, pointCoord.y + iconOffsetY, imgIx / 64));
    fragColor = pixel;
    if(fragColor.a == 0) {
        discard;
    }

    if(drawHitTest == 0) {
        int seldim = fData.p;
        bool isSelected = (seldim & 1) != 0;
        bool isDim = (seldim & 2) != 0;

        if(isSelected) {
            fragColor = vec4(1, 0.1, 0.1, 1);
        } else if(isDim) {
            fragColor = vec4(0.25, 0.25, 0.25, 1);
        } else {
            fragColor.rgb *= pointColor.rgb;
        }

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
    } else {
        fragColor = vec4(-(fData.s + 1), 0.0, 0.0, 1.0);
    }
}
