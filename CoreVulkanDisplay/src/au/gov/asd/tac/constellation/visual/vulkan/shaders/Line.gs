// LINE geometry shader.
// Points that are marked with the hidden indicator (color.a<0) are not emitted.

#version 450


// === CONSTANTS ===
// Keep this in sync with SceneBatchStore.
const int LINE_INFO_ARROW = 1;
const int LINE_INFO_OVERFLOW = 2;

// Keep this in sync with LineArrow.gs.
const float EDGE_SEPARATION_SCALE = 32;
const float VISIBLE_ARROW_DISTANCE = 100;

const float FLOAT_MULTIPLIER = 1024;


// === UNIFORMS ===
layout(std140, binding = 2) uniform UniformBlock {
    mat4 pMatrix;
    float visibilityLow;
    float visibilityHigh;
    float directionMotion;
    vec4 highlightColor;
    float alpha;
    int drawHitTest;
} ub;


// === PER PRIMITIVE DATA IN ===
layout(lines) in;
layout(location = 0) in vec4 vpointColor[];
layout(location = 1) flat in ivec4 gData[];


// === PER PRIMITIVE DATA OUT ===
layout(triangle_strip, max_vertices=22) out;
layout(location = 0) out vec4 pointColor;
layout(location = 1) flat out ivec4 fData;
layout(location = 2) out vec2 pointCoord;
layout(location = 3) flat out float lineLength;


