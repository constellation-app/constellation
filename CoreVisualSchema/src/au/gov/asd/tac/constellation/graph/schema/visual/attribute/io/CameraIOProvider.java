/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.CameraAttributeDescription;
import au.gov.asd.tac.constellation.utilities.camera.BoundingBox;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import au.gov.asd.tac.constellation.utilities.graphics.Frame;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * A GraphIOProvider for Camera instances.
 *
 * @author algol
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public final class CameraIOProvider extends AbstractGraphIOProvider {
    
    private static final Logger LOGGER = Logger.getLogger(CameraIOProvider.class.getName());

    private static final String LOOK_AT_EYE = "look_at_eye";
    private static final String LOOK_AT_CENTRE = "look_at_centre";
    private static final String LOOK_AT_UP = "look_at_up";
    private static final String LOOK_AT_ROTATION = "look_at_rotation";
    private static final String LOOK_AT_PREVIOUS_EYE = "look_at_previous_eye";
    private static final String LOOK_AT_PREVIOUS_CENTRE = "look_at_previous_centre";
    private static final String LOOK_AT_PREVIOUS_UP = "look_at_previous_up";
    private static final String LOOK_AT_PREVIOUS_ROTATION = "look_at_previous_rotation";
    private static final String FRAME = "frame";
    private static final String BOUNDING_BOX = "bounding_box";
    private static final String VISIBILITY_LOW = "visibility_low";
    private static final String VISIBILITY_HIGH = "visibility_high";
    private static final String MIX_RATIO = "mix_ratio";

    /**
     * Get a string representing the type of data that this provider handles.
     * 
     * @return A unique name indicating the type of data handled by this
     * provider.
     */
    @Override
    public String getName() {
        return CameraAttributeDescription.ATTRIBUTE_NAME;
    }

    /**
     * Deserialise an object from a JsonNode.
     * <p>
     * Refer to base class for detailed description.
     * 
     * @param attributeId The id of the attribute being read.
     * @param elementId The id of the element being read.
     * @param jnode The JsonNode to read from.
     * @param graph The graph that the resulting object will be placed in. Provided in case
     * the object requires some graph data.
     * @param vertexMap (not used) A mapping from a vertex id in the file to the vertex id
     * in the graph.
     * @param transactionMap (not used) A mapping from a transaction id in the file to the
     * transaction id in the graph.
     * @param byteReader (not used) The byte reader containing ancillary data (e.g. images)
     * that doesn't easily fit into a JSON document.
     * @param cache (not used) a cache that can be used to dedup identical instances of the
     * same immutable objects.
     * @throws java.io.IOException If there's a problem reading the document. 
     */
    @Override
    public void readObject(int attributeId, int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, final ImmutableObjectCache cache) {
        if (!jnode.isNull()) {
            final Camera camera = new Camera();

            final Vector3f lookAtEye = getVector(jnode, LOOK_AT_EYE);
            final Vector3f lookAtCentre = getVector(jnode, LOOK_AT_CENTRE);
            final Vector3f lookAtUp = getVector(jnode, LOOK_AT_UP);
            final Vector3f lookAtRotation = getVector(jnode, LOOK_AT_ROTATION);
            final Vector3f lookAtPreviousEye = getVector(jnode, LOOK_AT_PREVIOUS_EYE);
            final Vector3f lookAtPreviousCentre = getVector(jnode, LOOK_AT_PREVIOUS_CENTRE);
            final Vector3f lookAtPreviousUp = getVector(jnode, LOOK_AT_PREVIOUS_UP);
            final Vector3f lookAtPreviousRotation = getVector(jnode, LOOK_AT_PREVIOUS_ROTATION);

            // In case the look_at objects aren't there, or they're there but aren't valid numbers...
            boolean eyeIsOk = lookAtEye != null && lookAtCentre != null && lookAtUp != null && lookAtRotation != null
                    && lookAtPreviousEye != null && lookAtPreviousCentre != null && lookAtPreviousUp != null && lookAtPreviousRotation != null;
            if (eyeIsOk) {
                eyeIsOk = lookAtEye.isValid() && lookAtCentre.isValid() && lookAtUp.isValid() && lookAtRotation.isValid()
                        && lookAtPreviousEye != null && lookAtPreviousCentre.isValid() && lookAtPreviousUp.isValid() && lookAtPreviousRotation.isValid();
            }

            if (eyeIsOk) {
                camera.lookAtEye.set(lookAtEye);
                camera.lookAtCentre.set(lookAtCentre);
                camera.lookAtUp.set(lookAtUp);
                camera.lookAtRotation.set(lookAtRotation);
                camera.lookAtPreviousEye.set(lookAtPreviousEye);
                camera.lookAtPreviousCentre.set(lookAtPreviousCentre);
                camera.lookAtPreviousUp.set(lookAtPreviousUp);
                camera.lookAtPreviousRotation.set(lookAtPreviousRotation);
                getBoundingBox(camera.boundingBox, jnode, BOUNDING_BOX);
            }

            camera.setObjectFrame(getFrame(jnode, FRAME));
            camera.setVisibilityLow((float) jnode.get(VISIBILITY_LOW).doubleValue());
            camera.setVisibilityHigh((float) jnode.get(VISIBILITY_HIGH).doubleValue());
            camera.setMixRatio((int) jnode.get(MIX_RATIO).doubleValue());

            graph.setObjectValue(attributeId, elementId, camera);
        }
    }

    /**
     * Write this object to the JSON generator.
     * <p>
     * Refer to base class for detailed description.
     * 
     * @param attr The attribute being written.
     * @param elementId The id of the element being written.
     * @param jsonGenerator The JsonGenerator used to write to the JSON document.
     * @param graph The graph that the object belongs to. Provided in case the object requires some 
     * graph data.
     * @param byteWriter (not used)  For ancillary data (e.g. images) that doesn't easily
     * fit into a JSON document.
     * @param verbose Determines whether to write default values of attributes or not.
     * @throws IOException 
     */
    @Override
    public void writeObject(final Attribute attr, final int elementId, final JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attr.getId(), elementId)) {
            final Camera camera = (Camera) graph.getObjectValue(attr.getId(), elementId);
            if (camera == null) {
                jsonGenerator.writeNullField(attr.getName());
            } else {

                jsonGenerator.writeObjectFieldStart(attr.getName());

                addVector(jsonGenerator, LOOK_AT_EYE, camera.lookAtEye);
                addVector(jsonGenerator, LOOK_AT_CENTRE, camera.lookAtCentre);
                addVector(jsonGenerator, LOOK_AT_UP, camera.lookAtUp);
                addVector(jsonGenerator, LOOK_AT_ROTATION, camera.lookAtRotation);
                addVector(jsonGenerator, LOOK_AT_PREVIOUS_EYE, camera.lookAtPreviousEye);
                addVector(jsonGenerator, LOOK_AT_PREVIOUS_CENTRE, camera.lookAtPreviousCentre);
                addVector(jsonGenerator, LOOK_AT_PREVIOUS_UP, camera.lookAtPreviousUp);
                addVector(jsonGenerator, LOOK_AT_PREVIOUS_ROTATION, camera.lookAtPreviousRotation);

                addFrame(jsonGenerator, FRAME, camera.getObjectFrame());
                addBoundingBox(jsonGenerator, BOUNDING_BOX, camera.boundingBox);
                jsonGenerator.writeNumberField(VISIBILITY_LOW, camera.getVisibilityLow());
                jsonGenerator.writeNumberField(VISIBILITY_HIGH, camera.getVisibilityHigh());
                jsonGenerator.writeNumberField(MIX_RATIO, camera.getMixRatio());

                jsonGenerator.writeEndObject();
            }
        }
    }

    /**
     * Helper method to get a Frame from a JsonNode.
     *
     * @param node The node to get the frame from.
     * @param label Label of field to extract.
     * @return Extracted frame node containing origin, forward, and up fields.
     */
    private static Frame getFrame(final JsonNode node, final String label) {
        final JsonNode frameNode = node.get(label);
        final Frame frame = new Frame();
        frame.setOrigin(getVector(frameNode, "origin"));
        frame.setForwardVector(getVector(frameNode, "forward"));
        frame.setUpVector(getVector(frameNode, "up"));

        return frame;
    }

    /**
     * Helper method to get a bounding box from a JsonNode.
     * Returns bounding box found in supplied JsonNode with supplied label. If bounding box field isn't found,
     * is invalid, or is empty the returned bounding box is set as empty.
     *
     * @param bb The bounding box to return.
     * @param node The node to get the bounding box from.
     * @param label Label of field to extract.
     * @return Extracted bounding box or an empty bounding box record if no box found.
     */
    // TODO: why pass in the object, why not just return it - which is different to other helpers.
    private static BoundingBox getBoundingBox(final BoundingBox bb, final JsonNode node, final String label) {
        final JsonNode bbNode = node.isNull() ? null : node.get(label);
        final boolean isEmpty = bbNode == null || bbNode.get("is_empty").asBoolean();
        if (isEmpty) {
            bb.setEmpty(true);
        } else {
            final Vector3f min = getVector(bbNode, "min");
            final Vector3f max = getVector(bbNode, "max");
            final Vector3f min2 = getVector(bbNode, "min2");
            final Vector3f max2 = getVector(bbNode, "max2");
            if (min != null && max != null && min2 != null && max2 != null) {
                bb.set(min, max, min2, max2);
            } else {
                bb.setEmpty(true);
            }
        }
        return bb;
    }

    /**
     * Helper method to get a Vector3f from a JsonNode.
     *
     * @param node The JsonNode to get the Vector3f from.
     * @param label The label of the Vector3f node.
     *
     * @return The extracted vector, or null if child node with supplied label is null.
     */
    private static Vector3f getVector(final JsonNode node, final String label) {
        if (node.hasNonNull(label)) {
            final ArrayNode array = (ArrayNode) node.withArray(label);

            return new Vector3f((float) array.get(0).asDouble(), (float) array.get(1).asDouble(), (float) array.get(2).asDouble());
        }

        return null;
    }

    /**
     * Add a Frame to an ObjectNode.
     *
     * @param node The ObjectNode to write the Frame to.
     * @param label The label of the Frame node.
     * @param frame The Frame to be written.
     */
    private static void addFrame(final JsonGenerator jg, final String label, final Frame frame) {
        try {
            jg.writeObjectFieldStart(label);
            final Vector3f v = new Vector3f();

            frame.getOrigin(v);
            addVector(jg, "origin", v);

            frame.getForwardVector(v);
            addVector(jg, "forward", v);

            frame.getUpVector(v);
            addVector(jg, "up", v);
            jg.writeEndObject();
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Add a GraphBoundingBox to an ObjectNode.
     *
     * @param node The ObjectNode to write the bounding box to.
     * @param label The label of the bounding box node.
     * @param bb The bounding box to be written.
     */
    private static void addBoundingBox(final JsonGenerator jg, final String label, final BoundingBox bb) {
        try {
            jg.writeObjectFieldStart(label);
            jg.writeBooleanField("is_empty", bb.isEmpty());
            if (!bb.isEmpty()) {
                addVector(jg, "min", bb.getMin());
                addVector(jg, "max", bb.getMax());
                addVector(jg, "min2", bb.getMin2());
                addVector(jg, "max2", bb.getMax2());
            }
            jg.writeEndObject();
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Add a Vector3f to an ObjectNode.
     *
     * @param node The ObjectNode to write the Vector3f to.
     * @param label The label of the Vector3f node.
     * @param v The Vector3f to be written.
     */
    private static void addVector(final JsonGenerator jg, final String label, final Vector3f v) {
        if (v != null) {
            try {
                jg.writeArrayFieldStart(label);
                jg.writeNumber(v.getX());
                jg.writeNumber(v.getY());
                jg.writeNumber(v.getZ());
                jg.writeEndArray();
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        } else {
            try {
                jg.writeNullField(label);
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }
}
