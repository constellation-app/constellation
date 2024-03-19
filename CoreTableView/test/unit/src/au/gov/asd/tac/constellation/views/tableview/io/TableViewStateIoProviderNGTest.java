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
package au.gov.asd.tac.constellation.views.tableview.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class TableViewStateIoProviderNGTest extends ConstellationTest {

    private static final int ATTRIBUTE_ID = 55;
    private static final int ELEMENT_ID = 77;

    private TableViewStateIoProvider tableViewStateIoProvider;

    @BeforeMethod
    public void setUpMethod() throws Exception {
        tableViewStateIoProvider = new TableViewStateIoProvider();
    }

    @Test
    public void readObject() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonNode = objectMapper.readTree(
                new FileInputStream(getClass().getResource("resources/tableViewStateRead.json").getPath())
        );

        final GraphWriteMethods graph = mock(GraphWriteMethods.class);

        when(graph.getAttribute(GraphElementType.TRANSACTION, "My Transaction")).thenReturn(1);
        when(graph.getAttribute(GraphElementType.VERTEX, "My Vertex")).thenReturn(2);

        tableViewStateIoProvider.readObject(ATTRIBUTE_ID, ELEMENT_ID, jsonNode, graph, null, null, null, null);

        // Capture the call on the graph setter, pulling out the state
        final ArgumentCaptor<TableViewState> captor = ArgumentCaptor.forClass(TableViewState.class);

        verify(graph, times(1)).setObjectValue(eq(ATTRIBUTE_ID), eq(ELEMENT_ID), captor.capture());

        final TableViewState actual = captor.getValue();

        final TableViewState expected = new TableViewState();

        assertTrue(actual.isSelectedOnly());
        assertEquals(actual.getElementType(), GraphElementType.VERTEX);

        assertEquals(actual.getTransactionColumnAttributes().size(), 1);

        final Tuple<String, Attribute> transactionColumnAttr = actual
                .getTransactionColumnAttributes().get(0);
        assertEquals(transactionColumnAttr.getFirst(), "transactionPrefix");
        assertEquals(transactionColumnAttr.getSecond(), new GraphAttribute(graph, 1));

        assertEquals(actual.getVertexColumnAttributes().size(), 1);

        final Tuple<String, Attribute> vertexColumnAttr = actual
                .getVertexColumnAttributes().get(0);
        assertEquals(vertexColumnAttr.getFirst(), "vertexPrefix");
        assertEquals(vertexColumnAttr.getSecond(), new GraphAttribute(graph, 2));
    }

    @Test
    public void readObjectNullJson() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree("null");

        final GraphWriteMethods graph = mock(GraphWriteMethods.class);

        tableViewStateIoProvider.readObject(ATTRIBUTE_ID, ELEMENT_ID, root, graph, null, null, null, null);

        verifyNoInteractions(graph);
    }

    @Test
    public void readObjectObjectJson() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree("{}");

        final GraphWriteMethods graph = mock(GraphWriteMethods.class);

        tableViewStateIoProvider.readObject(ATTRIBUTE_ID, ELEMENT_ID, root, graph, null, null, null, null);

        verifyNoInteractions(graph);
    }

    @Test
    public void writeObject() throws IOException {
        final GraphAttribute transactionGraphAttr = mock(GraphAttribute.class);
        final GraphAttribute vertexGraphAttr = mock(GraphAttribute.class);

        when(transactionGraphAttr.getElementType()).thenReturn(GraphElementType.TRANSACTION);
        when(transactionGraphAttr.getName()).thenReturn("My Transaction");

        when(vertexGraphAttr.getElementType()).thenReturn(GraphElementType.VERTEX);
        when(vertexGraphAttr.getName()).thenReturn("My Vertex");

        final TableViewState state = new TableViewState();

        state.setSelectedOnly(true);
        state.setElementType(GraphElementType.VERTEX);
        state.setTransactionColumnAttributes(List.of(
                Tuple.create("transactionPrefix", transactionGraphAttr)
        ));
        state.setVertexColumnAttributes(List.of(
                Tuple.create("vertexPrefix", vertexGraphAttr)
        ));

        final Attribute attribute = mock(Attribute.class);
        when(attribute.getId()).thenReturn(ATTRIBUTE_ID);
        when(attribute.getName()).thenReturn("ATTR NAME");

        final GraphWriteMethods graph = mock(GraphWriteMethods.class);
        when(graph.isDefaultValue(ATTRIBUTE_ID, ELEMENT_ID)).thenReturn(false);
        when(graph.getObjectValue(ATTRIBUTE_ID, ELEMENT_ID)).thenReturn(state);

        final JsonFactory factory = new JsonFactory();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        JsonGenerator jsonGenerator = factory.createGenerator(output);

        // The code is written with the assumption that it is called within a document
        // that has already started being written. Without starting the object in the test
        // the code would throw invalid json exceptions.
        jsonGenerator.writeStartObject();

        tableViewStateIoProvider.writeObject(attribute, ELEMENT_ID, jsonGenerator, graph, null, false);

        jsonGenerator.writeEndObject();

        jsonGenerator.flush();

        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode expected = objectMapper.readTree(
                new FileInputStream(getClass().getResource("resources/tableViewStateWrite.json").getPath())
        );

        final JsonNode actual = objectMapper.readTree(new String(output.toByteArray(), StandardCharsets.UTF_8));

        assertEquals(actual, expected);
    }

    @Test
    public void writeObjectStateIsNull() throws IOException {
        final Attribute attribute = mock(Attribute.class);
        when(attribute.getId()).thenReturn(ATTRIBUTE_ID);
        when(attribute.getName()).thenReturn("ATTR NAME");

        final GraphWriteMethods graph = mock(GraphWriteMethods.class);
        when(graph.isDefaultValue(ATTRIBUTE_ID, ELEMENT_ID)).thenReturn(false);
        when(graph.getObjectValue(ATTRIBUTE_ID, ELEMENT_ID)).thenReturn(null);

        final JsonFactory factory = new JsonFactory();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        JsonGenerator jsonGenerator = factory.createGenerator(output);

        // The code is written with the assumption that it is called within a document
        // that has already started being written. Without starting the object in the test
        // the code would throw invalid json exceptions.
        jsonGenerator.writeStartObject();

        tableViewStateIoProvider.writeObject(attribute, ELEMENT_ID, jsonGenerator, graph, null, false);

        jsonGenerator.writeEndObject();

        jsonGenerator.flush();

        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode expected = objectMapper.readTree(
                "{\"ATTR NAME\": null}"
        );

        final JsonNode actual = objectMapper.readTree(new String(output.toByteArray(), StandardCharsets.UTF_8));

        assertEquals(actual, expected);
    }

    @Test
    public void writeObjectIsDefaultValue() throws IOException {
        final Attribute attribute = mock(Attribute.class);
        when(attribute.getId()).thenReturn(ATTRIBUTE_ID);
        when(attribute.getName()).thenReturn("ATTR NAME");

        final GraphWriteMethods graph = mock(GraphWriteMethods.class);
        when(graph.isDefaultValue(ATTRIBUTE_ID, ELEMENT_ID)).thenReturn(true);

        final JsonFactory factory = new JsonFactory();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final JsonGenerator jsonGenerator = factory.createGenerator(output);

        tableViewStateIoProvider.writeObject(attribute, ELEMENT_ID, jsonGenerator, graph, null, false);

        jsonGenerator.flush();

        assertEquals(new String(output.toByteArray(), StandardCharsets.UTF_8), "");
    }
}
