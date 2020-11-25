#version 450


// === PUSH CONSTANT ===
layout(std140, push_constant) uniform PushConstant {
    mat4 mvpMatrix;
}pc;

// === PER VERTEX DATA IN ===
layout(location = 0) in vec3 vertex;
layout(location = 1) in vec4 color;

// === PER VERTEX DATA OUT ===
layout(location = 0) out vec4 gColor;

void main(void) {
    gColor = color;
    gl_Position = pc.mvpMatrix * vec4(vertex, 1);
    gl_Position.y = -gl_Position.y;
}
