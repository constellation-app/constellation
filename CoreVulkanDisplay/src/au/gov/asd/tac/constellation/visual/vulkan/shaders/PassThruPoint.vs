#version 450


// === PUSH CONSTANT ===
layout(std140, push_constant) uniform UniformBlock {
    mat4 mvpMatrix;
}ub;


// === PER VERTEX DATA IN ===
layout(location = 0) in vec3 vertex;


void main(void) {
    gl_PointSize = 4.0;

    gl_Position = ub.mvpMatrix * vec4(vertex.x, vertex.y, vertex.z, 1);
    gl_Position.y = -gl_Position.y;
}
