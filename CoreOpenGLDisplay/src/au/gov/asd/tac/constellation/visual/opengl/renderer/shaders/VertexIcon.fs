// Color icons in one of two ways.
//
// If drawHitTest is false, draw a texture using pointColor[3].
// If drawHitTest is true, convert pointColor[2] to unique color to be used for hit testing.
//

#version 330 core

uniform sampler2DArray images;

// If non-zero, use the texture to color the icon.
// Otherwise, use a unique color for hit testing.
//
uniform int drawHitTest;

flat in vec4 hitBufferValue;
flat in mat4 iconColor;
noperspective centroid in vec3 textureCoords;

out vec4 fragColor;

void main(void) {
    if(drawHitTest==0) {
        fragColor = iconColor * texture(images, textureCoords);

        // Discarding only when fragColor.a==0.0 means that some "nearly transparent" pixels get drawn, which causes weird see-through
        // artifacts around the edges. Instead we'll discard nearly transparent pixels as well at an arbitrary cut-off point.
        if(fragColor.a < 0.1) {
            discard;
        }
    } else {
        if (iconColor[3][3] * texture(images, textureCoords).a > 0.1) {
            fragColor = hitBufferValue;
        } else {
            discard;
        }
    }
}
