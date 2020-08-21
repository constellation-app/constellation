#version 450


// === CONSTANTS ===
const float PI = 3.14159265;
const float border = 0.002;


// === UNIFORMS ===
layout(std140, binding = 1) uniform UniformBlock {
    mat4 pMatrix;
    float scale;
    float visibilityLow;
    float visibilityHigh;
} ub;


// === PER PRIMITIVE DATA IN ===
layout(points) in;
layout(location = 0) flat in vec4 gColor[];
layout(location = 1) flat in ivec4 gData[];
layout(location = 2) in float gnradius[];


// === PER PRIMITIVE DATA OUT ===
layout(triangle_strip, max_vertices=3) out;
layout(location = 0) flat out vec4 fColor;
layout(location = 1) out vec2 pointCoord;
layout(location = 2) out float fnradius;
layout(location = 3) flat out int isPointer;


vec4 rotate(vec4 v, float dx, float dy, float x, float y) {
    return ub.pMatrix * vec4(v.x + y * dx - x * dy, v.y + x * dx + y * dy, v.z, v.w);
}


void main() {
    float visibility = gColor[0][3];
    if(visibility > max(ub.visibilityLow, 0) && (visibility <= ub.visibilityHigh || visibility > 1.0)) {
        vec4 v = gl_in[0].gl_Position;
        float angle = radians(float(gData[0].p));

        // The GL angle has 0 pointing east incrementing anti-clockwise, just like real maths.
        // We want 0 pointing north incrementing clockwise.
        angle = PI/2-angle;

        float dx = cos(angle);
        float dy = sin(angle);

        v.x += gnradius[0] * dx;
        v.y += gnradius[0] * dy;

        dx *= ub.scale * 0.5 * v.z/-10;
        dy *= ub.scale * 0.5 * v.z/-10;

        isPointer = 1;
        fColor = gColor[0];
        pointCoord = vec2(0.4, 0);
        gl_Position = rotate(v, dx, dy, 0, 0);
        gl_Position.y = -gl_Position.y;
        EmitVertex();

        isPointer = 1;
        fColor = gColor[0];
        pointCoord = vec2(-0.7, 1);
        gl_Position = rotate(v, dx, dy, -1, 5);
        gl_Position.y = -gl_Position.y;
        EmitVertex();

        isPointer = 1;
        fColor = gColor[0];
        pointCoord = vec2(0.7, 1);
        gl_Position = rotate(v, dx, dy, 1, 5);
        gl_Position.y = -gl_Position.y;
        EmitVertex();

        EndPrimitive();
    }
}
