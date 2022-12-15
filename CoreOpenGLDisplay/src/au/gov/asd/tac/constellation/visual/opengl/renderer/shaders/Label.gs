// Geometry shader for labels.
// Outputs the 4 corners of a glyph, and if we are drawing the background of a connection label, possibly the 3 corners
// of an indicator triangle pointing at the conenction.
#version 330 core

// If the point is beyond this distance, don't draw it.
// Keep this in sync with the text fadeout in the fragment shader.
const float VISIBLE_DISTANCE = 150;

// The size of the indicator triangle on connection labels.
const float INDICATOR_SIZE = 0.1;

// An approximation of the trigonometric tangent function evaluated at various degree values.
const float SIN_SIXTY_DEGREES = 0.86603;

layout(points) in;
layout(triangle_strip, max_vertices=7) out;

// Matrix to convert from camera coordinates to scene coordinates.
uniform mat4 pMatrix;

// [0..1] x,y coordinates of glyph in glyphImageTexture. The whole part of x is the texture number
// [2..3] width, height of glyph in glyphImageTexture
uniform samplerBuffer glyphInfoTexture;

// The scaling factor to convert from texture coordinates to world unit coordinates
uniform float widthScalingFactor;
uniform float heightScalingFactor;

// Used to draw the connection indicator on the label background.
uniform vec4 highlightColor;

// The scaling factor if the glyph we are rendering is in fact the background for a line of text.
// This will be one in all other cases.
in float backgroundScalingFactor[];
in int glyphIndex[];
in vec4 labelColor[];
in float glyphScale[];
in int drawIndicator[];
in float drawIndicatorX[];
in float drawIndicatorY[];
in float depth[];

// The coordinates to lookup the glyph in the glyphImageTexture
noperspective centroid out vec3 textureCoordinates;
// The color of this glyph (constant for a whole label, unless we are rendering its background or connection indicator).
out vec4 fLabelColor;

flat out float fDepth;


void main(void) {

    if(labelColor[0].a > 0) {

        // The near and far planes
        float near = gl_DepthRange.near;
        float far = gl_DepthRange.far;

        // Retrive the positional information about the glyph from the glypInfoTexture
        vec4 glyphInfo = texelFetch(glyphInfoTexture, glyphIndex[0]);
        int glyphPage = int(glyphInfo[0]);
        float glyphX = glyphInfo[0] - glyphPage;
        float glyphY = glyphInfo[1];
        float glyphWidth = glyphInfo[2];
        float glyphHeight = glyphInfo[3];

        // The upper-left of the glyph in camera coordinates, as calculated from the vertex shader
        vec4 glyphLocation = gl_in[0].gl_Position;

        // Calculate depth
        vec4 depthVec = pMatrix * vec4(glyphLocation.xy, depth[0], glyphLocation.w);
        float calcdDepth = ((far - near) * (depthVec.z / depthVec.w) + far + near) / 2.0;

        // The dimensions (in camera coordinates) of the glyph
        float width = glyphWidth * glyphScale[0] * widthScalingFactor * backgroundScalingFactor[0];
        float height = glyphHeight * glyphScale[0] * heightScalingFactor;

        // Emitt the four corners of the glyph in screen coordinates.
        // Upper Left
        fLabelColor = labelColor[0];
        textureCoordinates = vec3(glyphX, glyphY, glyphPage);
        fDepth = calcdDepth;
        gl_Position = pMatrix * glyphLocation;
        EmitVertex();

        // Lower Left
        fLabelColor = labelColor[0];
        textureCoordinates = vec3(glyphX, glyphY + glyphHeight, glyphPage);
        fDepth = calcdDepth;
        vec4 locationOffset = vec4(0, -height, 0, 0);
        gl_Position = pMatrix * (glyphLocation + locationOffset);
        EmitVertex();

        // Upper Right
        fLabelColor = labelColor[0];
        textureCoordinates = vec3(glyphX + glyphWidth, glyphY, glyphPage);
        fDepth = calcdDepth;
        locationOffset = vec4(width, 0, 0, 0);
        gl_Position = pMatrix * (glyphLocation + locationOffset);
        EmitVertex();

        // Lower Right
        fLabelColor = labelColor[0];
        textureCoordinates = vec3(glyphX + glyphWidth, glyphY + glyphHeight, glyphPage);
        fDepth = calcdDepth;
        locationOffset =  vec4(width, -height, 0, 0);
        gl_Position = pMatrix * (glyphLocation + locationOffset);
        EmitVertex();

        EndPrimitive();

        // Draw an indicator for a connection label if necessary.
        if (drawIndicator[0] != 0) {

            // recalculate depth for direction indicator noting that we need to push the depth slightly forward of the
            // background glyph, but not as far forward as the other glyphs.
            float forward = 0.00025;
            vec4 depthVec = pMatrix * vec4(glyphLocation.xy, depth[0] + forward, glyphLocation.w);
            float calcdDepth = ((far - near)*(depthVec.z / depthVec.w) + far + near) / 2.0;

            // The indicator color is the graph highlight color, with the alpha value that is constant across the label.
            vec4 indicatorColor = vec4(highlightColor.xyz, 1.5 * labelColor[0].a);
            vec4 trianglePoint, triangleBase1, triangleBase2;

            if (drawIndicator[0] == 1 || drawIndicator[0] == 2) {
                // The indicator is an isosecles triangle pointing to the connection intersecting the top or bottom of the label background.
                float triangleDrop = drawIndicator[0] == 1 ? -INDICATOR_SIZE*3 : INDICATOR_SIZE*3;
                trianglePoint = vec4(drawIndicatorX[0] - glyphLocation.x, drawIndicatorY[0], 0, 0);
                triangleBase1 = vec4(drawIndicatorX[0] - glyphLocation.x + INDICATOR_SIZE, drawIndicatorY[0] + triangleDrop, 0, 0);
                triangleBase2 = vec4(drawIndicatorX[0] - glyphLocation.x - INDICATOR_SIZE, drawIndicatorY[0] + triangleDrop, 0, 0);
            } else if (drawIndicator[0] == 3 || drawIndicator[0] == 4) {
                // The indicator is an isosecles triangle pointing to the connection intersecting the left or right of the label background.
                float triangleDrop = drawIndicator[0] == 3 ? INDICATOR_SIZE * 3 : -INDICATOR_SIZE * 3;
                trianglePoint = vec4(drawIndicatorX[0], drawIndicatorY[0]-glyphLocation.y, 0, 0);
                triangleBase1 = vec4(drawIndicatorX[0] + triangleDrop, drawIndicatorY[0]-glyphLocation.y + INDICATOR_SIZE, 0, 0);
                triangleBase2 = vec4(drawIndicatorX[0] + triangleDrop, drawIndicatorY[0]-glyphLocation.y - INDICATOR_SIZE, 0, 0);
            }


            // Emit the three vertices for the indicator triangle.
            fLabelColor = indicatorColor;
            textureCoordinates = vec3(glyphX, glyphY, glyphPage);
            fDepth = calcdDepth;
            gl_Position = pMatrix * (glyphLocation + trianglePoint);
            EmitVertex();

            fLabelColor = indicatorColor;
            textureCoordinates = vec3(glyphX, glyphY, glyphPage);
            fDepth = calcdDepth;
            gl_Position = pMatrix * (glyphLocation + triangleBase1);
            EmitVertex();

            fLabelColor = indicatorColor;
            textureCoordinates = vec3(glyphX, glyphY, glyphPage);
            fDepth = calcdDepth;
            gl_Position = pMatrix * (glyphLocation + triangleBase2);
            EmitVertex();

            EndPrimitive();
        }
    }
}