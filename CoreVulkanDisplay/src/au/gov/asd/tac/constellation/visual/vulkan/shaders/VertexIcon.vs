// Draw icons as point sprites.
// Make points that are nearer to the camera bigger.
#version 450


// === PUSH CONSTANTS ===
layout(std140, push_constant) uniform VertexPushConstant {
    mat4 mvMatrix;
}vpc;


// === UNIFORMS ===
layout(std140, binding = 0) uniform UniformBlock {
    float morphMix;
    float visibilityLow;
    float visibilityHigh;
} ub;
layout(binding = 1) uniform samplerBuffer xyzTexture;


// === PER VERTEX DATA IN ===
layout(location = 0) in vec4 backgroundIconColor;
layout(location = 1) in ivec4 data;


// === PER VERTEX DATA OUT ===
layout(location = 0) flat out ivec4 gData;
layout(location = 1) out mat4 gBackgroundIconColor; //mat4 eats 4 slots
layout(location = 5) flat out int vxPosition;
layout(location = 6) flat out float gRadius;


void main(void) {
    // Pass stuff to the next shader.
    gData = data;
    gBackgroundIconColor = mat4(
        backgroundIconColor.r, 0, 0, 0,
        0, backgroundIconColor.g, 0, 0,
        0, 0, backgroundIconColor.b, 0,
        0, 0, 0, 1
    );
    vxPosition = gl_VertexIndex;

    int index = gl_VertexIndex * 2;
    vec4 v0 = texelFetch(xyzTexture, index);
    vec4 v1 = texelFetch(xyzTexture, index+1);
    vec3 mixedVertex = mix(v0.xyz, v1.xyz, ub.morphMix);

    float visibility = backgroundIconColor.a;
    if(visibility > max(ub.visibilityLow, 0) && (visibility <= ub.visibilityHigh || visibility > 1.0)) {
        gRadius = v0.w;
    } else {
        gRadius = -1;
    }

    gl_Position = vpc.mvMatrix * vec4(mixedVertex, 1);
}
