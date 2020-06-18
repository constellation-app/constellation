// Color icons in one of two ways.
//
// If drawHitTest is false, draw a texture using pointColor[3].
// If drawHitTest is true, convert pointColor[2] to unique color to be used for hit testing.
#version 450



// === UNIFORMS
// note this is an opaque uniform, ie not data we pass in but an object this shader can reference
layout(binding = 2) uniform sampler2DArray images;


// === PER TEXEL DATA IN
layout(location = 0) flat in mat4 iconColor;
layout(location = 4) noperspective centroid in vec3 textureCoords;


// === PER TEXEL DATA OUT
layout(location = 0) out vec4 fragColor;


void main(void) {
    //TODO_TT: hook up texture
    fragColor = iconColor * vec4(1.0, 1.0, 1.0, 1.0);
    //fragColor = iconColor * texture(images, textureCoords);

    // Discarding only when fragColor.a==0.0 means that some "nearly transparent" pixels get drawn, which causes weird see-through
    // artifacts around the edges. Instead we'll discard nearly transparent pixels as well at an arbitrary cut-off point.
    if(fragColor.a < 0.1) {
        discard;
    }
}
