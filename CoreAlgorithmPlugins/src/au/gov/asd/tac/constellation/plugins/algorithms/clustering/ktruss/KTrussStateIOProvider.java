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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.ktruss;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.KTrussStateAttributeDescription;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class KTrussStateIOProvider extends AbstractGraphIOProvider {

    @Override
    public String getName() {
        return KTrussStateAttributeDescription.ATTRIBUTE_NAME;
    }
    private static final String VERSION = "version";
    private static final String MODCOUNT = "modificationCounter";
    private static final String STRUCMODCOUNT = "strucModificationCount";
    private static final String CURRENTK = "currentK";
    private static final String HIGHESTK = "highestK";
    private static final String DIMMED = "excludedElementsDimmed";
    private static final String TOGGLES = "displayOptionToggles";
    private static final String EXTANT_SIZE = "extantKTrusses_size";
    private static final String EXTANT = "extantKTrusses";
    private static final String TRUSSTOINDEX_SIZE = "kTrussToIndex_size";
    private static final String TRUSSTOINDEX = "kTrussToIndex";
    private static final String INDEXTOTRUSS_SIZE = "indexToKTruss_size";
    private static final String INDEXTOTRUSS = "indexToKTruss";
    private static final String NODETOCOMP = "nodeToComponent";
    private static final String LINKTOCOMP = "linkToComponent";
    private static final String COMPTREE = "componentTree";
    private static final String COMPSIZES = "componentSizes";
    private static final String NESTEDVIS = "isNestedTrussesVisible";
    private static final String HIGHCOMP = "highestComponentNum";
    private static final String TOTALVERTS = "totalVerts";
    private static final String TOTALTRUSSVERTS = "totalVertsInTrusses";
    private static final String DRAWALL = "drawAllComponents";
    private static final String NESTEDCOLORED = "nestedTrussesColored";
    private static final String INTERACTIVE = "interactive";

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, final ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull()) {

            final int version = jnode.has(VERSION) ? jnode.get(VERSION).asInt() : 0;

            final long modificationCounter = jnode.get(MODCOUNT).asLong();
            final long strucModificationCount = jnode.get(STRUCMODCOUNT).asLong();
            final int currentK = jnode.get(CURRENTK).asInt();
            final int highestK = jnode.get(HIGHESTK).asInt();

            final boolean excludedElementsDimmed = jnode.get(DIMMED).asBoolean();
            final int displayOptionToggles = jnode.get(TOGGLES).asInt();
            final boolean[] extantKTrusses = new boolean[jnode.get(EXTANT_SIZE).asInt()];
            Iterator<JsonNode> iter = jnode.get(EXTANT).iterator();
            int index = 0;
            while (iter.hasNext()) {
                extantKTrusses[index++] = iter.next().asBoolean();
            }

            final int[] kTrussToIndex = new int[jnode.get(TRUSSTOINDEX_SIZE).asInt()];
            iter = jnode.get(TRUSSTOINDEX).iterator();
            index = 0;
            while (iter.hasNext()) {
                kTrussToIndex[index++] = iter.next().asInt();
            }

            final int[] indexToKTruss = new int[jnode.get(INDEXTOTRUSS_SIZE).asInt()];
            iter = jnode.get(INDEXTOTRUSS).iterator();
            index = 0;
            while (iter.hasNext()) {
                indexToKTruss[index++] = iter.next().asInt();
            }

            final Map<Integer, Integer> nodeToComponent = new HashMap<>();
            iter = jnode.get(NODETOCOMP).iterator();
            while (iter.hasNext()) {
                final int vxID = iter.next().asInt();
                final int component = iter.next().asInt();
                nodeToComponent.put(vertexMap.get(vxID), component);
            }

            final Map<Integer, Integer> linkToComponent = new HashMap<>();
            iter = jnode.get(LINKTOCOMP).iterator();
            while (iter.hasNext()) {
                final int lxID;
                if (version >= 2) {
                    lxID = graph.getTransactionLink(transactionMap.get(iter.next().asInt()));
                } else {
                    lxID = iter.next().asInt();
                }
                final int component = iter.next().asInt();
                linkToComponent.put(lxID, component);
            }

            final Map<Integer, Integer> componentTree = new HashMap<>();
            iter = jnode.get(COMPTREE).iterator();
            while (iter.hasNext()) {
                componentTree.put(iter.next().asInt(), iter.next().asInt());
            }

            final Map<Integer, Integer> componentSizes = new HashMap<>();
            iter = jnode.get(COMPSIZES).iterator();
            while (iter.hasNext()) {
                componentSizes.put(iter.next().asInt(), iter.next().asInt());
            }

            final boolean isNestedTrussesVisible = jnode.get(NESTEDVIS).asBoolean();
            final int highestComponentNum = jnode.get(HIGHCOMP).asInt();
            final int totalVerts = jnode.get(TOTALVERTS).asInt();
            final int totalVertsInTrusses = jnode.get(TOTALTRUSSVERTS).asInt();
            final boolean drawAllComponents = jnode.get(DRAWALL).asBoolean();
            final boolean nestedTrussesColored = jnode.get(NESTEDCOLORED).asBoolean();
            boolean interactive = true;
            if (jnode.has(INTERACTIVE)) {
                interactive = jnode.get(INTERACTIVE).asBoolean();
            }

            final KTrussState state = new KTrussState(modificationCounter, strucModificationCount, currentK, highestK, excludedElementsDimmed, displayOptionToggles, extantKTrusses, kTrussToIndex, indexToKTruss, nodeToComponent, linkToComponent, componentTree, componentSizes, isNestedTrussesVisible, highestComponentNum, totalVerts, totalVertsInTrusses, drawAllComponents, nestedTrussesColored, interactive);

            graph.setObjectValue(attributeId, elementId, state);
        }
    }

    @Override
    public void writeObject(final Attribute attr, final int elementId, final JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attr.getId(), elementId)) {
            final KTrussState state = (KTrussState) graph.getObjectValue(attr.getId(), elementId);
            if (state == null) {
                jsonGenerator.writeNullField(attr.getName());
            } else {

                jsonGenerator.writeNumberField(VERSION, 2);

                jsonGenerator.writeObjectFieldStart(attr.getName());
                jsonGenerator.writeNumberField(MODCOUNT, state.modificationCounter);
                jsonGenerator.writeNumberField(STRUCMODCOUNT, state.strucModificationCount);
                jsonGenerator.writeNumberField(CURRENTK, state.getCurrentK());
                jsonGenerator.writeNumberField(HIGHESTK, state.getHighestK());
                jsonGenerator.writeBooleanField(DIMMED, state.isExcludedElementsDimmed());
                jsonGenerator.writeNumberField(TOGGLES, state.getDisplayOptionToggles());

                final boolean[] extantKTrusses = state.getExtantKTrusses();
                jsonGenerator.writeNumberField(EXTANT_SIZE, extantKTrusses.length);
                jsonGenerator.writeArrayFieldStart(EXTANT);
                for (int i = 0; i < extantKTrusses.length; i++) {
                    jsonGenerator.writeBoolean(extantKTrusses[i]);
                }
                jsonGenerator.writeEndArray();

                final int[] kTrussToIndex = state.getkTrussToIndex();
                jsonGenerator.writeNumberField(TRUSSTOINDEX_SIZE, kTrussToIndex.length);
                jsonGenerator.writeArrayFieldStart(TRUSSTOINDEX);
                for (int i = 0; i < kTrussToIndex.length; i++) {
                    jsonGenerator.writeNumber(kTrussToIndex[i]);
                }
                jsonGenerator.writeEndArray();

                final int[] indexToKTruss = state.getIndexToKTruss();
                jsonGenerator.writeNumberField(INDEXTOTRUSS_SIZE, indexToKTruss.length);
                jsonGenerator.writeArrayFieldStart(INDEXTOTRUSS);
                for (int i = 0; i < indexToKTruss.length; i++) {
                    jsonGenerator.writeNumber(indexToKTruss[i]);
                }
                jsonGenerator.writeEndArray();

                Iterator<Map.Entry<Integer, Integer>> iter = state.getNodeToComponent().entrySet().iterator();
                jsonGenerator.writeArrayFieldStart(NODETOCOMP);
                while (iter.hasNext()) {
                    final Map.Entry<Integer, Integer> entry = iter.next();
                    final int vxID = entry.getKey();
                    if (graph.vertexExists(vxID)) {
                        jsonGenerator.writeNumber(vxID);
                        jsonGenerator.writeNumber(entry.getValue());
                    }
                }
                jsonGenerator.writeEndArray();

                iter = state.getLinkToComponent().entrySet().iterator();
                jsonGenerator.writeArrayFieldStart(LINKTOCOMP);
                while (iter.hasNext()) {
                    final Map.Entry<Integer, Integer> entry = iter.next();
                    final int lxID = entry.getKey();
                    if (graph.linkExists(lxID)) {
                        final int txID = graph.getLinkTransaction(lxID, 0);
                        jsonGenerator.writeNumber(txID);
                        jsonGenerator.writeNumber(entry.getValue());
                    }
                }
                jsonGenerator.writeEndArray();

                iter = state.getComponentTree().entrySet().iterator();
                jsonGenerator.writeArrayFieldStart(COMPTREE);
                while (iter.hasNext()) {
                    final Map.Entry<Integer, Integer> entry = iter.next();
                    jsonGenerator.writeNumber(entry.getKey());
                    jsonGenerator.writeNumber(entry.getValue());
                }
                jsonGenerator.writeEndArray();

                iter = state.getComponentSizes().entrySet().iterator();
                jsonGenerator.writeArrayFieldStart(COMPSIZES);
                while (iter.hasNext()) {
                    final Map.Entry<Integer, Integer> entry = iter.next();
                    jsonGenerator.writeNumber(entry.getKey());
                    jsonGenerator.writeNumber(entry.getValue());
                }
                jsonGenerator.writeEndArray();

                jsonGenerator.writeBooleanField(NESTEDVIS, state.isNestedTrussesVisible());
                jsonGenerator.writeNumberField(HIGHCOMP, state.getHighestComponentNum());
                jsonGenerator.writeNumberField(TOTALVERTS, state.getTotalVerts());
                jsonGenerator.writeNumberField(TOTALTRUSSVERTS, state.getTotalVertsInTrusses());
                jsonGenerator.writeBooleanField(DRAWALL, state.isDrawAllComponents());
                jsonGenerator.writeBooleanField(NESTEDCOLORED, state.isNestedTrussesColored());
                jsonGenerator.writeBooleanField(INTERACTIVE, state.isInteractive());
                jsonGenerator.writeEndObject();
            }
        }
    }
}
