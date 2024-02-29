/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.wordcloud.ui;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.openide.util.lookup.ServiceProvider;
/**
 * IOProvider to read and write the WordCloud graph attribute to and from file.
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class WordCloudIOProvider extends AbstractGraphIOProvider {

    private static final String WORDTOHASH = "wordsToHashes";
    private static final String WORDSETS = "hashesWordSets";
    private static final String ELTYPE = "elementType";
    private static final String WORDLIST = "wordListWithSizes";
    private static final String WORDSIGNIFICANCES = "wordSignificances";
    private static final String CURRENTSIGNIFICANCE = "currentSignificance";
    private static final String INFO = "queryInfoString";
    private static final String MODCOUNT = "modCount";
    private static final String SELECTED = "selectedWords";
    private static final String ISUNION = "isUnionSelect";
    private static final String ISSIZE = "isSizeSorted";
    private static final String VERSION = "version";

    @Override
    public String getName() {
        return WordCloud.WORD_CLOUD_ATTR;
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, final ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull()) {
            final int version = jnode.has(VERSION) ? jnode.get(VERSION).asInt() : 0;

            Iterator<JsonNode> iter = jnode.get(WORDTOHASH).iterator();
            final Map<String, Integer> wordsToHashes = new HashMap<>();
            while (iter.hasNext()) {
                wordsToHashes.put(iter.next().asText(), iter.next().asInt());
            }

            final GraphElementType elementType = GraphElementType.getValue(jnode.get(ELTYPE).asText());
            final Map<Integer, Set<Integer>> hashedWordSets = new HashMap<>();
            iter = jnode.get(WORDSETS).iterator();
            while (iter.hasNext()) {
                final int hash = iter.next().asInt();
                final int setSize = iter.next().asInt();
                final Set<Integer> wordSet = new HashSet<>();
                for (int i = 0; i < setSize; i++) {
                    final int elID = iter.next().asInt();                        
                    final int elementID = (elementType == GraphElementType.VERTEX) ? vertexMap.get(elID): transactionMap.get(elID);
                    wordSet.add(elementID);
                }
                hashedWordSets.put(hash, wordSet);
            }

            final SortedMap<String, Float> wordListWithSizes;

            iter = jnode.get(WORDLIST).iterator();
            wordListWithSizes = new TreeMap<>();
            while (iter.hasNext()) {
                final String word = iter.next().asText();
                // note this little hacky bit is a little horrid but is required as sizes were once saved as actual font sizes rather than relative frequencies between 0 and 1 
                final float size = version < 2 ? ((float) iter.next().asInt() - 10) / 30 : (float) iter.next().asDouble();
                wordListWithSizes.put(word, size);
            }

            final SortedMap<Double, Set<String>> wordSignificances = new TreeMap<>();
            if (jnode.has(WORDSIGNIFICANCES)) {
                iter = jnode.get(WORDSIGNIFICANCES).iterator();
                while (iter.hasNext()) {
                    final double significance = iter.next().asDouble();
                    final Set<String> words = new HashSet<>();
                    wordSignificances.put(significance, words);
                    final Iterator<JsonNode> subIter = iter.next().iterator();
                    while (subIter.hasNext()) {
                        words.add(iter.next().asText());
                    }
                }
            }

            final double currentSignificance = jnode.has(CURRENTSIGNIFICANCE) ? jnode.get(CURRENTSIGNIFICANCE).asDouble() : 0.05;

            final String queryString = jnode.get(INFO).asText();
            final long modcount = jnode.get(MODCOUNT).asLong();

            final Set<String> selectedWords = new HashSet<>();
            iter = jnode.get(SELECTED).iterator();
            while (iter.hasNext()) {
                selectedWords.add(iter.next().asText());
            }

            final boolean isUnionSelect = jnode.get(ISUNION).asBoolean();
            final boolean isSizeSorted = jnode.get(ISSIZE).asBoolean();

            final WordCloud cloud = new WordCloud(wordsToHashes, hashedWordSets, elementType, wordListWithSizes, wordSignificances, currentSignificance, queryString, modcount, selectedWords, isUnionSelect, isSizeSorted);

            graph.setObjectValue(attributeId, elementId, cloud);
        }
    }

    @Override
    public void writeObject(final Attribute attr, final int elementId, final JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byterWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attr.getId(), elementId)) {
            final WordCloud cloud = (WordCloud) graph.getObjectValue(attr.getId(), elementId);
            if (cloud == null) {
                jsonGenerator.writeNullField(attr.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attr.getName());
				jsonGenerator.writeNumberField(VERSION, 2);

                final Iterator<Map.Entry<String, Integer>> iterMap = cloud.getWordToHashes().entrySet().iterator();
                jsonGenerator.writeArrayFieldStart(WORDTOHASH);
                while (iterMap.hasNext()) {
                    final Map.Entry<String, Integer> entry = iterMap.next();
                    jsonGenerator.writeString(entry.getKey());
                    jsonGenerator.writeNumber(entry.getValue());
                }
                jsonGenerator.writeEndArray();

                final Iterator<Map.Entry<Integer, Set<Integer>>> iterMapSet = cloud.getHashedWordSets().entrySet().iterator();
                jsonGenerator.writeArrayFieldStart(WORDSETS);
                while (iterMapSet.hasNext()) {
                    final Map.Entry<Integer, Set<Integer>> entry = iterMapSet.next();
                    jsonGenerator.writeNumber(entry.getKey());
                    final Set<Integer> set = entry.getValue();
                    final Set<Integer> toWrite = new HashSet<>();
                    for (final int elID : set) {
                        if ((cloud.getElementType() == GraphElementType.VERTEX && graph.vertexExists(elID)) || (cloud.getElementType() == GraphElementType.TRANSACTION && graph.transactionExists(elID))) {
                            toWrite.add(elID);
                        }
                    }

                    jsonGenerator.writeNumber(toWrite.size());
                    final Iterator<Integer> iterSet = toWrite.iterator();
                    while (iterSet.hasNext()) {
                        jsonGenerator.writeNumber(iterSet.next());
                    }
                }
                jsonGenerator.writeEndArray();

                jsonGenerator.writeStringField(ELTYPE, cloud.getElementType().getLabel());

                final Iterator<Map.Entry<String, Float>> iterMapFloat = cloud.getWordListWithSizes().entrySet().iterator();
                jsonGenerator.writeArrayFieldStart(WORDLIST);
                while (iterMapFloat.hasNext()) {
                    final Map.Entry<String, Float> entry = iterMapFloat.next();
                    jsonGenerator.writeString(entry.getKey());
                    jsonGenerator.writeNumber(entry.getValue());
                }
                jsonGenerator.writeEndArray();

                final Iterator<Map.Entry<Double, Set<String>>> iterMapDouble = cloud.getWordSignificances().entrySet().iterator();
                jsonGenerator.writeArrayFieldStart(WORDSIGNIFICANCES);
                while (iterMapDouble.hasNext()) {
                    final Map.Entry<Double, Set<String>> entry = iterMapDouble.next();
                    jsonGenerator.writeNumber(entry.getKey());
                    jsonGenerator.writeStartArray();
                    final Iterator<String> iterStr = entry.getValue().iterator();
                    while (iterStr.hasNext()) {
                        jsonGenerator.writeString(iterStr.next());
                    }
                    jsonGenerator.writeEndArray();
                }
                jsonGenerator.writeEndArray();

                jsonGenerator.writeNumberField(CURRENTSIGNIFICANCE, cloud.getCurrentSignificance());

                jsonGenerator.writeStringField(INFO, cloud.getQueryInfo());
                jsonGenerator.writeNumberField(MODCOUNT, cloud.getModCount());

                final Iterator<String> iterStr = cloud.getSelectedWords().iterator();
                jsonGenerator.writeArrayFieldStart(SELECTED);
                while (iterStr.hasNext()) {
                    jsonGenerator.writeString(iterStr.next());
                }
                jsonGenerator.writeEndArray();

                jsonGenerator.writeBooleanField(ISUNION, cloud.getIsUnionSelect());
                jsonGenerator.writeBooleanField(ISSIZE, cloud.getIsSizeSorted());
                jsonGenerator.writeEndObject();

            }
        }
    }
}
