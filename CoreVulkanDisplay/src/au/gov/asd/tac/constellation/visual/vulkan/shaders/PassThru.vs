#version 330 core


// === UNIFORMS
layout(std140) uniform UniformBlock {
    mat4 mvpMatrix;
};

in vec3 vertex;
in vec4 color;

out vec4 gColor;

void main(void) {
    gColor = color;
    gl_Position = mvpMatrix * vec4(vertex, 1);
}
