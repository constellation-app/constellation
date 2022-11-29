#version 330 core

// We want thin black lines on the edges of the edges so we can see individual lines
// instead of a mass of color.
const float EDGE1 = 1.0/16;
const float EDGE2 = 15.0/16;

const int LINE_STYLE_SOLID = 0;
const int LINE_STYLE_DOTTED = 1;
const int LINE_STYLE_DASHED = 2;
const int LINE_STYLE_DIAMOND = 3;
const float LINE_DOT_SIZE = 0.3;

uniform float alpha;

uniform int drawHitTest;

// Anaglyphic drawing.
//
uniform int greyscale;

in vec4 pointColor;
flat in ivec4 fData;
in vec2 pointCoord;

flat in float lineLength;

out vec4 fragColor;

void main(void) {
    // Line style.
    if(fData.q != LINE_STYLE_SOLID && lineLength > 0) {
        float lineStyle = fData.q;
        float segmentSize = LINE_DOT_SIZE * (lineLength / (0.25 + lineLength));

        if(lineStyle == LINE_STYLE_DOTTED) {
            float seg = mod(pointCoord.y, 2 * segmentSize);
            if(seg > (1 * segmentSize) && seg < (2 * segmentSize)) {
                discard;
            }
        } else if(lineStyle == LINE_STYLE_DASHED) {
            float seg = mod(pointCoord.y, 3 * segmentSize);
            if(seg > (2 * segmentSize) && seg<(3 * segmentSize)) {
                discard;
            }
        } else if(lineStyle == LINE_STYLE_DIAMOND) {
            float seg = mod(pointCoord.y, segmentSize) / segmentSize;
            if((pointCoord.x < 0 && seg > pointCoord.x + 1) || (pointCoord.x > 0 && seg > (1 - pointCoord.x))) {
                discard;
            } else if((pointCoord.x < 0 && seg < (1 - pointCoord.x) - 1) || (pointCoord.x > 0 && seg < pointCoord.x)) {
                discard;
            }
        }
    }

    if(drawHitTest==0) {
        // Make the edges of the line darker so the viewer can distinguish between lines.
        if (abs(pointCoord.x) > 0.20) {
            fragColor = (1.2 - abs(pointCoord.x)) * pointColor;
        } else {
            fragColor = pointColor;
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
        // The line index is in pointColor[3].
        // Use it to make a unique color less than zero for hit testing.
        fragColor = vec4(-(fData[0] + 1), 0.0, 0.0, 1.0);
    }
}
