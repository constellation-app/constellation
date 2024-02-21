/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.interaction.animation;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Mathf;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * Fly around the graph using a succession of Catmull-Rom splines to generate a
 * smooth path between vertices.
 * <p>
 * A Catmull-Rom spline uses four control points (p0, p1, p2, p3) to generate a
 * C1 continuous spline that passes through all of the points. By using
 * successive control points (remove the first point and add a new point to the
 * end), a smooth path can be generated using the p1 &rarr; p2 segment.
 *
 * @author algol
 */
public final class FlyingAnimation extends Animation {
    
    public static final String NAME = "Fly Through Animation";
    
    private static final int STEPS_PER_LINK = 96;
    private static final int VERTICES_PER_SPLINE = 4;
    private static final int BACKTRACK_LENGTH = 10;
    
    // Keep track of recent links we've visited so we don't retrace our immediate tracks.
    private final ArrayDeque<Integer> prevLinks;
    private final SecureRandom random;
    private final ArrayDeque<Vector3f> xyzQueue;
    
    // Attribut references
    private int selectedAttr;
    private int xAttr;
    private int yAttr;
    private int zAttr;
    private int x2Attr;
    private int y2Attr;
    private int z2Attr;
    private boolean doMixing;
    
    private int stepsPerLink;
    private int step;
    
    private int cameraAttribute;
    private Camera initialPosition;
    private Camera camera;

    // The current destination vertex.
    private int currentVxId = Graph.NOT_FOUND;

    public FlyingAnimation() {
        random = new SecureRandom();

        xyzQueue = new ArrayDeque<>(VERTICES_PER_SPLINE);

        prevLinks = new ArrayDeque<>();
    }

    @Override
    public void initialise(final GraphWriteMethods wg) {
        // dont initilise the animation if there is less than 2 nodes
        if (wg.getVertexCount() <= 1) {
            stop();
        } else {
            xAttr = VisualConcept.VertexAttribute.X.get(wg);
            yAttr = VisualConcept.VertexAttribute.Y.get(wg);
            zAttr = VisualConcept.VertexAttribute.Z.get(wg);
            x2Attr = VisualConcept.VertexAttribute.X2.get(wg);
            y2Attr = VisualConcept.VertexAttribute.Y2.get(wg);
            z2Attr = VisualConcept.VertexAttribute.Z2.get(wg);
            selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
            doMixing = x2Attr != Graph.NOT_FOUND && y2Attr != Graph.NOT_FOUND && z2Attr != Graph.NOT_FOUND;
            cameraAttribute = VisualConcept.GraphAttribute.CAMERA.get(wg);
            camera = wg.getObjectValue(cameraAttribute, 0);
            initialPosition = new Camera(camera);
            stepsPerLink = STEPS_PER_LINK * (int) Math.sqrt(1 + (wg.getVertexCount() / 2000));
            
            xyzQueue.add(new Vector3f(camera.lookAtEye));
            xyzQueue.add(new Vector3f(camera.lookAtEye));
            xyzQueue.add(new Vector3f(camera.lookAtCentre));
            
            // Set up the initial four points for the spline.
            for (int i = xyzQueue.size(); i < VERTICES_PER_SPLINE; i++) {
                final Vector3f xyz = getNextVertex(wg, camera.getMix());
                xyzQueue.add(xyz);
            }
        }
    }

    @Override
    public void animate(final GraphWriteMethods wg) {
        
        // Do not animate unless there is more than 1 node
        if (wg.getVertexCount() > 1) {
            
            // Stop the animation if the camera has been changed externaly 
            if (camera != wg.getObjectValue(cameraAttribute, 0)){
                stop();
            } else {
                camera = new Camera(camera);

                if (step >= stepsPerLink) {
                    // Get the next p3 vertex.
                    final Vector3f xyz = getNextVertex(wg, camera.getMix());

                    // Remove the old p0 and add the new p3.
                    xyzQueue.removeFirst();
                    xyzQueue.addLast(xyz);

                    // The first step between p1 and p2.
                    step = 0;
                }

                // The four control points of the spline.
                final Iterator<Vector3f> it = xyzQueue.iterator();
                final float[] p0 = it.next().a;
                final float[] p1 = it.next().a;
                final float[] p2 = it.next().a;
                final float[] p3 = it.next().a;

                // Determine the new lookAt eye and center.
                final float t = step / (float) stepsPerLink;
                final float t1 = (step + 5) / (float) stepsPerLink;
                final float[] eye = new float[3];
                Mathf.catmullRom(eye, p0, p1, p2, p3, t);
                final float[] centre = new float[3];
                Mathf.catmullRom(centre, p0, p1, p2, p3, t1);

                camera.lookAtEye.set(eye[0], eye[1], eye[2]);
                camera.lookAtCentre.set(centre[0], centre[1], centre[2]);
                
                // The cameras up direction must be updated at each frame
                // This ensures the camera doesnt look in the default up direction causing perspective.
                Vector3f forward = Vector3f.subtract(camera.lookAtEye, camera.lookAtCentre);
                forward.normalize();
                
                Vector3f right = new Vector3f();
                right.crossProduct(forward, camera.lookAtUp);
                
                camera.lookAtUp.crossProduct(right, forward);
               
                wg.setObjectValue(cameraAttribute, 0, camera);
                step++;
            }
        }
    }

