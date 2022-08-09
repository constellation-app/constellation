// Draw labels for connections.
// The labels are drawn glyph by glyph: each shader call draws one glyph.
#version 330 core

// The z distance from the camera at which a label is no longer visible
const float LABEL_VISIBLE_DISTANCE = 150;

// Label scales are sent to the shader as integers between 1 and 64. Dividing by this factor converts them
// to float values between 0 and 4 as a proportion of the radius of the node.
const float LABEL_TO_NRADIUS_UNITS = 1 / 16.0;

// Line widths are sent to the shader as integers where 256 is equivalent to a node radius of one
const float LINE_WIDTH_TO_NRADIUS_UNITS = 1 / 256.0;

// The amount to darken the background of the labels with respect to the background of the graph.
// 1 means no darkening, 0 means completely black
const float BACKGROUND_DARKENING_FACTOR = 0.8;

// A constant that is used to scale all labels consistently that was chosen long ago for aesthetic reasons
// and hence needs to remain to ensure consistency across graphs until a new version of the schema addressing this issue is released.
const float LABEL_AESTHETIC_SCALE = 5 / 8.0;

// An approximation of the trigonometric tangent function evaluated at various degree values.
const float SIN_EIGHTY_DEGREES = 0.98481;

// A small constant added to the depths of labels in cases where they would otherwise appear past both nodes of their conncetion.
const float LABEL_DEPTH_PUSHBACK = 0.001;

// .xyz = world coordinates of node.
// .a = radius of node.
uniform samplerBuffer xyzTexture;

// Matrix to project from world coordinates to camera coordinates
uniform mat4 mvMatrix;

// Each column is a connection label with the following structure:
// [0..2] rgb color (note label colors do not habve an alpha)
// [3] label size
uniform mat4 labelInfo;

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
// This will be zero when no indicator should be drawn.
out int drawIndicator;
// Locations for placing the aforementioned indicator.
out float drawIndicatorX;
out float drawIndicatorY;
// The depth of the label which is used to bring labels in front of connections.
out float depth;

