// Icon geometry shader.
// Icons that are marked with the hidden indicator (color.a<0) are not emitted.
//
// The [gf]DisplayColor passes the highlight color through to the fragment shader.

#version 450


// === CONSTANTS ===
const int ICON_BITS = 16;
const int ICON_MASK = 0xffff;
const float TEXTURE_SIZE = 0.125;
const float HALF_PIXEL = (0.5 / (256 * 8));
const int TRANSPARENT_ICON = 5;
const mat4 IDENTITY_MATRIX = mat4(
    1, 0, 0, 0,
    0, 1, 0, 0,
    0, 0, 1, 0,
    0, 0, 0, 1
);


// === UNIFORMS ===
layout(std140, binding = 0) uniform UniformBlock {
    mat4 pMatrix;
    float pixelDensity;
    float pScale;
} ub;


// === PER PRIMITIVE DATA IN ===
layout(points) in;
layout(location = 0) flat in ivec2 gData[];
layout(location = 1) flat in mat4 gBackgroundIconColor[];
layout(location = 5) flat in float gRadius[];


// === PER PRIMITIVE DATA OUT ===
layout(triangle_strip, max_vertices=28) out;
layout(location = 0) flat out mat4 iconColor;
layout(location = 4) noperspective centroid out vec3 textureCoords;


// === FILE SCOPE VARS ===
vec4 v;


/*
Compared to the OpenGL version of this shader the Y is flipped.

OpenGL
^ Y
|
|
.----> X

Vulkan
.----> X
|
|
V Y
*/


void drawIcon(float x, float y, float radius, int icon, mat4 color) {
    if (icon != TRANSPARENT_ICON) {

        vec3 iconOffset = vec3(float(icon & 7) / 8, float((icon >> 3) & 7) / 8, float(icon >> 6));

        // Top left
        gl_Position = v + (ub.pScale * ub.pMatrix * vec4(x, y, 0, 0));
        iconColor = color;
        textureCoords = vec3(TEXTURE_SIZE - HALF_PIXEL, TEXTURE_SIZE - HALF_PIXEL, 0) + iconOffset;        
        EmitVertex();

        // Bottom left
        gl_Position = v + (ub.pScale * ub.pMatrix * vec4(x, y + radius, 0, 0));
        iconColor = color;
        textureCoords = vec3(TEXTURE_SIZE - HALF_PIXEL, HALF_PIXEL, 0) + iconOffset;        
        EmitVertex();

        // Top right
        gl_Position = v + (ub.pScale * ub.pMatrix * vec4(x + radius, y, 0, 0));
        iconColor = color;
        textureCoords = vec3(HALF_PIXEL, TEXTURE_SIZE - HALF_PIXEL, 0) + iconOffset;
        EmitVertex();

        // Bottom right
        gl_Position = v + (ub.pScale * ub.pMatrix * vec4(x + radius, y + radius, 0, 0));
        iconColor = color;
        textureCoords = vec3(HALF_PIXEL, HALF_PIXEL, 0) + iconOffset;      
        EmitVertex();

        EndPrimitive();
    }
}

void main() {
    float sideRadius = gRadius[0];
    if (sideRadius > 0) {

        // Get the position of the vertex
        v = gl_in[0].gl_Position;

        int bgIcon = (gData[0][0] >> ICON_BITS) & ICON_MASK;

        float iconPixelRadius = sideRadius * ub.pixelDensity / -v.z;
        if (iconPixelRadius < 1 && bgIcon != TRANSPARENT_ICON) {
            mat4 backgroundIconColor = gBackgroundIconColor[0];
            backgroundIconColor[3][3] = max(smoothstep(0.0, 1.0, iconPixelRadius), 0.7);
            drawIcon(-sideRadius, -sideRadius, 2 * sideRadius, bgIcon, backgroundIconColor);
        } else {

            // Draw the background icon
            mat4 iconColor = gBackgroundIconColor[0];
            drawIcon(-sideRadius, -sideRadius, 2 * sideRadius, bgIcon, iconColor);

            // Draw the foreground icon
            int fgIcon = gData[0][0] & ICON_MASK;
            if (bgIcon != TRANSPARENT_ICON) {
                iconColor[3][3] = smoothstep(1.0, 5.0, iconPixelRadius);
                drawIcon(-sideRadius, -sideRadius, 2 * sideRadius, fgIcon, iconColor);
            } else {
                drawIcon(-sideRadius, -sideRadius, 2 * sideRadius, fgIcon, iconColor);
                iconColor[3][3] = smoothstep(1.0, 5.0, iconPixelRadius);
            }
        }
    }
}
