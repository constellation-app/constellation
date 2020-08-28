// Icon geometry shader.
// Icons that are marked with the hidden indicator (color.a<0) are not emitted.
//
// The [gf]DisplayColor passes the highlight color through to the fragment shader.

#version 450


// === CONSTANTS ===
const int ICON_BITS = 16;
const int ICON_MASK = 0xffff;
const int SELECTED_MASK = 1;
const int DIM_MASK = 2;
const int HIGHLIGHT_ICON = 0;
const int TRANSPARENT_ICON = 5;

// Magic numbers, what could go wrong...
const mat4 DIM_MATRIX = 0.5 * mat4(
    0.21, 0.21, 0.21, 0.00,
    0.71, 0.71, 0.71, 0.00,
    0.08, 0.08, 0.08, 0.00,
    0.00, 0.00, 0.00, 1.00
);
const mat4 IDENTITY_MATRIX = mat4(1.0);


// === UNIFORMS ===
layout(std140, binding = 2) uniform UniformBlock {
    int iconsPerRowColumn;
    int iconsPerLayer;
    int atlas2DDimension;
    int drawHitTest;
    float pixelDensity;
    mat4 highlightColor;
    mat4 pMatrix;            
} ub;
layout(binding = 3) uniform isamplerBuffer flags;


// === PER PRIMITIVE DATA IN ===
layout(points) in;
layout(location = 0) flat in ivec4 gData[];
layout(location = 1) in mat4 gBackgroundIconColor[];
layout(location = 5) flat in int vxPosition[];

// The radius of the vertex
layout(location = 6) flat in float gRadius[];


// === PER PRIMITIVE DATA OUT ===
layout(triangle_strip, max_vertices=28) out;
layout(location = 0) flat out mat4 iconColor;
layout(location = 4) noperspective centroid out vec3 textureCoords;
layout(location = 5) flat out vec4 hitBufferValue;


// === FILE SCOPE VARS ===
vec4 vert;
vec4 hbv;
float iconDimUVSpace; //the width and height of an icon in UV space, 0.0-1.0
float halfPixel;


// NOTE: this shader has been modified for Vulkan.  The uniforms need to be explicitly
// bound to slots and locations that match descriptors defined in the CVKIconsRenderable
// class.  Additionally the Y goes down the screen in Vulkan as by default it uses a 
// right handed coordinate system (take your index finger as Z pointing away from you, your
// long finger is x pointing to the right, which leaves your thumb as y pointing down).
// OpenGL uses a left hand system with Y going up.  That means to convert a GL shader for 
// Vulkan we need to modify the Y and the V texture coordinate.  For this shader we take the
// negative y when processing the graph access vertices.  Texture coordinates don't care about
// the LHS or RHS used by the vertices.
void drawIcon(float x, float y, float radius, int icon, mat4 color) {

    if (icon != TRANSPARENT_ICON) {

/*  The shader needs to calculate texture coordinates that match the index in the texture, this
    is the Java function that places icons:
    public Vector3i IndexToTextureIndices(int index) {
        return new Vector3i(index % iconsPerRowColumn,
                            (index % iconsPerLayer) / iconsPerRowColumn,
                            index / iconsPerLayer);     
    }
*/
        int u = icon % ub.iconsPerRowColumn;
        int v = (icon % ub.iconsPerLayer) / ub.iconsPerRowColumn;
        int w = icon / ub.iconsPerLayer;
        vec3 iconOffset = vec3(float(u) / float(ub.iconsPerRowColumn), 
                               float(v) / float(ub.iconsPerRowColumn), 
                               float(w));

        // Bottom Left
        gl_Position = ub.pMatrix * vec4(vert.x + x, vert.y + y + radius, vert.z, vert.w);  
        gl_Position.y = -gl_Position.y;
        iconColor = color;
        hitBufferValue = hbv;
        textureCoords = vec3(halfPixel, halfPixel, 0) + iconOffset;
        EmitVertex();

        // Top Left
        gl_Position = ub.pMatrix * vec4(vert.x + x, vert.y + y, vert.z, vert.w);
        gl_Position.y = -gl_Position.y;
        iconColor = color;
        hitBufferValue = hbv;
        textureCoords = vec3(halfPixel, iconDimUVSpace - halfPixel, 0) + iconOffset;        
        EmitVertex();

        // Bottom Right      
        gl_Position = ub.pMatrix * vec4(vert.x + x + radius, vert.y + y + radius, vert.z, vert.w);
        gl_Position.y = -gl_Position.y;
        iconColor = color;
        hitBufferValue = hbv;
        textureCoords = vec3(iconDimUVSpace - halfPixel, halfPixel, 0) + iconOffset;        
        EmitVertex();

        // Top Right
        gl_Position = ub.pMatrix * vec4(vert.x + x + radius, vert.y + y, vert.z, vert.w);  
        gl_Position.y = -gl_Position.y;      
        iconColor = color;
        hitBufferValue = hbv;
        textureCoords = vec3(iconDimUVSpace - halfPixel, iconDimUVSpace - halfPixel, 0) + iconOffset;     
        EmitVertex();

        EndPrimitive();
    }
}

