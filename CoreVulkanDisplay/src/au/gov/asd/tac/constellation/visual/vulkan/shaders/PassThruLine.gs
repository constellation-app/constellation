#version 450


// === PER PRIMITIVE DATA IN ===
layout(lines) in;
layout(location = 0) in vec4 gColor[];


// === PER PRIMITIVE DATA OUT ===
layout(line_strip, max_vertices=2) out;
layout(location = 0) out vec4 fColor;


void main() {
    fColor = gColor[0];
    gl_Position = gl_in[0].gl_Position;
    EmitVertex();

    fColor = gColor[1];
    gl_Position = gl_in[1].gl_Position;
    EmitVertex();

    EndPrimitive();
}
