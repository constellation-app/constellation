// LINE geometry shader.
// Points that are marked with the hidden indicator (color.a<0) are not emitted.
//
// The edge number is used to move the edge
// so multiple edges between two nodes are visible. An obvious way to do this is to slide all of
// the endpoints depending on the total number of edges. however, this required knowing the total
// number of edges, and we're trying not to pass extra data where possible. Instead, even numbered
// edges are moved one way and odd numbered edges are moved the other way.
//
// If an edge is drawn far away using triangle_strips, it doesn't look right: the triangles are drawn with artifacts that
// leave gaps in the lines.
// Therefore for distant edges we'll draw actual lines.

#version 450


// === CONSTANTS ===
// Keep this in sync with SceneBatchStore.
const int LINE_INFO_ARROW = 1;
const int LINE_INFO_OVERFLOW = 2;
const float EDGE_SEPARATION_SCALE = 32;
const float VISIBLE_ARROW_DISTANCE = 100;
const float FLOAT_MULTIPLIER = 1024;


// === PUSH CONSTANTS ===
layout(std140, push_constant) uniform HitTestPushConstant {
    // If non-zero, use the texture to color the icon.
    // Otherwise, use a unique color for hit testing.
    // Offset is 64 as the projection matrix (in Line.vs) 
    // is before it in the pushConstant buffer.
    layout(offset = 64) int drawHitTest;
} htpc;


// === UNIFORMS ===
layout(std140, binding = 2) uniform UniformBlock {
    mat4 pMatrix;
    vec4 highlightColor;
    float visibilityLow;
    float visibilityHigh;
    float directionMotion;    
    float alpha;
} ub;


// === PER VERTEX DATA IN ===
layout(lines) in;
layout(location = 0) in vec4 vpointColor[];
layout(location = 1) flat in ivec4 gData[];


// === PER VERTEX DATA OUT ===
layout(line_strip, max_vertices=4) out;
layout(location = 0) out vec4 pointColor;
layout(location = 1) flat out int hitTestId;
layout(location = 2) flat out int lineStyle;
layout(location = 3) out float pointCoord;
layout(location = 4) flat out float lineLength;


