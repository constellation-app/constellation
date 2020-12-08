#version 450


// === PER PRIMITIVE DATA IN ===
layout(triangles) in;
layout(location = 0) in vec4 gColor[];


// === PER PRIMITIVE DATA OUT ===
layout(triangle_strip, max_vertices=3) out;
layout(location = 0) out vec4 fColor;


void main() {
    fColor = gColor[0];
    gl_Position = gl_in[0].gl_Position;
    EmitVertex();

    fColor = gColor[1];
    gl_Position = gl_in[1].gl_Position;
    EmitVertex();

    fColor = gColor[2];
    gl_Position = gl_in[2].gl_Position;
    EmitVertex();

    EndPrimitive();
}
