#version 330 core

uniform samplerBuffer xyzTexture;

uniform mat4 mvMatrix;
uniform float morphMix;

in vec4 vColor;
in ivec4 data;

out vec4 vpointColor;
flat out ivec4 gData;

void main(void) {
    // Pass the color to the fragment shader.
    //
    vpointColor = vColor;
    gData = data;

    // Decode the index into the xyzTexture and the LINE_INFO from each other and put the LINE_INFO back.
    //
    int vxIndex = gData[1] / 4;
    gData[1] = gData[1] - vxIndex * 4;

    int offset = vxIndex * 2;
    vec3 v = texelFetch(xyzTexture, offset).stp;
    vec3 vEnd = texelFetch(xyzTexture, offset + 1).stp;
    vec3 mixedVertex = mix(v, vEnd, morphMix);

    gl_Position = mvMatrix * vec4(mixedVertex, 1);
}
