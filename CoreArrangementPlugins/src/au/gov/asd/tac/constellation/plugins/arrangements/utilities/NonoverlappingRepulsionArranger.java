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
package au.gov.asd.tac.constellation.plugins.arrangements.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Ensure that no vertices overlap.
 * <p>
 * Order the vertices by distance from the centre of the graph, then start in the middle and work outwards, pushing
 * vertices out when they overlap.
 *
 * @author algol
 */
public final class NonoverlappingRepulsionArranger implements Arranger {

    @Override
    public void arrange(final GraphWriteMethods graph) throws InterruptedException {

        final int xAttr = VisualConcept.VertexAttribute.X.get(graph);
        final int yAttr = VisualConcept.VertexAttribute.Y.get(graph);
        final int radiusAttr = VisualConcept.VertexAttribute.LABEL_RADIUS.get(graph);

        // Create an array of Blobs, determine the bounding box as we go.
        double minx = Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double maxx = -Double.MAX_VALUE;
        double maxy = -Double.MAX_VALUE;
        final int vxCount = graph.getVertexCount();
        final Blob[] blobs = new Blob[vxCount];
        int ix = 0;
        for (int position = 0; position < vxCount; position++) {
            final int vxId = graph.getVertex(position);

            final double x = graph.getFloatValue(xAttr, vxId);
            final double y = graph.getFloatValue(yAttr, vxId);
            final double radius = radiusAttr != Graph.NOT_FOUND ? graph.getFloatValue(radiusAttr, vxId) : 1;
            final Blob blob = new Blob(vxId, x, y, radius);
            blobs[ix++] = blob;

            if (x - radius < minx) {
                minx = x - radius;
            }
            if (y - radius < miny) {
                miny = y - radius;
            }
            if (x + radius > maxx) {
                maxx = x + radius;
            }
            if (y + radius > maxy) {
                maxy = y + radius;
            }
        }

        final double centreX = (minx + maxx) / 2f;
        final double centreY = (miny + maxy) / 2f;

        final Comparator<Blob> sorter = (o1, o2) -> {
            final double d1 = o1.distanceFrom(centreX, centreY);
            final double d2 = o2.distanceFrom(centreX, centreY);

            return (int) Math.signum(d1 - d2);
        };

        // Move the first blob to 0,0; move the other blobs with it.
        final double offsetX = blobs[0].x;
        final double offsetY = blobs[0].y;
        for (final Blob b : blobs) {
            b.x -= offsetX;
            b.y -= offsetY;
//                graph.setFloatValue(xAttr, b.vxId, b.x);
//                graph.setFloatValue(yAttr, b.vxId, b.y);
        }
//            graph.firePropertyChange(GraphProperties.RELOAD);

        // Sort the blobs in order of distance from the centre, nearest first.
        Arrays.sort(blobs, sorter);

        // The first blob stays at (0,0).
        // Start at the second blob and work outwards checking for overlaps.
        int remainingCandidates = 1;
        boolean checkForOverlaps = true;
        while (checkForOverlaps) {
//                Arrays.sort(blobs, sorter);
//            System.out.printf("@ANR sort %d %d\n", remainingCandidates, blobs.length);
            Arrays.sort(blobs, remainingCandidates - 1, blobs.length, sorter);
            boolean moved = false;
            for (int i = remainingCandidates; i < blobs.length; i++) {
                final Blob b = blobs[i];
//                System.out.printf("@ANR %d %s\n", i, b);
//                    final float dc = b.distanceFrom(centreX, centreY);

                // Compare this Blob with the Blobs that are closer to the centre.
                // After this loop completes, this Blob won't overlap with any closer-to-the-centre Blobs.
                //                    boolean bmoved = false;
                for (int j = i - 1; j >= 0; j--) {
                    final Blob other = blobs[j];
//                    System.out.printf("@ANR %d %s %d %s\n", i, b, j, other);
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    if (b.overlaps(other)) {
                        // Move this Blob away from the one it overlaps.
                        //                        System.out.printf("@ANR %d<>%d %s repulse from %s\n", i, j, b, other);
                        b.repulseFrom(other, centreX, centreY);

                        moved = true;
//                            bmoved = true;

//                            // Do this to watch the vertices move.
//                            //                            graph.setFloatValue(xAttr, b.vxId, b.x);
//                            graph.setFloatValue(yAttr, b.vxId, b.y);
//                            graph.firePropertyChange(GraphProperties.RELOAD);
                        // If this blob moved, it's more likely to have moved onto a nearby more distant blob.
                        // Don't bother going all the way to the centre on this iteration.
                        //                            break;
                    }
//                        else if(dc-other.distanceFrom(centreX, centreY)>b.radius+other.radius)
//                        {
//                            break;
//                        }
                }

//                    // This is the current furthest from the centre blob. If it didn't need to move,
//                    // then we'll assume that its position is now fixed. It doesn't overlap any of the
//                    // blobs nearer to the centre, and any blobs yet to be checked will move if there
//                    // are any overlaps with this one.
//                    //                    if(bmoved && i==remainingCandidates)
//                    {
//                        remainingCandidates++;
////                        moved = false;
//                    }
            }

            // This is the current furthest from the centre blob. If it didn't need to move,
            // then we'll assume that its position is now fixed. It doesn't overlap any of the
            // blobs nearer to the centre, and any blobs yet to be checked will move if there
            // are any overlaps with this one.
            //                if(!moved)
//                {
//                    remainingCandidates++;
//                }
            remainingCandidates++;

//            System.out.println("remainingCandiates:" + remainingCandidates);
            // If this blob moved, do another iteration to see if it moved onto another blob.
            checkForOverlaps = moved;
        }

        // Copy the blob positions back into the graph.
        for (final Blob b : blobs) {
            graph.setFloatValue(xAttr, b.vxId, (float) b.x);
            graph.setFloatValue(yAttr, b.vxId, (float) b.y);
        }
    }

