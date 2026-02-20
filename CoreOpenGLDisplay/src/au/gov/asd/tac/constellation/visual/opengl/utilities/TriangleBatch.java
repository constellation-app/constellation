/*
 * Copyright 2010-2025 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.visual.opengl.utilities;

import au.gov.asd.tac.constellation.utilities.graphics.Mathf;
import au.gov.asd.tac.constellation.utilities.graphics.Vector2f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES3;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * A batch that specializes in triangles.
 *
 * @author algol
 */
public class TriangleBatch {

    private static final int VERTEX_DATA = 0;
    private static final int NORMAL_DATA = 1;
    private static final int TEXTURE_DATA = 2;
    private static final int INDEX_DATA = 3;
    // The arrays of VectorNx might be more efficient as plain arrays, rather than
    // converting them when necessary, but its more obvious this way.
    // Change later if necessary.
    private short[] allIndexes;        // Array of indexes
    private Vector3f[] allVerts;        // Array of vertices
    private Vector3f[] allNorms;        // Array of normals
    private Vector2f[] allTexCoords;    // Array of texture coordinates
    int nMaxIndexes;         // Maximum workspace
    int nNumIndexes;         // Number of indexes currently used
    int nNumVerts;           // Number of vertices actually used
    int[] bufferObjects;
    int[] vertexArrayBufferObject;

    public TriangleBatch() {
        allIndexes = null;
        allVerts = null;
        allNorms = null;
        allTexCoords = null;

        nMaxIndexes = 0;
        nNumIndexes = 0;
        nNumVerts = 0;
        bufferObjects = new int[4];
        vertexArrayBufferObject = new int[1];
    }

    public void dispose(final GL2ES3 gl) {
        gl.glDeleteBuffers(4, bufferObjects, 0);
        gl.glDeleteVertexArrays(1, vertexArrayBufferObject, 0);
    }

    public int getIndexCount() {
        return nNumIndexes;
    }

    public int getVertexCount() {
        return nNumVerts;
    }

    /**
     * Start assembling a mesh.
     * <p>
     * You need to specify a maximum amount of indexes that you expect. The
     * end() will clean up any unneeded memory.
     *
     * @param maxVerts the maximum number of vertices to include in the mesh.
     */
    public void beginMesh(final int maxVerts) {
        nMaxIndexes = maxVerts;
        nNumIndexes = 0;
        nNumVerts = 0;

        // Allocate new blocks. In reality, the other arrays will be
        // much shorter than the index array.
        allIndexes = new short[nMaxIndexes];
        allVerts = Vector3f.createArray(nMaxIndexes);
        allNorms = Vector3f.createArray(nMaxIndexes);
        allTexCoords = Vector2f.createArray(nMaxIndexes);
    }

    /**
     * Add a triangle to the mesh.
     * <p>
     * This searches the current list for identical (well, almost identical -
     * these are floats) vertices. If one is found, it is added to the index
     * array. If not, it is added to both the index array and the vertex array
     * grows by one as well.
     *
     * @param verts the vectors representing the locations of the vertices.
     * @param norms the vectors representing the normals of the vertices.
     * @param texCoords the vectors representing the texture coordinates of the
     * vertices.
     */
    public void addTriangle(final Vector3f[] verts, final Vector3f[] norms, final Vector2f[] texCoords) {
        final float e = 0.00001F; // How small a difference to equate.

        // First thing we do is make sure the normals are unit length!
        // It's almost always a good idea to work with pre-normalized normals.
        norms[0].normalize();
        norms[1].normalize();
        norms[2].normalize();

        // Search for match - triangle consists of three vertices.
        for (int iVertex = 0; iVertex < 3; iVertex++) {
            short iMatch = 0;
            for (iMatch = 0; iMatch < nNumVerts; iMatch++) {
                // If the vertex positions are the same...
                if (Mathf.closeEnough(allVerts[iMatch].a[0], verts[iVertex].a[0], e)
                        && Mathf.closeEnough(allVerts[iMatch].a[1], verts[iVertex].a[1], e)
                        && Mathf.closeEnough(allVerts[iMatch].a[2], verts[iVertex].a[2], e)
                        && // and the Normal is the same...
                        Mathf.closeEnough(allNorms[iMatch].a[0], norms[iVertex].a[0], e)
                        && Mathf.closeEnough(allNorms[iMatch].a[1], norms[iVertex].a[1], e)
                        && Mathf.closeEnough(allNorms[iMatch].a[2], norms[iVertex].a[2], e)
                        && // and Texture is the same...
                        Mathf.closeEnough(allTexCoords[iMatch].a[0], texCoords[iVertex].a[0], e)
                        && Mathf.closeEnough(allTexCoords[iMatch].a[1], texCoords[iVertex].a[1], e)) {
                    // Then add the index only.
                    allIndexes[nNumIndexes] = iMatch;
                    nNumIndexes++;
                    break;
                }
            }

            // No match for this vertex, add to end of list.
            if (iMatch == nNumVerts && nNumVerts < nMaxIndexes && nNumIndexes < nMaxIndexes) {
                allVerts[nNumVerts].set(verts[iVertex]);
                allNorms[nNumVerts].set(norms[iVertex]);
                allTexCoords[nNumVerts].set(texCoords[iVertex]);
                allIndexes[nNumIndexes] = (short) nNumVerts;
                nNumIndexes++;
                nNumVerts++;
            }
        }
    }

