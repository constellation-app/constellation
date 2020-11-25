#version 450


// === PER PRIMITIVE DATA IN ===
layout(triangles) in;
layout(location = 0) in vec4 gData[];


// === PER PRIMITIVE DATA IN ===
layout(triangle_strip, max_vertices=3) out;
layout(location = 0) flat out float tex;
layout(location = 1) out vec2 pointCoord;
layout(location = 2) flat out vec2 width_height_frac;


void main() {
    float visible = gData[0].p;
    if (visible > 0) {
        width_height_frac = vec2(gData[1].p, gData[2].p);
        tex = gData[0].q;
        pointCoord = gData[0].xy;
        gl_Position = gl_in[0].gl_Position;
        EmitVertex();

        width_height_frac = vec2(gData[1].p, gData[2].p);
        tex = gData[0].q;
        pointCoord = gData[1].xy;
        gl_Position = gl_in[1].gl_Position;
        EmitVertex();

        width_height_frac = vec2(gData[1].p, gData[2].p);
        tex = gData[0].q;
        pointCoord = gData[2].xy;
        gl_Position = gl_in[2].gl_Position;
        EmitVertex();

        EndPrimitive();
    }
}