    @Override
    public void setMaintainMean(final boolean b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * A convenient representation of a vertex.
     */
    private static class Blob {

        private static final float MIN_DISTANCE = 0.01f;
        private final int vxId;
        private double x;
        private double y;
        private final double radius;

        /**
         * Construct a new Blob.
         *
         * @param vxId The vertex corresponding to this Blob.
         * @param x The x position of the vertex.
         * @param y The y position of the vertex.
         * @param radius The radius of the vertex.
         */
        public Blob(final int vxId, final double x, final double y, final double radius) {
            this.vxId = vxId;
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        /**
         * Return the vertex id.
         *
         * @return The vertex id.
         */
        public int getVxId() {
            return vxId;
        }

        /**
         * Return the x position.
         *
         * @return The x position.
         */
        public double getX() {
            return x;
        }

        /**
         * Return the y position.
         *
         * @return The y position.
         */
        public double getY() {
            return y;
        }

        /**
         * How far is the centre of this blob from (ox, oy)?
         *
         * @param ox Other x.
         * @param oy Other y.
         *
         * @return The distance from the other x,y.
         */
        double distanceFrom(final double ox, final double oy) {
            return Math.hypot(ox - x, oy - y);
        }

        /**
         * Does this Blob overlap the other Blob?
         *
         * @param other The other Blob.
         *
         * @return True if this Blob and the other Blob overlap, false otherwise.
         */
        boolean overlaps(final Blob other) {
            return distanceFrom(other.x, other.y) < radius + other.radius;
        }

        /**
         * Repulse this blob from the other blob.
         * <p>
         * This blob will move itself away from the other blob such that the centres are radius+other.radius apart, in
         * the same direction that the centre of this blob is relative to the centre of the other blob.
         * <p>
         * If the blobs are too close to determine a direction, this blob will move away from the centre instead.
         * <p>
         * If the blob is exactly on the centre, just move it.
         *
         * @param other The other Blob.
         * @param centreX The x position of the centre.
         * @param centreY The y position of the centre.
         */
        void repulseFrom(final Blob other, final double centreX, final double centreY) {
            final double d = distanceFrom(other.x, other.y);
            final double r = radius + other.radius;
            if (d > MIN_DISTANCE) {
                x += (x - other.x) * r / d;
                y += (y - other.y) * r / d;
            } else if (x != centreX && y != centreY) {
//                Random rand = new Random();
//                float angle = (rand.nextFloat() * (float) Math.PI) - ((float) Math.PI/2);
//                float cosangle = (float) Math.cos(angle);
//                float sinangle = (float) Math.sin(angle);
//                float xdist = (x-centreX)*cosangle - (y-centreY)*sinangle;
//                float ydist = (y-centreY)*cosangle + (x-centreX)*sinangle;
                final double d2 = distanceFrom(centreX, centreY);
                x += (x - centreX) * r / d2;
                y += (y - centreY) * r / d2;
//                x += xdist*r/d2;
//                y += ydist*r/d2;
            } else {
                y += r;
            }
        }

        @Override
        public String toString() {
            return String.format("Blob[%d;x=%d,y=%d,r=%d]", vxId, x, y, radius);
        }
    }
}
