#version 330 core

// The transparency threshold before we stop drawing glyphs
// Prevents some of the artifacts we get from drawing things behind barely visible pixels that exist to anti-alias the glyph.
const float ALPHA_THRESHOLD = 0.1;

// Anaglyphic drawing.
//
uniform int greyscale;


// Texture containing the grayscale images of the glyphs.
// .r = transparency at a given pixel location of the glyph
// .gba = unused
uniform sampler2DArray glyphImageTexture;

noperspective centroid in vec3 textureCoordinates;
in vec4 fLabelColor;
flat in float fDepth;

out vec4 fragColor;

void main(void) {

    // Lookup the texture
    vec4 color = texture(glyphImageTexture, textureCoordinates);

    // Color to emit is that passes in with its alpha multiplied by the value from the glyph image texture
    // If the resulting alpha is below the threshold we discard.
    float alpha = color.r * fLabelColor.a;
    if (alpha < ALPHA_THRESHOLD) {
        discard;
    }
    fragColor = vec4(fLabelColor.rgb, alpha);

    if (greyscale==1) {
        // Anaglyphic drawing.
        // Choose whichever "convert to greyscale" algorithm works best.
        //

        // Luminosity
        //
        fragColor.rgb = vec3(0.21*fragColor.r + 0.71*fragColor.g + 0.08*fragColor.b);

        // Average
        //
        //fragColor.rgb = vec3((fragColor.r + fragColor.g + fragColor.b)/3);

        // Lightness
        //
        //fragColor.rgb = vec3((max(fragColor.r, max(fragColor.g, fragColor.b))+min(fragColor.r, min(fragColor.g, fragColor.b)))/2);
    }

    // Set the depth as specified by the geometry shader
    gl_FragDepth = fDepth;

}