    /**
     * We've finished providing data; compact the data, map the buffers, and set
     * up the vertex array.
     * <p>
     * You should really save the results of the indexing for future use if the
     * model data is static (doesn't change).
     *
     * @param gl the current OpenGL context.
     */
    public void end(final GL2ES3 gl) {
        // Create the master vertex array object.
        gl.glGenVertexArrays(1, vertexArrayBufferObject, 0);
        gl.glBindVertexArray(vertexArrayBufferObject[0]);

        // Create the buffer objects.
        gl.glGenBuffers(4, bufferObjects, 0);

        // Copy data to video memory.
        FloatBuffer buf;

        // Vertex data.
        buf = FloatBuffer.wrap(GLTools.toFloatArray(allVerts));
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferObjects[VERTEX_DATA]);
        gl.glEnableVertexAttribArray(ShaderManager.ATTRIBUTE_VERTEX);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 3L * nNumVerts, buf, GL.GL_STATIC_DRAW);
        gl.glVertexAttribPointer(ShaderManager.ATTRIBUTE_VERTEX, 3, GL.GL_FLOAT, false, 0, 0);

        // Normal data.
        buf = FloatBuffer.wrap(GLTools.toFloatArray(allNorms));
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferObjects[NORMAL_DATA]);
        gl.glEnableVertexAttribArray(ShaderManager.ATTRIBUTE_NORMAL);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * 3L * nNumVerts, buf, GL.GL_STATIC_DRAW);
        gl.glVertexAttribPointer(ShaderManager.ATTRIBUTE_NORMAL, 3, GL.GL_FLOAT, false, 0, 0);

        // Texture coordinates.
        buf = FloatBuffer.wrap(GLTools.toFloatArray(allTexCoords));
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferObjects[TEXTURE_DATA]);
        gl.glEnableVertexAttribArray(ShaderManager.ATTRIBUTE_TEXTURE0);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * nNumVerts * 2L, buf, GL.GL_STATIC_DRAW);
        gl.glVertexAttribPointer(ShaderManager.ATTRIBUTE_TEXTURE0, 2, GL.GL_FLOAT, false, 0, 0);

        // Indexes.
        ShortBuffer shortBuf = ShortBuffer.wrap(allIndexes);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, bufferObjects[INDEX_DATA]);
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, (long) Buffers.SIZEOF_SHORT * nNumIndexes, shortBuf, GL.GL_STATIC_DRAW);

        // Done
        gl.glBindVertexArray(0);

        // Free older, larger arrays.
        // Reassign pointers so they are marked as unused.
        allIndexes = null;
        allVerts = null;
        allNorms = null;
        allTexCoords = null;
    }

    /**
     * Draw the triangles.
     * <p>
     * Make sure you call glEnableClientState for these arrays.
     *
     * @param gl the current OpenGL context.
     */
    public void draw(final GL2ES3 gl) {
        gl.glBindVertexArray(vertexArrayBufferObject[0]);

        gl.glDrawElements(GL.GL_TRIANGLES, nNumIndexes, GL.GL_UNSIGNED_SHORT, 0);

        // Unbind to anybody
        // Should this be just plain 0?
        gl.glBindVertexArray(0);
    }
}