void main() {
    // Lines are explicitly not drawn if they have visibility <= 0.
    // See au.gov.asd.tac.constellation.visual.opengl.task;
    float visibility = vpointColor[0][3];
    if(visibility > max(ub.visibilityLow, 0) && (visibility <= ub.visibilityHigh || visibility > 1.0)) {
        // The ends of the line.
        vec4 end0 = gl_in[0].gl_Position;
        vec4 end1 = gl_in[1].gl_Position;

        // If the line is selected then move it slightly forward so
        // that it is drawn in front of unselected lines.
        int seldim = gData[0][2];
        bool isSelected = (seldim & 1) != 0;
        bool isDim = (seldim & 2) != 0;
        if(isSelected) {
            end0.z += 0.001;
            end1.z += 0.001;
        }

        float width = (gData[1].q >> 2) / FLOAT_MULTIPLIER;
        float lineDistance = max(0, min(-end0.z, -end1.z));

        // Always draw full lines and arrows if the width is greater than normal.
        // (There's probably some optimisation at a further distance, but this will do for now.)
        if(lineDistance <= VISIBLE_ARROW_DISTANCE || width > 1) {
            float lineStyle = gData[1].q & 0x3;

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
            end0.xy += 2 * offset*dir.xy / 32;
            end1.xy += 2 * offset*dir.xy / 32;

//            end0.z += offset * 0.0001;
//            end1.z += offset * 0.0001;

            vec4 halfWidth = vec4(dir.xy / 32, 0, 0);
            int lineInfo0 = gData[0].t;
            int lineInfo1 = gData[1].t;

            bool outgoing0 = (lineInfo0&LINE_INFO_ARROW) != 0;
            bool outgoing1 = (lineInfo1&LINE_INFO_ARROW) != 0;


            // Adjust the width as specified by the graph attribute.
            halfWidth *= width;
            arrowVector *= width * 0.75;

            // If this line is selected and we're not drawing into the hit test buffer,
            // or we've been told to draw it fatter (presumably to indicate many transactions in this edge),
            // draw it fatter.
            if((isSelected && ub.drawHitTest == 0) || (lineInfo0 & LINE_INFO_OVERFLOW) != 0 || (lineInfo1 & LINE_INFO_OVERFLOW) != 0) {
                halfWidth *= 2;
                arrowVector *= 1.5;
            }

            // If we're drawing into the hit buffer, the ub.alpha is left alone, because hit testing depends on the edge id
            // stored in the ub.alpha of the color.
            // If we're drawing into the display buffer, vary the ub.alpha depending on the distance of the line from the camera.
            // We use the nearest end of the line for the distance.
            vec4 color0;
            vec4 color1;

            if(ub.drawHitTest==0) {
                color0 = vpointColor[0];
                color1 = vpointColor[1];

                if(isSelected){
                    color0 = ub.highlightColor;
                    color1 = ub.highlightColor;
                } else if (isDim) {
                    color0 = vec4(0.25, 0.25, 0.25, 0.5);
                    color1 = color0;
                }

                // This overwrites the visibility value in vpointcolor[][3].
                // TODO: The fade-out distance should depend on the current bounding box / camera distance.
                float alphaFade = 1 - smoothstep(10, 500000, lineDistance);
                color0.a = alphaFade * ub.alpha;
                color1.a = alphaFade * ub.alpha;
            } else {
                color0 = vec4(0, 0, 0, 0);
                color1 = color0;
            }

            // Arrowheads and shafts need a different linestyle.
            // The line shaft linestyle (dotted, dashed, etc) specifies that a part of the line will be discarded in the fragment shader.
            // However, the arrowheads must always be drawn, so their line style must always be solid.
            // We'll pass the lineStyle in fData.q (rather than a variable) so it can be primitive specific.
            //
            // If end1 has an outgoing arrow and end0 is not
            // too far away then draw an arrow at end0.
            if(outgoing1 && (end0.z>-100 || width>1)) {
                // Draw a triangle to represent the arrow
                pointColor = color0;
                gl_Position = ub.pMatrix * end0;
                gl_Position.y = -gl_Position.y;
                fData = ivec4(gData[0].stp, 0);
                pointCoord = vec2(0, 1);
                EmitVertex();

                pointColor = color0;
                gl_Position = ub.pMatrix * (end0 - halfWidth * 4 + arrowVector);
                gl_Position.y = -gl_Position.y;
                fData = ivec4(gData[0].stp, 0);
                pointCoord = vec2(-0.7, 0.5);
                EmitVertex();

                pointColor = color0;
                gl_Position = ub.pMatrix * (end0 + halfWidth * 4 + arrowVector);
                gl_Position.y = -gl_Position.y;
                fData = ivec4(gData[0].stp, 0);
                pointCoord = vec2(0.7, 0.5);
                EmitVertex();

                end0 += arrowVector;

                // If both ends have arrows then convert the triangle into a diamond.
                if (outgoing0) {
                    pointColor = color0;
                    gl_Position = ub.pMatrix * (end0 + arrowVector * 0.5);
                    gl_Position.y = -gl_Position.y;
                    fData = ivec4(gData[0].stp, 0);
                    pointCoord = vec2(0, 0);
                    EmitVertex();

                    end0 += arrowVector * 0.4;
                }

                EndPrimitive();
            }

            // If end0 has an outgoing arrow and end1 is not
            // too far away then draw an arrow at end1.
            if(outgoing0 && (end1.z > -100 || width > 1)) {
                // Draw a triangle to represent the arrow
                pointColor = color1;
                gl_Position = ub.pMatrix * end1;
                gl_Position.y = -gl_Position.y;
                fData = ivec4(gData[0].stp, 0);
                pointCoord = vec2(0, 1);
                EmitVertex();

                pointColor = color1;
                gl_Position = ub.pMatrix * (end1 - halfWidth * 4 - arrowVector);
                gl_Position.y = -gl_Position.y;
                fData = ivec4(gData[0].stp, 0);
                pointCoord = vec2(-0.7, 0.5);
                EmitVertex();

                pointColor = color1;
                gl_Position = ub.pMatrix * (end1 + halfWidth * 4 - arrowVector);
                gl_Position.y = -gl_Position.y;
                fData = ivec4(gData[0].stp, 0);
                pointCoord = vec2(0.7, 0.5);
                EmitVertex();

                end1 -= arrowVector;

                // If both ends have arrows then convert the triangle into a diamond.
                if (outgoing1) {
                    pointColor = color1;
                    gl_Position = ub.pMatrix * (end1 - arrowVector * 0.5);
                    gl_Position.y = -gl_Position.y;
                    fData = ivec4(gData[0].stp, 0);
                    pointCoord = vec2(0, 0);
                    EmitVertex();

                    end1 -= arrowVector * 0.4;
                }

                EndPrimitive();
            }

            // Recalculate the line length because we may have moved the end points
            lineLength = length(end1 - end0);

            // Draw the shaft of the edge with the required line style.
            pointColor = color0;
            gl_Position = ub.pMatrix * (end0 + halfWidth);
            gl_Position.y = -gl_Position.y;
            fData = ivec4(gData[0].stp, lineStyle);
            pointCoord = vec2(0.5, 0);
            EmitVertex();

            pointColor = color0;
            gl_Position = ub.pMatrix * (end0 - halfWidth);
            gl_Position.y = -gl_Position.y;
            fData = ivec4(gData[0].stp, lineStyle);
            pointCoord = vec2(-0.5, 0);
            EmitVertex();

            pointColor = color1;
            gl_Position = ub.pMatrix * (end1 + halfWidth);
            gl_Position.y = -gl_Position.y;
            fData = ivec4(gData[0].stp, lineStyle);
            pointCoord = vec2(0.5, lineLength);
            EmitVertex();

            pointColor = color1;
            gl_Position = ub.pMatrix * (end1 - halfWidth);
            gl_Position.y = -gl_Position.y;
            fData = ivec4(gData[0].stp, lineStyle);
            pointCoord = vec2(-0.5, lineLength);
            EmitVertex();

            EndPrimitive();

            // Draw the motion bars
            if(ub.drawHitTest == 0 && ub.directionMotion != -1 && lineLength > 1 && outgoing0 != outgoing1) {
                end0.z += 0.001;
                end1.z += 0.001;

                float motionLen = lineLength / 32.0;
                float motionPos = ub.directionMotion - (floor(ub.directionMotion / lineLength) * lineLength);

                if(outgoing1) {
                    motionPos = lineLength - motionPos;
                    motionLen = -motionLen;
                }

                float motionStart = clamp(motionPos - motionLen, 0, lineLength);
                float motionEnd = clamp(motionPos + motionLen, 0, lineLength);

                vec4 color;
                if (ub.drawHitTest == 0) {
                    color = mix(vpointColor[0], vpointColor[1], motionPos / (lineLength - motionLen));
                    if (!isDim && !isSelected) {
                        float luminosity = 0.21 * color.r + 0.71 * color.g + 0.08 * color.b;
                        float adjust = luminosity > 0.5 ? -0.3 : 0.3;
                        color = clamp(vec4((adjust + color.r), (adjust + color.g), (adjust + color.b), color.a), 0, 1);
                    }
                    color.a = 1;
                } else {
                    color = vec4(0, 0, 0, 0);
                }

                pointColor = color;
                gl_Position = ub.pMatrix * (end0 + halfWidth + motionStart * lineDirection);
                gl_Position.y = -gl_Position.y;
                fData = ivec4(gData[0].stp, lineStyle);
                pointCoord = vec2(0.5, motionStart);
                EmitVertex();

                pointColor = color;
                gl_Position = ub.pMatrix * (end0 - halfWidth + motionStart * lineDirection);
                gl_Position.y = -gl_Position.y;
                fData = ivec4(gData[0].stp, lineStyle);
                pointCoord = vec2(-0.5, motionStart);
                EmitVertex();

                pointColor = color;
                gl_Position = ub.pMatrix * (end0 + halfWidth + motionEnd * lineDirection);
                gl_Position.y = -gl_Position.y;
                fData = ivec4(gData[0].stp, lineStyle);
                pointCoord = vec2(0.5, motionEnd);
                EmitVertex();

                pointColor = color;
                gl_Position = ub.pMatrix * (end0 - halfWidth + motionEnd * lineDirection);
                gl_Position.y = -gl_Position.y;
                fData = ivec4(gData[0].stp, lineStyle);
                pointCoord = vec2(-0.5, motionEnd);
                EmitVertex();

                EndPrimitive();
            }
        }
    }
}