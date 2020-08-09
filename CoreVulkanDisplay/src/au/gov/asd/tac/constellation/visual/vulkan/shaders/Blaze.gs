#version 330 core

layout(points) in;
layout(triangle_strip, max_vertices=3) out;

const float PI = 3.14159265;
const float border = 0.002;

uniform mat4 pMatrix;

uniform float scale;
uniform float visibilityLow;
uniform float visibilityHigh;

flat in vec4 gColor[];
flat out vec4 fColor;

flat in ivec4 gData[];
//flat out ivec4 fData;

out vec2 pointCoord;
in float gnradius[];
out float fnradius;

flat out int isPointer;

vec4 rotate(vec4 v, float dx, float dy, float x, float y) {
    return pMatrix * vec4(v.x + y * dx - x * dy, v.y + x * dx + y * dy, v.z, v.w);
}

void main() {
    float visibility = gColor[0][3];
    if(visibility > max(visibilityLow, 0) && (visibility <= visibilityHigh || visibility > 1.0)) {
        vec4 v = gl_in[0].gl_Position;
        float angle = radians(float(gData[0].p));

        // The GL angle has 0 pointing east incrementing anti-clockwise, just like real maths.
        // We want 0 pointing north incrementing clockwise.
        angle = PI/2-angle;

        float dx = cos(angle);
        float dy = sin(angle);

        v.x += gnradius[0] * dx;
        v.y += gnradius[0] * dy;

        dx *= scale * 0.5 * v.z/-10;
        dy *= scale * 0.5 * v.z/-10;

        isPointer = 1;
        fColor = gColor[0];
        pointCoord = vec2(0.4, 0);
        gl_Position = rotate(v, dx, dy, 0, 0);
        EmitVertex();

        isPointer = 1;
        fColor = gColor[0];
        pointCoord = vec2(-0.7, 1);
        gl_Position = rotate(v, dx, dy, -1, 5);
        EmitVertex();

        isPointer = 1;
        fColor = gColor[0];
        pointCoord = vec2(0.7, 1);
        gl_Position = rotate(v, dx, dy, 1, 5);
        EmitVertex();

        EndPrimitive();
    }
}
