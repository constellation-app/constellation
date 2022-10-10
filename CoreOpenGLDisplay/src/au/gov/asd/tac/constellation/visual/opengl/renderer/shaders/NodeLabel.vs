// Draw labels for nodes.
// The labels are drawn glyph by glyph: each shader call draws one glyph.
#version 330 core

// The z distance from the camera at which a label is no longer visible
const float LABEL_VISIBLE_DISTANCE = 150;

// Label scales are sent to the shader as integers between 1 and 64. Dividing by this factor converts them
// to float values between 0 and 4 as a proportion of the radius of the node.
const float LABEL_TO_NRADIUS_UNITS = 1/16.0;

// A constant that is used to scale all labels consistently that was chosen long ago for aesthetic reasons
// and hence needs to remain to ensure consistency across graphs until a new version of the schema addressing this issue is released.
const float LABEL_AESTHETIC_SCALE = 5/8.0;

// The gap between a node and the first label
const float LABEL_NODE_GAP = 0.2 / LABEL_AESTHETIC_SCALE;

// The amount to darken the background of the labels with respect to the background of the graph.
// 1 means no darkening, 0 means completely black
const float BACKGROUND_DARKENING_FACTOR = 0.8;

// .xyz = world coordinates of node.
// .a = radius of node.
uniform samplerBuffer xyzTexture;

// Matrix to project from world coordinates to camera coordinates
uniform mat4 mvMatrix;

// Each column is a node (bottom or top) label with the following structure:
// [0..2] rgb color (note label colors do not habve an alpha)
// [3] label size
uniform mat4 labelBottomInfo;
uniform mat4 labelTopInfo;

// Information from the graph's visual state
uniform float morphMix;
uniform float visibilityLow;
uniform float visibilityHigh;

// The index of the background glyph in the glyphInfo texture
uniform int backgroundGlyphIndex;

// Used to draw the label background.
uniform vec4 backgroundColor;

// [0] the index of the glyph in the glyphInfoTexture
// [1..2] x and y offsets of this glyph from the top centre of the line of text
// [3] The visibility of this glyph (constant for a node, but easier to pass in the batch).
in vec4 glyphLocationData;

// [0] The index of the node containg this glyph in the xyzTexture
// [1] The total scale of the lines and their labels up to this point (< 0 if this is a glyph in a bottom label)
// [2] The label number in which this glyph occurs
// [3] Unused
in ivec4 graphLocationData;

// Information about the texture location, color and scale of the glyph
out int glyphIndex;
out vec4 labelColor;
out float glyphScale;
// The scaling factor if the glyph we are rendering is in fact the background for a line of text.
// This will be one in all other cases.
out float backgroundScalingFactor;
// A value describing the side of the background on which an indicator should be for connection labels.
// This will always be zero here because we don't draw indicators for node labels.
out int drawIndicator;
// Locations for placing the aforementioned indicator. Again they are unused here
out float drawIndicatorX;
out float drawIndicatorY;
// The depth of the label which is used to bring labels in front of connections.
// As this feature is not used for node labels, depth simply gets set to the z_position of the label.
out float depth;

void main(void) {

    glyphIndex = int(glyphLocationData[0]);
    float glyphXOffset = glyphLocationData[1];
    float glyphYOffset = glyphLocationData[2];
    float glyphVis = glyphLocationData[3];

    int nodeIndex = graphLocationData[0];
    int totalScale = graphLocationData[1];
    int labelNumber = graphLocationData[2];

    // Find the xyz of the vertex that this glyph belongs to,
    // specified by an offset into the xyzTexture buffer.
    int offset = nodeIndex * 2;
    vec4 v = texelFetch(xyzTexture, offset);
    vec4 vEnd = texelFetch(xyzTexture, offset + 1);
    vec4 mixedVertex = mix(v, vEnd, morphMix);

    // Calculate the pixel coordinates of the vertex
    vec4 nodeLocation = mvMatrix * vec4(mixedVertex.xyz, 1);

    // Get the radius of the associated vertex
    float nradius = mixedVertex.w;


    // Get the size and color of this label from the relevant label information matrix
    mat4 labelInfo = totalScale < 0 ? labelBottomInfo : labelTopInfo;
    float labelScale = labelInfo[labelNumber][3] * LABEL_TO_NRADIUS_UNITS * LABEL_AESTHETIC_SCALE;
    glyphScale = nradius * labelScale;
    
    // Determine visiblity of this label based both on the visibility of the associated node, and the fade out distance for labels.
    float distance = -nodeLocation.z / nradius;
    float alpha = (glyphVis > max(visibilityLow, 0) && (glyphVis <= visibilityHigh || glyphVis > 1.0)) ?
        1 - smoothstep((LABEL_VISIBLE_DISTANCE - 20) * glyphScale, LABEL_VISIBLE_DISTANCE * glyphScale, distance) : 0.0;

    // Set the color (and background information if this glyph is the background)
    if (glyphIndex == backgroundGlyphIndex) {
        labelColor = vec4(backgroundColor.xyz * BACKGROUND_DARKENING_FACTOR, alpha);
        backgroundScalingFactor = abs(2 * glyphXOffset);
    } else {
        labelColor = vec4(labelInfo[labelNumber].xyz, alpha);
        backgroundScalingFactor = 1;
    }

    // Set all values that only pertain to connection labels to their defaults
    drawIndicator = 0;
    drawIndicatorX = 0;
    drawIndicatorY = 0;
    depth = nodeLocation.z;

    // The total vertical offset of the label from the centre of the node. Note that the top label adjustment exists because we are measuring
    // the offset to the top of the label, not the centre.
    float topLabelAdjustment = labelScale * (1 + sign(totalScale)) / 2.0;
    float labelYOffset = -nradius * ((totalScale * LABEL_TO_NRADIUS_UNITS * LABEL_AESTHETIC_SCALE) + (LABEL_NODE_GAP * sign(totalScale) + topLabelAdjustment));

    // Calculate the pixel coordinates of the glyph's location on the graph
    nodeLocation.x += glyphXOffset * glyphScale;
    nodeLocation.y -= (glyphYOffset * glyphScale) + labelYOffset;
    gl_Position = nodeLocation;
}