void main() {
    // Lines are explicitly not drawn if they have visibility <= 0.
    // See au.gov.asd.tac.constellation.visual.opengl.task;
    float visibility = vpointColor[0][3];
    if (visibility > max(ub.visibilityLow, 0) && (visibility <= ub.visibilityHigh || visibility > 1.0)) {
        // The ends of the line.
        vec4 end0 = gl_in[0].gl_Position;
        vec4 end1 = gl_in[1].gl_Position;

        // If the line is selected then move it slightly forward so
        // that it is drawn in front of unselected lines.
        int seldim = gData[0][2];
        bool isSelected = (seldim & 1) != 0;
        bool isDim = (seldim & 2) != 0;
        if (isSelected) {
            end0.z += 0.001;
            end1.z += 0.001;
        }

        float width = (gData[1].q >> 2) / FLOAT_MULTIPLIER;
        float lineDistance = max(0, min(-end0.z, -end1.z));

        // Only draw line lines if the width is not greater than normal.
        // (There's probably some optimisation at a further distance, but this will do for now.)
        if (lineDistance > VISIBLE_ARROW_DISTANCE && width <= 1) {
            lineStyle = gData[1].q & 0x3;

            // The lines currently end at the centre of the 2*2 point sprite.
            // We want to end them on the surface of a 1-radius sphere around the centre,
            // so we subtract 1 from each end of the line.
            // As the ends of the line approach each other, the ends should move towards the centres of the points.
            lineLength = distance(end0, end1);
            vec4 lineDirection = normalize(end1 - end0);
            float arrowLength = (clamp(lineLength, 0.0, 3.0)) * 0.29167;
            vec4 arrowVector = lineDirection * arrowLength;

            end1 -= arrowVector;
            end0 += arrowVector;

            vec3 dir = normalize(cross(vec3(end0.xy - end1.xy, 0), vec3(0, 0, 1)));
            float offset = gData[0].q / FLOAT_MULTIPLIER;
            end0.xy += 2 * offset * dir.xy / 32;
            end1.xy += 2 * offset * dir.xy / 32;

            // If we're drawing into the hit buffer, the alpha is left alone, because hit testing depends on the edge id
            // stored in the alpha of the color.
            // If we're drawing into the display buffer, vary the alpha depending on the distance of the line from the camera.
            // We use the nearest end of the line for the distance.
            vec4 color0;
            vec4 color1;

            // First, we tried passing the txId in the red element of the color0 and color1 vec4s.
            // We crossed our fingers that color interpolation wouldn't screw up the value.
            // However, this doesn't seem to be the case: sometimes an incorrect value ends up in
            // the hit test framebuffer.
            // Therefore, we'll pass the txId in a separate int that has no chance of being interpolated.
            int hti;

            if (htpc.drawHitTest == 0) {
                color0 = vpointColor[0];
                color1 = vpointColor[1];

                // If this line is selected, and we're not drawing into the hit test buffer,
                // draw it fatter.
                if (isSelected) {
                    color0 = ub.highlightColor;
                    color1 = ub.highlightColor;
                } else if (isDim) {
                    color0 = vec4(0.25, 0.25, 0.25, 0.5);
                    color1 = color0;
                }

                // This overwrites the visibility value in vpointcolor[][3].
                // TODO: The fade-out distance should depend on the current bounding box / camera distance.
                float lineAlpha = 1 - smoothstep(10, 500000, lineDistance);
                color0.a = lineAlpha * ub.alpha;
                color1.a = lineAlpha * ub.alpha;

                hti = 0;
            } else {
                // Color vec4s will be unused, but let's not leave things uninitialised.
                color0 = vec4(0);
                color1 = color0;
                hti = -(gData[0].s + 1);
            }

            lineLength = length(end1 - end0);

            pointColor = color0;
            hitTestId = hti;
            gl_Position = ub.pMatrix * end0;
            gl_Position.y = -gl_Position.y;
            pointCoord = 0;
            EmitVertex();

            pointColor = color1;
            hitTestId = hti;
            gl_Position = ub.pMatrix * end1;
            gl_Position.y = -gl_Position.y;
            pointCoord = lineLength;
            EmitVertex();

            EndPrimitive();

            // Draw the motion bars.
            bool arrow0 = (gData[0].t & LINE_INFO_ARROW) != 0;
            bool arrow1 = (gData[1].t & LINE_INFO_ARROW) != 0;
            if (htpc.drawHitTest == 0 && ub.directionMotion != -1 && lineLength > 1 && arrow0 != arrow1) {
                end0.z += 0.001;
                end1.z += 0.001;

                lineLength = length(end1 - end0);
                float motionLen = lineLength / 32.0;
                float motionPos = ub.directionMotion - (floor(ub.directionMotion / lineLength) * lineLength);

                if (arrow1) {
                    motionPos = lineLength - motionPos;
                    motionLen = -motionLen;
                }

                float motionStart = clamp(motionPos - motionLen, 0, lineLength);
                float motionEnd = clamp(motionPos + motionLen, 0, lineLength);

                vec4 color = mix(vpointColor[0], vpointColor[1], motionPos / (lineLength - motionLen));
                if (!isDim && !isSelected) {
                    float luminosity = 0.21 * color.r + 0.71 * color.g + 0.08 * color.b;
                    float adjust = luminosity > 0.5 ? -0.3 : 0.3;
                    color = clamp(vec4((adjust + color.r), (adjust + color.g), (adjust + color.b), color.a), 0, 1);
                }
                color.a = 1;

                pointColor = color;
                gl_Position = ub.pMatrix * (end0 + motionStart * lineDirection);
                gl_Position.y = -gl_Position.y;
                pointCoord = 0;
                EmitVertex();

                pointColor = color;
                gl_Position = ub.pMatrix * (end0 + motionEnd * lineDirection);
                gl_Position.y = -gl_Position.y;
                pointCoord = 0;
                EmitVertex();

                EndPrimitive();
            }
        }
    }
}
