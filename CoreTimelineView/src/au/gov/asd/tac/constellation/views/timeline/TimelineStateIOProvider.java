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
package au.gov.asd.tac.constellation.views.timeline;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalUtilities;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 * A
 * <code>AbstractGraphIOProvider</code> for <code>TimelineState</code>
 * instances.
 *
 * @see AbstractGraphIOProvider
 * @see TimelineState
 *
 * @author betelgeuse
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class TimelineStateIOProvider extends AbstractGraphIOProvider {

    private static final String LTE = "lower_time_extent";
    private static final String UTE = "upper_time_extent";
    private static final String DTA = "datetime_attribute";
    private static final String HNL = "has_node_label_attributes";
    private static final String INL = "is_node_label_on";
    private static final String NLA = "node_label_attribute";
    private static final String EXN = "exclusion_state";
    private static final String SSO = "show_selected_only";
    private static final String TIME_ZONE = "time_zone";

    @Override
    public String getName() {
        return TimelineConcept.MetaAttribute.TIMELINE_STATE.getAttributeType();
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, ImmutableObjectCache cache)
            throws IOException {
        if (!jnode.isNull()) {
            final TimelineState state = new TimelineState();

            state.setLowerTimeExtent(jnode.get(LTE).asDouble());
            state.setUpperTimeExtent(jnode.get(UTE).asDouble());
            state.setDateTimeAttr(jnode.get(DTA).asText());
            if (jnode.get(HNL).asBoolean()) {
                state.setShowingNodeLabels(jnode.get(INL).asBoolean());
                state.setNodeLabelsAttr(jnode.get(NLA).asText());
            }

            if (jnode.has(EXN)) {
                state.setExclusionState(jnode.get(EXN).asInt());
            }

            if (jnode.has(SSO)) {
                state.setShowingSelectedOnly(jnode.get(SSO).asBoolean());
            }

            if (jnode.has(TIME_ZONE)) {
                state.setTimeZone(ZoneId.of(jnode.get(TIME_ZONE).asText()));
            } else {
                state.setTimeZone(TemporalUtilities.UTC);
            }

            graph.setObjectValue(attributeId, elementId, state);
        }
    }

    @Override
    public void writeObject(final Attribute attr, final int elementId, final com.fasterxml.jackson.core.JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attr.getId(), elementId)) {
            TimelineState state = (TimelineState) graph.getObjectValue(attr.getId(), elementId);
            if (state == null) {
                jsonGenerator.writeNullField(attr.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attr.getName());
                jsonGenerator.writeNumberField(LTE, state.getLowerTimeExtent());
                jsonGenerator.writeNumberField(UTE, state.getUpperTimeExtent());
                jsonGenerator.writeStringField(DTA, state.getDateTimeAttr());
                jsonGenerator.writeBooleanField(HNL, state.getNodeLabelsAttr() != null);
                if (state.getNodeLabelsAttr() != null) {
                    jsonGenerator.writeBooleanField(INL, state.isShowingNodeLabels());
                    jsonGenerator.writeStringField(NLA, state.getNodeLabelsAttr());
                }

                jsonGenerator.writeNumberField(EXN, state.getExclusionState());
                jsonGenerator.writeBooleanField(SSO, state.isShowingSelectedOnly());
                jsonGenerator.writeStringField(TIME_ZONE, state.getTimeZone().getId());

                jsonGenerator.writeEndObject();
            }

        }
    }
}
