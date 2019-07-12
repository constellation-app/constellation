// Icon geometry shader.
// Icons that are marked with the hidden indicator (color.a<0) are not emitted.
//
// The [gf]DisplayColor passes the highlight color through to the fragment shader.
//

#version 330 core

const int ICON_BITS = 16;
const int ICON_MASK = 0xffff;

const int SELECTED_MASK = 1;
const int DIM_MASK = 2;

const float TEXTURE_SIZE = 0.125;
const float HALF_PIXEL = (0.5 / (256 * 8));

const int HIGHLIGHT_ICON = 0;
const int TRANSPARENT_ICON = 5;

const mat4 DIM_MATRIX = 0.5 * mat4(
    0.21, 0.21, 0.21, 0.00,
    0.71, 0.71, 0.71, 0.00,
    0.08, 0.08, 0.08, 0.00,
    0.00, 0.00, 0.00, 1.00
);

const mat4 IDENTITY_MATRIX = mat4(
    1, 0, 0, 0,
    0, 1, 0, 0,
    0, 0, 1, 0,
    0, 0, 0, 1
);

layout(points) in;
layout(triangle_strip, max_vertices=28) out;

uniform mat4 pMatrix;
uniform isamplerBuffer flags;
uniform float pixelDensity;
uniform mat4 highlightColor;
uniform int drawHitTest;

flat in ivec4 gData[];
in mat4 gBackgroundIconColor[];
flat in int vxPosition[];

// The radius of the vertex
flat in float gRadius[];

flat out mat4 iconColor;
noperspective centroid out vec3 textureCoords;
flat out vec4 hitBufferValue;

vec4 v;
vec4 hbv;

void drawIcon(float x, float y, float radius, int icon, mat4 color) {

    if (icon != TRANSPARENT_ICON) {

        vec3 iconOffset = vec3(float(icon & 7) / 8, float((icon >> 3) & 7) / 8, float(icon >> 6));

        gl_Position = pMatrix * vec4(v.x + x, v.y + y, v.z, v.w);
        iconColor = color;
        hitBufferValue = hbv;
        textureCoords = vec3(HALF_PIXEL, TEXTURE_SIZE - HALF_PIXEL, 0) + iconOffset;
        EmitVertex();

        gl_Position = pMatrix * vec4(v.x + x, v.y + y + radius, v.z, v.w);
        iconColor = color;
        hitBufferValue = hbv;
        textureCoords = vec3(HALF_PIXEL, HALF_PIXEL, 0) + iconOffset;
        EmitVertex();

        gl_Position = pMatrix * vec4(v.x + x + radius, v.y + y, v.z, v.w);
        iconColor = color;
        hitBufferValue = hbv;
        textureCoords = vec3(TEXTURE_SIZE - HALF_PIXEL, TEXTURE_SIZE - HALF_PIXEL, 0) + iconOffset;
        EmitVertex();

        gl_Position = pMatrix * vec4(v.x + x + radius, v.y + y + radius, v.z, v.w);
        iconColor = color;
        hitBufferValue = hbv;
        textureCoords = vec3(TEXTURE_SIZE - HALF_PIXEL, HALF_PIXEL, 0) + iconOffset;
        EmitVertex();

        EndPrimitive();
    }
}

void main() {

    // Nodes are explicitly not drawn if they have visibility <= 0.
    // See au.gov.asd.tac.constellation.visual.opengl.task.NodeHider.java.
    //
    float sideRadius = gRadius[0];
    if (sideRadius > 0) {

        // Get the position of the vertex
        v = gl_in[0].gl_Position;

        // Calculate the hit buffer value
        hbv = vec4(gData[0][3] + 1, 0, 0, 1);

        // Set a minimum size for the vertex
//        if (v.z < -10000 * sideRadius) {
//            sideRadius *= v.z / -10000;
//        }
        // Get the flags associated with this vertex
        // selected, dimmed etc.
        int fd = texelFetch(flags, vxPosition[0]).s;

        // If the vertex is selected then move it slightly forward so
        // that it is drawn in front of unselected vertices.
        if((fd & SELECTED_MASK) != 0) {
            // Set a minimum size for selected vertices.
            //
            float ddist = 800;
            if(v.z < -ddist) {
                sideRadius *= 1 - ((ddist+v.z)/ddist);
            }

            v.z += 0.001;
        }

        int bgIcon = (gData[0][0] >> ICON_BITS) & ICON_MASK;

        float iconPixelRadius = sideRadius * pixelDensity / -v.z;
        if (iconPixelRadius < 1 && bgIcon != TRANSPARENT_ICON) {
            mat4 backgroundIconColor = gBackgroundIconColor[0];
            backgroundIconColor[3][3] = max(smoothstep(0, 1, iconPixelRadius), 0.7);
            if ((fd & DIM_MASK) != 0) {
                backgroundIconColor = DIM_MATRIX * backgroundIconColor;
            }
            drawIcon(-sideRadius, -sideRadius, 2 * sideRadius, bgIcon, backgroundIconColor);

        } else {

            // Draw the background icon
            mat4 iconColor = gBackgroundIconColor[0];
            if ((fd & DIM_MASK) != 0) {
                iconColor = DIM_MATRIX * iconColor;
            }
            drawIcon(-sideRadius, -sideRadius, 2 * sideRadius, bgIcon, iconColor);

            // Draw the foreground icon
            int fgIcon = gData[0][0] & ICON_MASK;
            if ((fd & DIM_MASK) != 0) {
                iconColor = DIM_MATRIX;
            } else {
                iconColor = IDENTITY_MATRIX;
            }
            if (bgIcon != TRANSPARENT_ICON) {
                iconColor[3][3] = smoothstep(1, 5, iconPixelRadius);
                drawIcon(-sideRadius, -sideRadius, 2 * sideRadius, fgIcon, iconColor);
            } else {
                drawIcon(-sideRadius, -sideRadius, 2 * sideRadius, fgIcon, iconColor);
                iconColor[3][3] = smoothstep(1, 5, iconPixelRadius);
            }

            // Draw the decorators
            float decoratorRadius = sideRadius * 2/3;
            float decoratorOffset = sideRadius / 3;
            drawIcon(-sideRadius, decoratorOffset, decoratorRadius, gData[0][1] & ICON_MASK, iconColor);
            drawIcon(-sideRadius, -sideRadius, decoratorRadius, (gData[0][1] >> ICON_BITS) & ICON_MASK, iconColor);
            drawIcon(decoratorOffset, -sideRadius, decoratorRadius, gData[0][2] & ICON_MASK, iconColor);
            drawIcon(decoratorOffset, decoratorOffset, decoratorRadius, (gData[0][2] >> ICON_BITS) & ICON_MASK, iconColor);
        }

        // If the vertex is selected then draw the highlight icon
        if ((drawHitTest == 0) && (fd & SELECTED_MASK) != 0) {
            drawIcon(-sideRadius, -sideRadius, 2 * sideRadius, HIGHLIGHT_ICON, highlightColor);
        }
    }
}