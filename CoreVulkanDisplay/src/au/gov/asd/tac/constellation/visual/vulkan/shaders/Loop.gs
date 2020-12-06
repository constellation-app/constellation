#version 450


// === CONSTANTS ===
const float LOOP_SIZE = 0.5;


// === UNIFORMS ===
layout(std140, binding = 2) uniform UniformBlock {
    mat4 pMatrix;
    float visibilityLow;
    float visibilityHigh;
    int iconsPerRowColumn;
    int iconsPerLayer;
    int atlas2DDimension;
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
layout(location = 2) noperspective centroid out vec3 textureCoords;


void main() {
    float visibility = vpointColor[0].q;
    if(visibility > max(ub.visibilityLow, 0) && (visibility <= ub.visibilityHigh || visibility > 1.0)) {
        
        // === TEXTURE COORDS === //
        float halfPixel = 0.5 / float(ub.atlas2DDimension);
        float iconDimUVSpace = 1.0 / float(ub.iconsPerRowColumn);

/*  The shader needs to calculate texture coordinates that match the index in the texture, this
    is the Java function that places icons:
    public Vector3i IndexToTextureIndices(int index) {
        return new Vector3i(index % iconsPerRowColumn,
                            (index % iconsPerLayer) / iconsPerRowColumn,
                            index / iconsPerLayer);     
    }
*/
        int icon = gData[0].q;
        int u = icon % ub.iconsPerRowColumn;
        int v = (icon % ub.iconsPerLayer) / ub.iconsPerRowColumn;
        int w = icon / ub.iconsPerLayer;
        vec3 iconOffset = vec3(float(u) / float(ub.iconsPerRowColumn), 
                               float(v) / float(ub.iconsPerRowColumn), 
                               float(w));


        // === VERTEX COORDS === //
        vec4 vert = gl_in[0].gl_Position;                                
        float sideRadius = nradius[0];
        vert += vec4(sideRadius, sideRadius, 0, 0);

        vec4 ul = ub.pMatrix * vec4(vert.x-LOOP_SIZE, vert.y - LOOP_SIZE, vert.z, vert.w);
        vec4 ll = ub.pMatrix * vec4(vert.x-LOOP_SIZE, vert.y + LOOP_SIZE, vert.z, vert.w);
        vec4 ur = ub.pMatrix * vec4(vert.x+LOOP_SIZE, vert.y - LOOP_SIZE, vert.z, vert.w);
        vec4 lr = ub.pMatrix * vec4(vert.x+LOOP_SIZE, vert.y + LOOP_SIZE, vert.z, vert.w);


        // === VERTEX EMISSION === //

        // Top left
        pointColor = vpointColor[0];
        fData = gData[0];
        textureCoords = vec3(halfPixel, iconDimUVSpace - halfPixel, 0) + iconOffset;
        gl_Position = ul;
        gl_Position.y = -gl_Position.y;
        EmitVertex();

        // Bottom left
        pointColor = vpointColor[0];
        fData = gData[0];
        textureCoords = vec3(halfPixel, halfPixel, 0) + iconOffset;   
        gl_Position = ll;
        gl_Position.y = -gl_Position.y;
        EmitVertex();

        // Top right
        pointColor = vpointColor[0];
        fData = gData[0];
        textureCoords = vec3(iconDimUVSpace - halfPixel, iconDimUVSpace - halfPixel, 0) + iconOffset;           
        gl_Position = ur;
        gl_Position.y = -gl_Position.y;
        EmitVertex();

        // Bottom right
        pointColor = vpointColor[0];
        fData = gData[0];
        textureCoords = vec3(iconDimUVSpace - halfPixel, halfPixel, 0) + iconOffset;         
        gl_Position = lr;
        gl_Position.y = -gl_Position.y;
        EmitVertex();

        EndPrimitive();
    }
}