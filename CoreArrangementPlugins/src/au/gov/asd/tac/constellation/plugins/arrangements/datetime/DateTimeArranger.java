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
package au.gov.asd.tac.constellation.plugins.arrangements.datetime;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import javafx.util.Pair;

/**
 *
 *
 * @author Quasar985
 */
public class DateTimeArranger implements Arranger {

    private boolean maintainMean = false;

    private static final int TEMP_GAP = 100;

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        final int vxCount = wg.getVertexCount();
        final int txCount = wg.getTransactionCount();
        final int linkCount = wg.getLinkCount();
        if (vxCount < 2) {
            // Graphs of size 0 or 1 are already arranged by datetime.
            return;
        }

        final float[] oldMean = maintainMean ? ArrangementUtilities.getXyzMean(wg) : null;

        // Get required attributes.
        final int xAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());

        final int nodeDateTimeAttr = wg.getAttribute(GraphElementType.VERTEX, TemporalConcept.VertexAttribute.DATETIME.getName());
        //final int txDateTimeAttr = wg.getAttribute(GraphElementType.VERTEX, TemporalConcept.TransactionAttribute.DATETIME.getName());
        final int txDateTimeAttr = TemporalConcept.TransactionAttribute.DATETIME.ensure(wg);

        if (txDateTimeAttr == GraphConstants.NOT_FOUND) {
            System.out.println("txDateTimeAttr not found!!");
            return;
        }

        // Use these if they exist.
        final int x2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X2.getName());
        final int y2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y2.getName());
        final int z2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z2.getName());
        final boolean xyz2 = x2Attr != Graph.NOT_FOUND && y2Attr != Graph.NOT_FOUND && z2Attr != Graph.NOT_FOUND;

        // Make and array of id's and their datetimes, currently from all links. Also track number of unique date times
        final Pair<Integer, Long>[] dateTimes = new Pair[linkCount];
        for (int position = 0; position < linkCount; position++) {
            
            //final int txId = wg.getTransaction(position);
            final int linkId = wg.getLink(position);
            dateTimes[position] = new Pair<>(linkId, getLowestDateTimeInLink(wg, linkId, txDateTimeAttr));
        }

        // Sort by the datetime
        Arrays.sort(dateTimes, Comparator.comparing(Pair::getValue));
        System.out.println(Arrays.toString(dateTimes));

        // Get distinct datetime values
        final long[] distinctDateTimes = Arrays.stream(dateTimes).map(Pair::getValue).distinct().mapToLong(obj -> (obj)).toArray();
        System.out.println(Arrays.toString(distinctDateTimes));

        // Number of columns is number of unique datetime values
        final int numColumns = distinctDateTimes.length;
        System.out.println("numColumns " + numColumns);

        // iterate through distinct values, and arrange all nodes with that value vertically TODO: make placemnet direction dynamic 
// above commeetn is old
        int colIndex = 0;
        int rowIndex = 0;
        for (int i = 0; i < dateTimes.length; i++) {
            final long currentDateTime = distinctDateTimes[colIndex];
            final int prevIndex = Math.max(i - 1, 0);

            // if current datetime match the previous, continue placing in current column. Otherwise start new column
            if (Objects.equals(dateTimes[i].getValue(), dateTimes[prevIndex].getValue())) {
                // Place node
                final int vId = dateTimes[i].getKey(); // wrong i think

                rowIndex++;
            } else {
                // New column
                colIndex++;
                rowIndex = 0;

                // Place node
            }

        }

        if (maintainMean) {
            ArrangementUtilities.moveMean(wg, oldMean);
        }
        /*
        final SetRadiusForArrangement radiusSetter = new SetRadiusForArrangement(wg);
        radiusSetter.setRadii();

        final float[] oldMean = maintainMean ? ArrangementUtilities.getXyzMean(wg) : null;

        // Get required attributes.
        final int xAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        final int nradiusAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.NODE_RADIUS.getName());

        // Use these if they exist.
        final int x2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X2.getName());
        final int y2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y2.getName());
        final int z2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z2.getName());
        final boolean xyz2 = x2Attr != Graph.NOT_FOUND && y2Attr != Graph.NOT_FOUND && z2Attr != Graph.NOT_FOUND;

        // Determine the circumference of the circle, given that nodes may have different radii.
        float circleCircumference = 0;
        float maxRadius = 0;
        final float sqrt2 = (float) Math.sqrt(2); // Use radius to the corners rather than the sides.
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final float nradius = (nradiusAttr != Graph.NOT_FOUND ? wg.getFloatValue(nradiusAttr, vxId) : 1) * sqrt2;
            circleCircumference += 2F * nradius;
            maxRadius = Math.max(maxRadius, nradius);
        }

        // If there's a really big node, make the circle bigger.
        circleCircumference = Math.max(circleCircumference, 2 * maxRadius * sqrt2 * (float) Math.PI);

        // Now arrange the vertices on the circumference, positioned by their fraction of
        // the space they each take up.
        final float circleRadius = circleCircumference / (2F * (float) Math.PI);
        float angle = 0;
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final float nradius = (nradiusAttr != Graph.NOT_FOUND ? wg.getFloatValue(nradiusAttr, vxId) : 1) * sqrt2;

            // What fraction of the circumference is this?
            // And therefore, what is the angle subtended?
            // (if the circumference is 0 then we divide by 1 to avoid dividing by 0 (nradius will be 0 anyway))
            final float arcfrac = (2F * nradius) / (circleCircumference != 0 ? circleCircumference : 1);
            final float arclen = 2F * (float) Math.PI * arcfrac;
            final float subtends = arclen;

            final float positionOnCircle = angle + subtends / 2F;

            // Calculate the x & y position for each vertex.
            final float x = circleRadius * (float) (Math.sin(positionOnCircle));
            final float y = circleRadius * (float) (Math.cos(positionOnCircle));
            final float z = 0;

            wg.setFloatValue(xAttr, vxId, x);
            wg.setFloatValue(yAttr, vxId, y);
            wg.setFloatValue(zAttr, vxId, z);

            if (xyz2) {
                wg.setFloatValue(x2Attr, vxId, x);
                wg.setFloatValue(y2Attr, vxId, y);
                wg.setFloatValue(z2Attr, vxId, z);
            }

            angle += subtends;
        }

        if (maintainMean) {
            ArrangementUtilities.moveMean(wg, oldMean);
        }
         */
    }

    @Override
    public void setMaintainMean(final boolean b) {
        maintainMean = b;
    }

    private long getLowestDateTimeInLink(final GraphWriteMethods wg, final int linkId, final int txDateTimeAttr) {
        final int txCount = wg.getLinkTransactionCount(linkId);
        long lowestDateTime = Long.MAX_VALUE;
        for (int i = 0; i < txCount; i++) {
            final int txId = wg.getLinkTransaction(linkId, i);
            final long dateTime = wg.getLongValue(txDateTimeAttr, txId);
            
            // TODO: check if 0 is lowest or not
            // early return, lowest possible value found
//            if (dateTime == 0) {
//                return 0L;
//            }

            if (dateTime < lowestDateTime) {
                lowestDateTime = dateTime;
            }

        }

        return lowestDateTime;
    }

}
