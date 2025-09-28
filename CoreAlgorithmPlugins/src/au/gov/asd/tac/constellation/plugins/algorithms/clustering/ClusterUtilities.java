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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.graphics.BBoxf;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author algol
 */
public class ClusterUtilities {
    
    private ClusterUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Set the color of the vertices and transactions in clusters such that each
     * cluster has a unique color.
     * <p>
     * A cluster is identified by the integer clusterId attribute; all vertices
     * will the same cluster value are in the same cluster.
     * <p>
     * The graph meta-color attributes are changed to refer to this (vertex +
     * transaction) attribute.
     *
     * @param wg The graph to modify.
     * @param clusterId The cluster attribute to work with
     * @param vxColorId The cluster vertex color attribute id.
     * @param txColorId The cluster transaction color attribute id.
     */
    public static void colorClusters(final GraphWriteMethods wg, final int clusterId, final int vxColorId, final int txColorId) {
        final int vxCount = wg.getVertexCount();

        // How many clusters are there?
        final Map<Integer, Integer> clusterMap = new HashMap<>();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final int cluster = wg.getIntValue(clusterId, vxId);
            if (!clusterMap.containsKey(cluster)) {
                clusterMap.put(cluster, clusterMap.size());
            }
        }

        final int nClusters = clusterMap.size();
        final ConstellationColor[] palette = ConstellationColor.createPalettePhi(nClusters, 0, 0.5F, 0.95F);

        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final int cluster = wg.getIntValue(clusterId, vxId);
            wg.setObjectValue(vxColorId, vxId, palette[clusterMap.get(cluster)]);
        }

        final int txVisibilityId = VisualConcept.TransactionAttribute.VISIBILITY.ensure(wg);
        final int txCount = wg.getTransactionCount();
        for (int position = 0; position < txCount; position++) {
            final int txId = wg.getTransaction(position);
            final int vxSrcId = wg.getTransactionSourceVertex(txId);
            final int vxDstId = wg.getTransactionDestinationVertex(txId);
            final int srcCluster = wg.getIntValue(clusterId, vxSrcId);
            final int dstCluster = wg.getIntValue(clusterId, vxDstId);

            final boolean isSameCluster = srcCluster == dstCluster;
            final ConstellationColor cv;
            final float visibility;
            if (isSameCluster) {
                cv = palette[clusterMap.get(srcCluster)];
                visibility = 1;
            } else {
                cv = ConstellationColor.DARK_GREY;
                visibility = 0;
            }

            wg.setObjectValue(txColorId, txId, cv);
            wg.setFloatValue(txVisibilityId, txId, visibility);
        }

        setColorRef(wg, wg.getAttributeName(vxColorId), wg.getAttributeName(txColorId));
    }

    public static void setColorRef(final GraphWriteMethods wg, final String vxColorAttrName, final String txColorAttrName) {
        final int vxColorRef = VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE.ensure(wg);
        final int txColorRef = VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE.ensure(wg);

        wg.setStringValue(vxColorRef, 0, vxColorAttrName);
        wg.setStringValue(txColorRef, 0, txColorAttrName);
    }

    /**
     * Make the graph all explodey based on cluster membership.
     * <p>
     * A cluster is identified by the integer attribute "cluster"; all vertices
     * will the same cluster value are in the same cluster.
     *
     * @param wg The graph to modify.
     * @param clusterId The cluster attribute to work with.
     */
    public static void explodeGraph(final GraphWriteMethods wg, final int clusterId) {
        final Map<Integer, BBoxf> boxes = new HashMap<>();

        final int xId = VisualConcept.VertexAttribute.X.ensure(wg);
        final int yId = VisualConcept.VertexAttribute.Y.ensure(wg);
        final int zId = VisualConcept.VertexAttribute.Z.ensure(wg);

        // Determine the bounding boxes of each cluster and the entire graph.
        final BBoxf bigBox = new BBoxf();
        final int vxCount = wg.getVertexCount();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final int cluster = wg.getIntValue(clusterId, vxId);
            if (!boxes.containsKey(cluster)) {
                final BBoxf box = new BBoxf();
                boxes.put(cluster, box);
            }

            final BBoxf box = boxes.get(cluster);
            final float x = wg.getFloatValue(xId, vxId);
            final float y = wg.getFloatValue(yId, vxId);
            final float z = wg.getFloatValue(zId, vxId);
            bigBox.add(x, y, z);
            box.add(x, y, z);
        }

        final int x2Id = VisualConcept.VertexAttribute.X2.ensure(wg);
        final int y2Id = VisualConcept.VertexAttribute.Y2.ensure(wg);
        final int z2Id = VisualConcept.VertexAttribute.Z2.ensure(wg);

        // Explode the clusters away from the centre of the graph.
        final float[] centre = bigBox.getCentre();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final int cluster = wg.getIntValue(clusterId, vxId);
            final BBoxf box = boxes.get(cluster);
            final float[] clusterCentre = box.getCentre();

            clusterCentre[BBoxf.X] -= centre[BBoxf.X];
            clusterCentre[BBoxf.Y] -= centre[BBoxf.Y];
            clusterCentre[BBoxf.Z] -= centre[BBoxf.Z];

            clusterCentre[BBoxf.X] *= 2;
            clusterCentre[BBoxf.Y] *= 2;
            clusterCentre[BBoxf.Z] *= 2;

            clusterCentre[BBoxf.X] += centre[BBoxf.X];
            clusterCentre[BBoxf.Y] += centre[BBoxf.Y];
            clusterCentre[BBoxf.Z] += centre[BBoxf.Z];

            final float x = wg.getFloatValue(xId, vxId);
            final float y = wg.getFloatValue(yId, vxId);
            final float z = wg.getFloatValue(zId, vxId);

            wg.setFloatValue(x2Id, vxId, x + clusterCentre[BBoxf.X]);
            wg.setFloatValue(y2Id, vxId, y + clusterCentre[BBoxf.Y]);
            wg.setFloatValue(z2Id, vxId, z + clusterCentre[BBoxf.Z]);
        }
    }
}
