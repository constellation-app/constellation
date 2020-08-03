// Draw icons as point sprites.
// Make points that are nearer to the camera bigger.
#version 450


// === UNIFORMS ===
layout(std140, push_constant) uniform UniformBlock {
    mat4 mvMatrix;
    float visibilityLow;
    float visibilityHigh;
} ub;


// === PER VERTEX DATA IN ===
// This is a set of data for this vertex.
// TODO_TT: maybe we can remove data from the vert shader altogether as it is
// unused and in the geo shader only the first element of data is used.
layout(location = 0) in ivec2 data;
layout(location = 1) in vec4 backgroundIconColor;


// === PER VERTEX DATA OUT ===
layout(location = 0) out ivec2 gData; // {icon indexes (encoded to int), digit index * 4)
layout(location = 1) out mat4 gBackgroundIconColor; //mat4 eats 4 slots
layout(location = 5) flat out float gRadius;


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
    if(visibility > max(ub.visibilityLow, 0.0) && (visibility <= ub.visibilityHigh || visibility > 1.0)) {
        gRadius = 1;
    } else {
        gRadius = -1;
    }

    gl_Position = ub.mvMatrix * vec4(digitPosition, 1);
}
