#version 450


// === CONSTANTS ===
const float LOOP_SIZE = 0.5;


// === UNIFORMS ===
layout(std140, binding = 2) uniform UniformBlock {
    mat4 pMatrix;
    float visibilityLow;
    float visibilityHigh;
} ub;


// === PER PRIMITIVE DATA IN ===
layout(points) in;
layout(location = 0) in vec4 vpointColor[];
layout(location = 1) flat in ivec4 gData[];
layout(location = 2) flat in float nradius[];


// === PER PRIMITIVE DATA OUT ===
layout(triangle_strip, max_vertices=4) out;
layout(location = 0) out vec4 pointColor;
layout(location = 1) flat out ivec4 fData;
layout(location = 2) out vec2 pointCoord;


void main() {
    float visibility = vpointColor[0].q;
    if(visibility > max(ub.visibilityLow, 0) && (visibility <= ub.visibilityHigh || visibility > 1.0)) {
        vec4 v = gl_in[0].gl_Position;

        float sideRadius = nradius[0];
        v += vec4(sideRadius, sideRadius, 0, 0);

        vec4 ll = ub.pMatrix * vec4(v.x-LOOP_SIZE, v.y - LOOP_SIZE, v.z, v.w);
        vec4 ul = ub.pMatrix * vec4(v.x-LOOP_SIZE, v.y + LOOP_SIZE, v.z, v.w);
        vec4 lr = ub.pMatrix * vec4(v.x+LOOP_SIZE, v.y - LOOP_SIZE, v.z, v.w);
        vec4 ur = ub.pMatrix * vec4(v.x+LOOP_SIZE, v.y + LOOP_SIZE, v.z, v.w);

        //distance = ll.z;
        pointColor = vpointColor[0];
        fData = gData[0];
        pointCoord = vec2(0, 0.125);
        gl_Position = ll;
        gl_Position.y = -gl_Position.y;
        EmitVertex();

        //distance = ul.z;
        pointColor = vpointColor[0];
        fData = gData[0];
        pointCoord = vec2(0, 0);
        gl_Position = ul;
        gl_Position.y = -gl_Position.y;
        EmitVertex();

        //distance = lr.z;
        pointColor = vpointColor[0];
        fData = gData[0];
        pointCoord = vec2(0.125, 0.125);
        gl_Position = lr;
        gl_Position.y = -gl_Position.y;
        EmitVertex();

        //distance = ur.z;
        pointColor = vpointColor[0];
        fData = gData[0];
        pointCoord = vec2(0.125, 0);
        gl_Position = ur;
        gl_Position.y = -gl_Position.y;
        EmitVertex();

        EndPrimitive();
    }
}