void main() {
    // The atlas texture size can change as more icons are added so we need to do a little calculation
    halfPixel = 0.5 / float(ub.atlas2DDimension);
    iconDimUVSpace = 1.0 / float(ub.iconsPerRowColumn);

    // Nodes are explicitly not drawn if they have visibility <= 0.
    // See au.gov.asd.tac.constellation.visual.opengl.task.NodeHider.java.

    //TT: vertex shader will encode this to either 1 if visible or -1 otherwise
    float sideRadius = gRadius[0];
    if (sideRadius > 0) {

        // Get the position of the vertex
        vert = gl_in[0].gl_Position;

        // Calculate the hit buffer value
        hbv = vec4(gData[0][3] + 1, 0, 0, 1);

        // Get the flags associated with this vertex
        // selected, dimmed etc.
        int fd = texelFetch(flags, vxPosition[0]).s;

        // If the vertex is selected then move it slightly forward so
        // that it is drawn in front of unselected vertices.
        if((fd & SELECTED_MASK) != 0) {
            // Set a minimum size for selected vertices.
            float ddist = 800;
            if(vert.z < -ddist) {
                sideRadius *= 1 - ((ddist+vert.z)/ddist);
            }

            vert.z += 0.001;
        }

        int bgIcon = (gData[0][0] >> ICON_BITS) & ICON_MASK;

        float iconPixelRadius = sideRadius * ub.pixelDensity / -vert.z;
        if (iconPixelRadius < 1 && bgIcon != TRANSPARENT_ICON) {
            mat4 backgroundIconColor = gBackgroundIconColor[0];
            backgroundIconColor[3][3] = max(smoothstep(0.0, 1.0, iconPixelRadius), 0.7);
            if ((fd & DIM_MASK) != 0) {
                backgroundIconColor = DIM_MATRIX * backgroundIconColor;
            }
            drawIcon(-sideRadius, -sideRadius, 2 * sideRadius, bgIcon, backgroundIconColor);

        } else {

            // Draw the background icon
            mat4 iconColor = gBackgroundIconColor[0];
            if ((fd & DIM_MASK) != 0) {
                iconColor = DIM_MATRIX * iconColor;
            }
            drawIcon(-sideRadius, -sideRadius, 2 * sideRadius, bgIcon, iconColor);

            // Draw the foreground icon
            int fgIcon = gData[0][0] & ICON_MASK;
            if ((fd & DIM_MASK) != 0) {
                iconColor = DIM_MATRIX;
            } else {
                iconColor = IDENTITY_MATRIX;
            }
            if (bgIcon != TRANSPARENT_ICON) {
                iconColor[3][3] = smoothstep(1.0, 5.0, iconPixelRadius);
                drawIcon(-sideRadius, -sideRadius, 2 * sideRadius, fgIcon, iconColor);
            } else {
                drawIcon(-sideRadius, -sideRadius, 2 * sideRadius, fgIcon, iconColor);
                iconColor[3][3] = smoothstep(1.0, 5.0, iconPixelRadius);
            }

            // Draw the decorators
            float decoratorRadius = sideRadius * 2/3;
            float decoratorOffset = sideRadius / 3;
            drawIcon(-sideRadius, decoratorOffset, decoratorRadius, gData[0][1] & ICON_MASK, iconColor);
            drawIcon(-sideRadius, -sideRadius, decoratorRadius, (gData[0][1] >> ICON_BITS) & ICON_MASK, iconColor);
            drawIcon(decoratorOffset, -sideRadius, decoratorRadius, gData[0][2] & ICON_MASK, iconColor);
            drawIcon(decoratorOffset, decoratorOffset, decoratorRadius, (gData[0][2] >> ICON_BITS) & ICON_MASK, iconColor);
        }

        // If the vertex is selected then draw the highlight icon
        if ((ub.drawHitTest == 0) && (fd & SELECTED_MASK) != 0) {
            drawIcon(-sideRadius, -sideRadius, 2 * sideRadius, HIGHLIGHT_ICON, ub.highlightColor);
        }
    }
}