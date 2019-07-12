// Draw icons as point sprites.
// Make points that are nearer to the camera bigger.
#version 330 core

uniform mat4 mvMatrix;
uniform float visibilityLow;
uniform float visibilityHigh;
uniform float offset;

// This is a set of data for this vertex.
in ivec2 data;
in vec4 backgroundIconColor;

flat out ivec2 gData;
out mat4 gBackgroundIconColor;
flat out float gRadius;

void main(void) {
    // Pass stuff to the next shader.
    gData = data;
    gBackgroundIconColor = mat4(
        backgroundIconColor.r, 0, 0, 0,
        0, backgroundIconColor.g, 0, 0,
        0, 0, backgroundIconColor.b, 0,
        0, 0, 0, 1
    );

    vec3 digitPosition = vec3(data[1], 0, 0);
    
    float visibility = backgroundIconColor.a;
    if(visibility > max(visibilityLow, 0) && (visibility <= visibilityHigh || visibility > 1.0)) {
        gRadius = 1;
    } else {
        gRadius = -1;
    }

    gl_Position = mvMatrix * vec4(digitPosition, 1);
}
