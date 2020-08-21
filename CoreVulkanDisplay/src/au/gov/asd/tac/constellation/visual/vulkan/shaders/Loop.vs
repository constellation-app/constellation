#version 450


// === UNIFORMS ===
layout(binding = 0) uniform samplerBuffer xyzTexture;
layout(std140, binding = 1) uniform UniformBlock {
    mat4 mvMatrix;
    float morphMix;
} ub;


// === PER VERTEX DATA IN ===
layout(location = 0) in vec4 vColor;
layout(location = 1) in ivec4 data;


// === PER VERTEX DATA OUT ===
layout(location = 0) out vec4 vpointColor;
layout(location = 1) flat out ivec4 gData;
layout(location = 2) flat out float nradius;


void main(void) {
    // Pass the color to the fragment shader.
    vpointColor = vColor;
    gData = data;

    // Get the index into the xyzTexture.
    int vxIndex = gData.t;

    int offset = vxIndex * 2;
    vec3 v = texelFetch(xyzTexture, offset).stp;
    vec3 vEnd = texelFetch(xyzTexture, offset + 1).stp;
    vec3 mixedVertex = mix(v, vEnd, ub.morphMix);

    gl_Position = ub.mvMatrix * vec4(mixedVertex, 1);

    // Get the side radius of the associated vertex and pass that through
    // so the text is drawn relative to the node size.
    nradius = mix(texelFetch(xyzTexture, offset).q, texelFetch(xyzTexture, offset + 1).q, ub.morphMix);
}
