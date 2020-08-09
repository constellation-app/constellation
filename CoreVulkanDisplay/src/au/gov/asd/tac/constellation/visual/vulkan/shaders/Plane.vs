#version 330 core

uniform mat4 mvpMatrix;

in vec3 vertex;
in vec4 data;

out vec4 gData;

void main(void) {
    gData = data;
    gl_Position = mvpMatrix * vec4(vertex, 1);
}