void main(void) {

    float labelWidth = glyphLocationData[0];
    float glyphXOffset = glyphLocationData[1];
    float glyphYOffset = glyphLocationData[2];
    float glyphVis = glyphLocationData[3];

    int lowNodeIndex = graphLocationData[0];
    int highNodeIndex = graphLocationData[1];
    float offset = LINE_WIDTH_TO_NRADIUS_UNITS * (graphLocationData[2] >> 16);
    int totalScale = (graphLocationData[2] >> 2) & 0x3FF;
    int labelNumber = graphLocationData[2] & 0x3;
    glyphIndex = (graphLocationData[3] >> 8) & 0xFFF;
    float stagger = (graphLocationData[3] & 0xFF) / 256.0;

    // Find the xyz of the endpoint vertices,
    // specified by an offset into the xyzTexture buffer.
    int lowOffset = lowNodeIndex * 2;
    vec4 v1 = texelFetch(xyzTexture, lowOffset);
    vec4 v1End = texelFetch(xyzTexture, lowOffset + 1);
    vec4 mixedV1 = mix(v1, v1End, morphMix);

    int highOffset = highNodeIndex * 2;
    vec4 v2 = texelFetch(xyzTexture, highOffset);
    vec4 v2End = texelFetch(xyzTexture, highOffset + 1);
    vec4 mixedV2 = mix(v2, v2End, morphMix);

    // Calculate the unit vector parallel to the connection
    v1 = mvMatrix * vec4(mixedV1.xyz, 1);
    v2 = mvMatrix * vec4(mixedV2.xyz, 1);
    vec3 connectionDirection = normalize(v1.xyz - v2.xyz);

    // The unit vector perpendicular to the connection in the x-y plane
    vec3 connectionPerpendicular = normalize(cross(vec3(connectionDirection.xy, 0), vec3(0, 0, 1)));

    // Offset both nodes to match the offset of the connection being labeled
    // This offset occurs when more than one parallel edge/transaction is drawn between two nodes.
    v1.xy += connectionPerpendicular.xy * offset;
    v2.xy += connectionPerpendicular.xy * offset;

    // Calculate the pixel coordinates of the correct place along the connection
    if (length(v1.xyz - v2.xyz) - mixedV1.w - mixedV2.w > 0) {
        v1.xyz -= mixedV1.w * connectionDirection;
        v2.xyz += mixedV2.w * connectionDirection;
    }
    vec4 connectionLocation = vec4(mix(v1.xyz, v2.xyz, stagger), 1);

    // Get the size and color of this label from the relevant label information matrix
    glyphScale = labelInfo[labelNumber][3] * LABEL_TO_NRADIUS_UNITS * LABEL_AESTHETIC_SCALE;

    // Determine visiblity of this label based both on the visibility of the associated node, and the fade out distance for labels.
    float distance = -connectionLocation.z;
    float alpha = (glyphVis > max(visibilityLow, 0) && (glyphVis <= visibilityHigh || glyphVis > 1.0)) ?
        1 - smoothstep((LABEL_VISIBLE_DISTANCE-20) * glyphScale, LABEL_VISIBLE_DISTANCE * glyphScale, distance) : 0.0;

    // The total vertical offset of the label from the line joining two nodes.
    float labelYOffset = (totalScale * LABEL_TO_NRADIUS_UNITS * LABEL_AESTHETIC_SCALE);
    // We need to subtract half the size of the first connection label from every line's Y offset,
    // since we want the connection to align with the first label's centre (rather than its top).
    labelYOffset -= 0.5 * labelInfo[0][3] * LABEL_TO_NRADIUS_UNITS * LABEL_AESTHETIC_SCALE;

    // Set the color appropritely - this comes from the labelInfo matrix for a normal glyph, or the graph background color
    // if it is a background glyph.
    labelColor = glyphIndex == backgroundGlyphIndex ?
        vec4(backgroundColor.xyz * BACKGROUND_DARKENING_FACTOR, alpha) : vec4(labelInfo[labelNumber].xyz, alpha);

    // Calculate the pixel coordinates of the glyph's location on the graph
    vec4 locationOffset = vec4(glyphXOffset * glyphScale, -(glyphYOffset * glyphScale) - labelYOffset, 0, 0);
    vec4 labelLocation = connectionLocation + vec4(labelWidth * glyphScale, -labelYOffset, 0, 0);

    // In this section of the shader we calculate where the connection intecepts the boundary of the label
    // We use this for two purposes:
    // - to set the depth of the whole label to the closest z-position of all the intercepts thereby bringing it in front of the connection
    // - to draw indicators on the label which point at the connection.
    float distanceAlongConnection;
    float connectionInterceptX;
    float connectionInterceptY;
    float topIntercept;
    float topInterceptZ;
    bool hasTop = false;
    float bottomIntercept;
    float bottomInterceptZ;
    bool hasBottom = false;
    float leftIntercept;
    float leftInterceptZ;
    bool hasLeft = false;
    float rightIntercept;
    float rightInterceptZ;
    bool hasRight = false;
    depth = labelLocation.z;
    drawIndicator = 0;

    // The scaling factor for the background glyph - used to draw the background at the correct size in the geometry shader
    backgroundScalingFactor = abs(2 * labelWidth);
    // The location of the bottom right corner of the label
    vec4 labelBRLocation = labelLocation + vec4(glyphScale * backgroundScalingFactor, -glyphScale, 0, 0);
    // The near plane
    float near = gl_DepthRange.near;

    // Look for an intercept at the top of the label
    float beta = labelLocation.y / labelLocation.z;
    if (beta * connectionDirection.z - connectionDirection.y != 0) {
        // Find the distance from v1 along the connection to the point where the ray from the camera through the
        // top of the label meets the connection.
        distanceAlongConnection = (v1.y - beta * v1.z) / (beta * connectionDirection.z - connectionDirection.y);
        // Find the coordinates of the aforementioned point
        connectionInterceptX = v1.x +  distanceAlongConnection * connectionDirection.x;
        topInterceptZ = v1.z +  distanceAlongConnection * connectionDirection.z;
        // Check that this point actually lies between v1 and v2
        if (-topInterceptZ > near && connectionInterceptX >= min(v1.x, v2.x) && connectionInterceptX <= max(v1.x, v2.x)) {
            // Calculate the intercept on the top of the label by scaling this point
            topIntercept = (connectionInterceptX * labelLocation.z) / topInterceptZ;
            // If the intercept actually lies on the top side of the label, flag that there is a top intercept
            if (topIntercept > labelLocation.x && topIntercept < labelLocation.x + glyphScale * backgroundScalingFactor) {
                hasTop = true;
                depth = labelLocation.z + abs(topInterceptZ - labelLocation.z);
            }
        }
    }
    // Look for an intercept at the bottom of the label
    beta = labelBRLocation.y / labelBRLocation.z;
    if (beta*connectionDirection.z - connectionDirection.y != 0) {
        // Find the distance from v1 along the connection to the point where the ray from the camera through the
        // bottom of the label meets the connection.
        distanceAlongConnection = (v1.y - beta * v1.z) / (beta*connectionDirection.z - connectionDirection.y);
        // Find the coordinates of the aforementioned point
        connectionInterceptX = v1.x +  distanceAlongConnection * connectionDirection.x;
        bottomInterceptZ = v1.z + distanceAlongConnection * connectionDirection.z;
        // Check that this point actually lies between v1 and v2
        if (-bottomInterceptZ > near && connectionInterceptX >= min(v1.x, v2.x) && connectionInterceptX <= max(v1.x, v2.x)) {
            // Calculate the intercept on the bottom of the label by scaling this point
            bottomIntercept = (connectionInterceptX * labelBRLocation.z) / bottomInterceptZ;
            // If the intercept actually lies on the bottom side of the label, flag that there is a bottom intercept
            if (bottomIntercept > labelLocation.x && bottomIntercept < labelLocation.x + glyphScale * backgroundScalingFactor) {
                hasBottom = true;
            }
        }
    }

    // If either a top or bottom intercept was found, set the depth and details about the label indicator appropriately.
    if (hasTop && hasBottom) {
        if (topInterceptZ > bottomInterceptZ) {
            drawIndicatorX = topIntercept;
            drawIndicatorY = 0;
            depth = topInterceptZ;
            drawIndicator = 1;
        } else {
            drawIndicatorX = bottomIntercept;
            drawIndicatorY = -glyphScale;
            depth = bottomInterceptZ;
            drawIndicator = 2;
        }
    } else if (hasTop) {
        drawIndicatorX = topIntercept;
        drawIndicatorY = 0;
        depth = max(topInterceptZ, bottomInterceptZ);
        drawIndicator = 1;
    } else if (hasBottom) {
        drawIndicatorX = bottomIntercept;
        drawIndicatorY = -glyphScale;
        depth = max(topInterceptZ, bottomInterceptZ);
        drawIndicator = 2;
    }

    // If there are no top or bottom intercepts, look for one on the left or the right
    else {
        // Look for an intercept at the left of the label
        beta = labelLocation.x / labelLocation.z;
        if (beta * connectionDirection.z - connectionDirection.x != 0) {
            // Find the distance from v1 along the connection to the point where the ray from the camera through the
            // left of the label meets the connection.
            distanceAlongConnection = (v1.x - beta * v1.z) / (beta * connectionDirection.z - connectionDirection.x);
            // Find the coordinates of the aforementioned point
            connectionInterceptY = v1.y +  distanceAlongConnection * connectionDirection.y;
            leftInterceptZ = v1.z +  distanceAlongConnection*connectionDirection.z;
            // Check that this point actually lies between v1 and v2
            if (-leftInterceptZ > near && connectionInterceptY >= min(v1.y, v2.y) && connectionInterceptY <= max(v1.y, v2.y)) {
                // Calculate the intercept on the left of the label by scaling this point
                leftIntercept = (connectionInterceptY * labelLocation.z) / leftInterceptZ;
                // If the intercept actually lies on left top side of the label, flag that there is a top intercept
                if (leftIntercept < labelLocation.y && leftIntercept > labelLocation.y - glyphScale) {
                    hasLeft = true;
                }
            }
        }
        // Look for an intercept at the right of the label
        beta = labelBRLocation.x / labelBRLocation.z;
        if (beta*connectionDirection.z - connectionDirection.x != 0) {
            // Find the distance from v1 along the connection to the point where the ray from the camera through the
            // right of the label meets the connection.
            distanceAlongConnection = (v1.x - beta*v1.z) / (beta * connectionDirection.z - connectionDirection.x);
            // Find the coordinates of the aforementioned point
            connectionInterceptY = v1.y +  distanceAlongConnection * connectionDirection.y;
            rightInterceptZ = v1.z +  distanceAlongConnection * connectionDirection.z;
            // Check that this point actually lies between v1 and v2
            if (-rightInterceptZ > near && connectionInterceptY >= min(v1.y, v2.y) && connectionInterceptY <= max(v1.y, v2.y)) {
                // Calculate the intercept on the right of the label by scaling this point
                rightIntercept = (connectionInterceptY * labelBRLocation.z) / rightInterceptZ;
                // If the intercept actually lies on the right side of the label, flag that there is a top intercept
                if (rightIntercept < labelLocation.y && rightIntercept > labelLocation.y - glyphScale) {
                    hasRight = true;
                }
            }
        }

        // If either a left or right intercept was found, set the depth and details about the label indicator appropriately.
        if (hasLeft && hasRight) {
            if (leftInterceptZ > rightInterceptZ) {
                drawIndicatorX = 0;
                drawIndicatorY = leftIntercept;
                drawIndicator = 3;
                depth = leftInterceptZ;
            } else {
                drawIndicatorX = glyphScale*backgroundScalingFactor;
                drawIndicatorY = rightIntercept;
                drawIndicator = 4;
                depth = rightInterceptZ;
            }
        } else if (hasLeft) {
            drawIndicatorX = 0;
            drawIndicatorY = leftIntercept;
            depth = max(leftInterceptZ, rightInterceptZ);
            drawIndicator = 3;
        } else if (hasRight) {
            drawIndicatorX = glyphScale * backgroundScalingFactor;
            drawIndicatorY = rightIntercept;
            depth = max(leftInterceptZ, rightInterceptZ);
            drawIndicator = 4;
        }

    }

    // If we are not looking at the background glyph and the first label, we don't want to draw an indicator.
    if (glyphIndex != backgroundGlyphIndex || totalScale != 0) {
        drawIndicator = 0;
    }
    // If we are not looking at the background glyph, reset the background scaling factor to the default
    if (glyphIndex != backgroundGlyphIndex) {
        backgroundScalingFactor = 1;
    }

    // Used to brings glyphs slightly forward - text glyphs are brought further forward than the background glyph.
    // Note that both of these quantities are strictly smaller than LABEL_DEPTH_PUSHBACK
    float bringForward = glyphIndex == backgroundGlyphIndex ? 0.00025 : 0.00075;


    // If the calculated depth is in front of the connection's anterior node,
    // clamp it to the depth of this node push it backwards a small amount.
    if (depth >= max(v1.z, v2.z)) {
        depth = max(v1.z, v2.z) - LABEL_DEPTH_PUSHBACK;
    }
    // If the calculated depth is behind the connection's posterior node,
    // clamp it to the depth of this node and push it forward a small amount.
    if (depth <= min(v1.z, v2.z)) {
        depth = min(v1.z, v2.z) + LABEL_DEPTH_PUSHBACK;
    }
    // If the calculated depth is in front of the near plane,
    // clamp it to the depth of this plane and push it backwards a small amount.
    if(-depth < near) {
        depth = -near - LABEL_DEPTH_PUSHBACK;
    }
    // Bring this glyph forwards by the appropriate amount depending on whether or not it is the background glyph.
    depth += bringForward;

    // Output the location of this glyph.
    gl_Position = connectionLocation+locationOffset;
}
