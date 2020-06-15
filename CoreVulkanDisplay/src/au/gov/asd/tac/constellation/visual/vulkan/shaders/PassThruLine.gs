#version 330 core

layout(lines) in;
layout(line_strip, max_vertices=2) out;

in vec4 gColor[];
out vec4 fColor;

void main() {
    fColor = gColor[0];
    gl_Position = gl_in[0].gl_Position;
    EmitVertex();

    fColor = gColor[1];
    gl_Position = gl_in[1].gl_Position;
    EmitVertex();

    EndPrimitive();
}
