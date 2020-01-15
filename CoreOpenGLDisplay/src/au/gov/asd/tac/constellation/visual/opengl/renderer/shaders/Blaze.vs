// Draw blazes next to nodes.

#version 330 core

uniform samplerBuffer xyzTexture;

uniform mat4 mvMatrix;
uniform float morphMix;

// This is a set of data for this vertex.
in vec4 blazeColor;
in ivec4 blazeData;

flat out vec4 gColor;
flat out ivec4 gData;

out float gnradius;

void main(void) {
    // Pass stuff to the next shader.
    gColor = blazeColor;
    gData = blazeData;

    // Find the xyz of the vertex that this blaze belongs to,
    // specified by an offset into the xyzTexture buffer.
    int offset = blazeData[0] * 2;
    vec3 v = texelFetch(xyzTexture, offset).stp;
    vec3 vEnd = texelFetch(xyzTexture, offset + 1).stp;
    vec3 mixedVertex = mix(v, vEnd, morphMix);

    gl_Position = mvMatrix * vec4(mixedVertex, 1);

    // Get the side radius of the associated vertex and pass that through
    // so the blaze is drawn relative to the node size.
    gnradius = texelFetch(xyzTexture, offset).q;
}
