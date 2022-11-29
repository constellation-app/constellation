#version 330 core

const int LINE_STYLE_SOLID = 0;
const int LINE_STYLE_DOTTED = 1;
const int LINE_STYLE_DASHED = 2;
const int LINE_STYLE_DIAMOND = 3;
const float LINE_DOT_SIZE = 0.3;

// Anaglyphic drawing.
//
uniform int greyscale;

in vec4 pointColor;
flat in int hitTestId;
flat in int lineStyle;
in float pointCoord;

flat in float lineLength;

out vec4 fragColor;

void main(void) {
    // Line style.
    if(lineStyle != LINE_STYLE_SOLID && lineLength > 0) {
        float segmentSize = LINE_DOT_SIZE * (lineLength / (0.25 + lineLength));

        if(lineStyle == LINE_STYLE_DOTTED || lineStyle == LINE_STYLE_DIAMOND) {
            float seg = mod(pointCoord, 2 * segmentSize);
            if(seg>(1 * segmentSize) && seg < (2 * segmentSize)) {
                discard;
            }
        } else if(lineStyle == LINE_STYLE_DASHED) {
            float seg = mod(pointCoord, 3 * segmentSize);
            if(seg > (2 * segmentSize) && seg < (3 * segmentSize)) {
                discard;
            }
        }
    }

    if (hitTestId==0) {
        fragColor = pointColor;

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
        fragColor = vec4(hitTestId, 0, 0, 1);
    }
}
