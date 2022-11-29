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
package au.gov.asd.tac.constellation.graph.utilities.placeholder;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import java.util.List;

/**
 *
 * @author capella
 */
public class StoreGraphRecordStore extends StoreGraph implements RecordStore {

    private int currentSourceVertex = -1;
    private int currentDestinationVertex = -1;
    private int currentTransaction = -1;

    private static final String NOT_SUPPORTED = "Not supported yet.";

    public StoreGraphRecordStore(Schema schema) {
        super(schema);
        schema.newGraph(this);
    }

    public void complete() {
        if (currentSourceVertex != -1) {
            getSchema().completeVertex(this, currentSourceVertex);
            getSchema().completeVertex(this, currentDestinationVertex);
            getSchema().completeTransaction(this, currentTransaction);
        }
    }

    @Override
    public int add() {
        complete();
        currentSourceVertex = addVertex();
        currentDestinationVertex = addVertex();
        currentTransaction = addTransaction(currentSourceVertex, currentDestinationVertex, true);
        return -1;
    }

    @Override
    public void add(RecordStore recordStore) {
        recordStore.reset();
        while (recordStore.next()) {
            add();
            for (String key : recordStore.keys()) {
                set(key, recordStore.get(key));
            }
        }
    }

    /**
     * Sets a key-value pair on the RecordStore.
     *
     * @param key must start with "source.", "transaction." or "destination."
     * @param value the value to associate with the key.
     */
    @Override
    public void set(String key, String value) {
        int dividerPosition = key.indexOf('.');
        int typeStartPosition = key.indexOf('<');
        int typeEndPosition = key.indexOf('>');
        String keyDescriptor = key.substring(0, dividerPosition);
        String keyAttribute;
        String keyType;
        if (typeStartPosition == -1) {
            keyAttribute = key.substring(dividerPosition + 1);
            keyType = "string";
        } else {
            keyAttribute = key.substring(dividerPosition + 1, typeStartPosition);
            keyType = key.substring(typeStartPosition + 1, typeEndPosition);
        }

        int id;
        GraphElementType elementType;
        switch (keyDescriptor) {
            case "source":
                id = currentSourceVertex;
                elementType = GraphElementType.VERTEX;
                break;
            case "destination":
                id = currentDestinationVertex;
                elementType = GraphElementType.VERTEX;
                break;
            case "transaction":
                id = currentTransaction;
                elementType = GraphElementType.TRANSACTION;
                break;
            default:
                throw new RuntimeException("key must start with source, transaction or destination.");
        }
        int attribute = getAttribute(elementType, keyAttribute);
        if (attribute == Graph.NOT_FOUND) {
            attribute = getSchema().getFactory().ensureAttribute(this, elementType, keyAttribute);
            if (attribute == Graph.NOT_FOUND) {
                attribute = addAttribute(elementType, keyType, keyAttribute, "", null, null);
            }
        }

        setStringValue(attribute, id, value);
    }

    @Override
    public int index() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean next() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean hasValue(String key) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean hasValue(int record, String key) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public String get(String key) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public String get(int record, String key) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public List<String> getAll(String key) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void set(int record, String key, String value) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public List<String> values() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public List<String> values(int record) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public List<String> keys() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public String toStringVerbose() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }
}
