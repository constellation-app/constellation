#version 450


// === CONSTANTS ===
// The transperancy threshold before we stop drawing glyphs
// Prevents some of the artifacts we get from drawing things behind barely visible pixels that exist to anti-alias the glyph.
const float ALPHA_THRESHOLD = 0.1;


// === UNIFORMS ===
// Texture containing the grayscale images of the glyphs.
// .r = transperancy at a given pixel location of the glyph
// .gba = unused
layout(binding = 4) uniform sampler2DArray glyphImageTexture;


// === PER FRAGMENT DATA IN ===
layout(location = 0) noperspective centroid in vec3 textureCoordinates;
layout(location = 1) in vec4 fLabelColor;
layout(location = 2) flat in float fDepth;


// === PER FRAGMENT DATA OUT ===
out vec4 fragColor;


void main(void) {

    // Lookup the texture
    vec4 color = texture(glyphImageTexture, textureCoordinates);

    // Colour to emmit is that passes in with its alpha multiplied by the value from the glyph image texture
    // If the resulting alpha is below the threshold we discard.
    float alpha = color.r * fLabelColor.a;
    if (alpha < ALPHA_THRESHOLD) {
        discard;
    }
    fragColor = vec4(fLabelColor.rgb, alpha);

    // Set the depth as specified by the geometry shader
    gl_FragDepth = fDepth;

}
