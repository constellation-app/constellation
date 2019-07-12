#version 330 core

const float LOOP_SIZE = 0.5;

layout(points) in;
layout(triangle_strip, max_vertices=4) out;

uniform mat4 pMatrix;

uniform float visibilityLow;
uniform float visibilityHigh;

in vec4 vpointColor[];
out vec4 pointColor;

flat in ivec4 gData[];
flat out ivec4 fData;

flat in float nradius[];

out vec2 pointCoord;

void main() {
    float visibility = vpointColor[0].q;
    if(visibility > max(visibilityLow, 0) && (visibility <= visibilityHigh || visibility > 1.0)) {
        vec4 v = gl_in[0].gl_Position;

        float sideRadius = nradius[0];
        v += vec4(sideRadius, sideRadius, 0, 0);

        vec4 ll = pMatrix * vec4(v.x-LOOP_SIZE, v.y - LOOP_SIZE, v.z, v.w);
        vec4 ul = pMatrix * vec4(v.x-LOOP_SIZE, v.y + LOOP_SIZE, v.z, v.w);
        vec4 lr = pMatrix * vec4(v.x+LOOP_SIZE, v.y - LOOP_SIZE, v.z, v.w);
        vec4 ur = pMatrix * vec4(v.x+LOOP_SIZE, v.y + LOOP_SIZE, v.z, v.w);

        //distance = ll.z;
        pointColor = vpointColor[0];
        fData = gData[0];
        pointCoord = vec2(0, 0.125);
        gl_Position = ll;
        EmitVertex();

        //distance = ul.z;
        pointColor = vpointColor[0];
        fData = gData[0];
        pointCoord = vec2(0, 0);
        gl_Position = ul;
        EmitVertex();

        //distance = lr.z;
        pointColor = vpointColor[0];
        fData = gData[0];
        pointCoord = vec2(0.125, 0.125);
        gl_Position = lr;
        EmitVertex();

        //distance = ur.z;
        pointColor = vpointColor[0];
        fData = gData[0];
        pointCoord = vec2(0.125, 0);
        gl_Position = ur;
        EmitVertex();

        EndPrimitive();
    }
}