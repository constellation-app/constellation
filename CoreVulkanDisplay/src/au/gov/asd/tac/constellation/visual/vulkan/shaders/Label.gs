// Geometry shader for labels.
// Outputs the 4 corners of a glyph, and if we are drawing the background of a connection label, possibly the 3 corners
// of an indicator triangle pointing at the conenction.
#version 450


// === CONSTANTS ===
// If the point is beyond this distance, don't draw it.
// Keep this in sync with the text fadeout in the fragment shader.
const float VISIBLE_DISTANCE = 150;

// The size of the indicator triangle on connection labels.
const float INDICATOR_SIZE = 0.1;

// An approximation of the trigonometric tangent function evaluated at various degree values.
const float SIN_SIXTY_DEGREES = 0.86603;


// === UNIFORMS ===
layout(std140, binding = 2) uniform UniformBlock {
    // Matrix to convert from camera coordinates to scene coordinates.
    mat4 pMatrix;

    // The scaling factor to convert from texture coordinates to world unit coordinates
    float widthScalingFactor;
    float heightScalingFactor;

    // Used to draw the connection indicator on the label background.
    vec4 highlightColor;

    // gl_DepthRange is not available for Vulkan shaders so we have to pass these through ourselves
    float near;
    float far;
} ub;

// [0..1] x,y coordinates of glyph in glyphImageTexture. The whole part of x is the texture number
// [2..3] width, height of glyph in glyphImageTexture
layout(binding = 3) uniform samplerBuffer glyphInfoTexture;


// === PER PRIMITIVE DATA IN ===
layout(points) in;

// The scaling factor if the glyph we are rendering is in fact the background for a line of text.
// This will be one in all other cases.
layout(location = 0) in float backgroundScalingFactor[];
layout(location = 1) in int glyphIndex[];
layout(location = 2) in vec4 labelColor[];
layout(location = 3) in float glyphScale[];
layout(location = 4) in int drawIndicator[];
layout(location = 5) in float drawIndicatorX[];
layout(location = 6) in float drawIndicatorY[];
layout(location = 7) in float depth[];


// === PER PRIMITIVE DATA OUT ===
layout(triangle_strip, max_vertices=7) out;

// The coordinates to lookup the glyph in the glyphImageTexture
layout(location = 0) noperspective centroid out vec3 textureCoordinates;
// The colour of this glyph (constant for a whole label, unless we are rendering its background or connection indicator).
layout(location = 1) out vec4 fLabelColor;

layout(location = 2) flat out float fDepth;


void main(void) {

    if(labelColor[0].a > 0) {
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
        vec4 depthVec = ub.pMatrix * vec4(glyphLocation.xy, depth[0], glyphLocation.w);
        float calcdDepth = ((ub.far - ub.near) * (depthVec.z / depthVec.w) + ub.far + ub.near) / 2.0;

        // The dimensions (in camera coordinates) of the glyph
        float width = glyphWidth * glyphScale[0] * ub.widthScalingFactor * backgroundScalingFactor[0];
        float height = glyphHeight * glyphScale[0] * ub.heightScalingFactor;

        // Emitt the four corners of the glyph in screen coordinates.
        // Upper Left
        fLabelColor = labelColor[0];
        textureCoordinates = vec3(glyphX, glyphY, glyphPage);
        fDepth = calcdDepth;
        gl_Position = ub.pMatrix * glyphLocation;
        gl_Position.y = -gl_Position.y;
        EmitVertex();

        // Lower Left
        fLabelColor = labelColor[0];
        textureCoordinates = vec3(glyphX, glyphY + glyphHeight, glyphPage);
        fDepth = calcdDepth;
        vec4 locationOffset = vec4(0, -height, 0, 0);
        gl_Position = ub.pMatrix * (glyphLocation + locationOffset);
        gl_Position.y = -gl_Position.y;
        EmitVertex();

        // Upper Right
        fLabelColor = labelColor[0];
        textureCoordinates = vec3(glyphX + glyphWidth, glyphY, glyphPage);
        fDepth = calcdDepth;
        locationOffset = vec4(width, 0, 0, 0);
        gl_Position = ub.pMatrix * (glyphLocation + locationOffset);
        gl_Position.y = -gl_Position.y;
        EmitVertex();

        // Lower Right
        fLabelColor = labelColor[0];
        textureCoordinates = vec3(glyphX + glyphWidth, glyphY + glyphHeight, glyphPage);
        fDepth = calcdDepth;
        locationOffset =  vec4(width, -height, 0, 0);
        gl_Position = ub.pMatrix * (glyphLocation + locationOffset);
        gl_Position.y = -gl_Position.y;
        EmitVertex();

        EndPrimitive();

        // Draw an indicator for a connection label if necessary.
        if (drawIndicator[0] != 0) {

            // recalculate depth for direction indicator noting that we need to push the depth slightly forward of the
            // background glyph, but not as far forward as the other glyphs.
            float forward = 0.00025;
            vec4 depthVec = ub.pMatrix * vec4(glyphLocation.xy, depth[0] + forward, glyphLocation.w);
            float calcdDepth = ((ub.far - ub.near)*(depthVec.z / depthVec.w) + ub.far + ub.near) / 2.0;

            // The indicator colour is the graph highlight colour, with the alpha value that is constant across the label.
            vec4 indicatorColor = vec4(ub.highlightColor.xyz, 1.5 * labelColor[0].a);
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
            gl_Position = ub.pMatrix * (glyphLocation + trianglePoint);
            gl_Position.y = -gl_Position.y;
            EmitVertex();

            fLabelColor = indicatorColor;
            textureCoordinates = vec3(glyphX, glyphY, glyphPage);
            fDepth = calcdDepth;
            gl_Position = ub.pMatrix * (glyphLocation + triangleBase1);
            gl_Position.y = -gl_Position.y;
            EmitVertex();

            fLabelColor = indicatorColor;
            textureCoordinates = vec3(glyphX, glyphY, glyphPage);
            fDepth = calcdDepth;
            gl_Position = ub.pMatrix * (glyphLocation + triangleBase2);
            gl_Position.y = -gl_Position.y;
            EmitVertex();

            EndPrimitive();
        }
    }
}