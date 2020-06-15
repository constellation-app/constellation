#version 330 core

uniform samplerBuffer xyzTexture;

uniform mat4 mvMatrix;
uniform float morphMix;

in vec4 vColor;
in ivec4 data;

out vec4 vpointColor;
flat out ivec4 gData;

flat out float nradius;

void main(void) {
    // Pass the color to the fragment shader.
    vpointColor = vColor;
    gData = data;

    // Get the index into the xyzTexture.
    int vxIndex = gData.t;

    int offset = vxIndex * 2;
    vec3 v = texelFetch(xyzTexture, offset).stp;
    vec3 vEnd = texelFetch(xyzTexture, offset + 1).stp;
    vec3 mixedVertex = mix(v, vEnd, morphMix);

    gl_Position = mvMatrix * vec4(mixedVertex, 1);

    // Get the side radius of the associated vertex and pass that through
    // so the text is drawn relative to the node size.
    nradius = mix(texelFetch(xyzTexture, offset).q, texelFetch(xyzTexture, offset + 1).q, morphMix);
}
