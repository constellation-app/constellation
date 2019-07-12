// Draw icons as point sprites.
// Make points that are nearer to the camera bigger.
#version 330 core

uniform samplerBuffer xyzTexture;

uniform mat4 mvMatrix;
uniform float morphMix;
uniform float visibilityLow;
uniform float visibilityHigh;

// Incoming per vertex...
in vec4 backgroundIconColor;

// This is a set of data for this vertex.
in ivec4 data;

flat out ivec4 gData;
out mat4 gBackgroundIconColor;
flat out int vxPosition;
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
    vxPosition = gl_VertexID;

    int index = gl_VertexID * 2;
    vec4 v0 = texelFetch(xyzTexture, index);
    vec4 v1 = texelFetch(xyzTexture, index+1);
    vec3 mixedVertex = mix(v0.xyz, v1.xyz, morphMix);

    float visibility = backgroundIconColor.a;
    if(visibility > max(visibilityLow, 0) && (visibility <= visibilityHigh || visibility > 1.0)) {
        gRadius = v0.w;
    } else {
        gRadius = -1;
    }

    gl_Position = mvMatrix * vec4(mixedVertex, 1);
}