    @Override
    public void reset(final GraphWriteMethods wg) {
        AnimationUtilities.startAnimation(new PanAnimation("Reset View", wg.getObjectValue(cameraAttribute, 0), initialPosition, true), GraphNode.getGraph(graphID));
    }

    @Override
    public long getIntervalInMillis() {
        return 35;
    }

    private Vector3f getNextVertex(final GraphReadMethods rg, final float mix) {
        final Vector3f xyz;

        // If there is no valid graph just return a default vector
        if (rg.getVertexCount() == 0) {
            return new Vector3f(0, 0, 0);
        }

        currentVxId = getNextVertexId(rg);

        float x = rg.getFloatValue(xAttr, currentVxId);
        float y = rg.getFloatValue(yAttr, currentVxId);
        float z = rg.getFloatValue(zAttr, currentVxId);
        if (doMixing) {
            final float x2 = rg.getFloatValue(x2Attr, currentVxId);
            final float y2 = rg.getFloatValue(y2Attr, currentVxId);
            final float z2 = rg.getFloatValue(z2Attr, currentVxId);

            x = Graphics3DUtilities.mix(x, x2, mix);
            y = Graphics3DUtilities.mix(y, y2, mix);
            z = Graphics3DUtilities.mix(z, z2, mix);
        }
        xyz = new Vector3f(x, y, z);

        return xyz;
    }

    private int getNextVertexId(final GraphReadMethods rg) {
        if (currentVxId != Graph.NOT_FOUND) {
            // Go through the links connected to the current vertex in a random order.
            final int nLinks = rg.getVertexLinkCount(currentVxId);
            final int[] shuffledLinks = shuffled(nLinks);

            for (int epos = 0; epos < nLinks; epos++) {
                final int linkId = rg.getVertexLink(currentVxId, shuffledLinks[epos]);

                // Don't backtrack over a recent link.
                if (!prevLinks.contains(linkId)) {
                    int nextVxId = rg.getLinkLowVertex(linkId);
                    if (nextVxId == currentVxId) {
                        nextVxId = rg.getLinkHighVertex(linkId);
                    }

                    // Avoid loops and vertices with one link.
                    if (nextVxId != currentVxId && (rg.getVertexLinkCount(nextVxId) > 1)) {
                        // Add this to the list of recent links.
                        prevLinks.addLast(linkId);
                        if (prevLinks.size() >= BACKTRACK_LENGTH) {
                            prevLinks.removeFirst();
                        }

                        return nextVxId;
                    }
                }
            }
        } else {
            for (int position = 0; position < rg.getVertexCount(); position++) {
                final int vxId = rg.getVertex(position);

                final boolean selected = rg.getBooleanValue(selectedAttr, vxId);
                if (selected) {
                    return vxId;
                }
            }
        }

        // We've run out of planned options for the next vertex.
        // Get a random vertex that isn't the current vertex.
        int nextId = currentVxId;
        while (nextId == currentVxId) {
            final int position2 = random.nextInt(rg.getVertexCount());
            nextId = rg.getVertex(position2);
        }

        return nextId;
    }

    /**
     * An array of 0..n-1 shuffled.
     *
     * @param n
     * @return
     */
    private int[] shuffled(final int n) {
        final int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = i;
        }

        // Shuffle the array.
        for (int i = n; i > 1; i--) {
            final int ix = random.nextInt(i);
            final int t = a[i - 1];
            a[i - 1] = a[ix];
            a[ix] = t;

        }

        return a;
    }

    @Override
    protected String getName() {
        return NAME;
    }
}
