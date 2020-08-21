#version 450


// === UNIFORMS
layout(std140, push_constant) uniform UniformBlock {
    mat4 mvpMatrix;
}ub;

// === PER VERTEX DATA IN
layout(location = 0) in vec3 vertex;
layout(location = 1) in vec4 color;

// === PER VERTEX DATA OUT
layout(location = 0) out vec4 gColor;

void main(void) {
    gColor = color;
    gl_Position = ub.mvpMatrix * vec4(vertex, 1);
    gl_Position.y = -gl_Position.y;
}
