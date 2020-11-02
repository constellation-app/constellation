#version 450


// === CONSTANTS ===
const int LINE_STYLE_SOLID = 0;
const int LINE_STYLE_DOTTED = 1;
const int LINE_STYLE_DASHED = 2;
const int LINE_STYLE_DIAMOND = 3;
const float LINE_DOT_SIZE = 0.3;


// === PER FRAGMENT DATA IN ===
layout(location = 0) in vec4 pointColor;
layout(location = 1) flat in int hitTestId;
layout(location = 2) flat in int lineStyle;
layout(location = 3) in float pointCoord;
layout(location = 4) flat in float lineLength;


// === PER FRAGMENT DATA OUT ===
layout(location = 0) out vec4 fragColor;


void main(void) {
    // Line style.
    if (lineStyle != LINE_STYLE_SOLID && lineLength > 0) {
        float segmentSize = LINE_DOT_SIZE * (lineLength / (0.25 + lineLength));

        if (lineStyle == LINE_STYLE_DOTTED || lineStyle == LINE_STYLE_DIAMOND) {
            float seg = mod(pointCoord, 2 * segmentSize);
            if(seg>(1 * segmentSize) && seg < (2 * segmentSize)) {
                discard;
            }
        } else if (lineStyle == LINE_STYLE_DASHED) {
            float seg = mod(pointCoord, 3 * segmentSize);
            if (seg > (2 * segmentSize) && seg < (3 * segmentSize)) {
                discard;
            }
        }
    }

    fragColor = hitTestId == 0 ? pointColor : vec4(hitTestId, 0, 0, 1);
}
