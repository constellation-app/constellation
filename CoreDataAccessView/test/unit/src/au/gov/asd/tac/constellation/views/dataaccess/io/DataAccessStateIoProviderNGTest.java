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
package au.gov.asd.tac.constellation.views.dataaccess.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.views.dataaccess.state.DataAccessState;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessStateIoProviderNGTest {

    private static final int ATTRIBUTE_ID = 55;
    private static final int ELEMENT_ID = 77;

    private DataAccessStateIoProvider dataAccessStateIoProvider;

    @BeforeMethod
    public void setUpMethod() throws Exception {
        dataAccessStateIoProvider = new DataAccessStateIoProvider();
    }

    @Test
    public void readObject() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonNode = objectMapper.readTree(
                new FileInputStream(getClass().getResource("resources/dataAccessStateRead.json").getPath())
        );

        final GraphWriteMethods graph = mock(GraphWriteMethods.class);

        dataAccessStateIoProvider.readObject(ATTRIBUTE_ID, ELEMENT_ID, jsonNode, graph, null, null, null, null);

        // Capture the call on the graph setter, pulling out the state
        final ArgumentCaptor<DataAccessState> captor = ArgumentCaptor.forClass(DataAccessState.class);

        verify(graph, times(1)).setObjectValue(eq(ATTRIBUTE_ID), eq(ELEMENT_ID), captor.capture());

        final List<Map<String, String>> state = captor.getValue().getState();

        // Verify that there are two tabs
        assertEquals(state.size(), 2);

        // Verify the contents of tab 1
        final Map<String, String> expectedTab1 = new HashMap<>();
        expectedTab1.put("key1", "value1");
        expectedTab1.put("key2", null);
        expectedTab1.put("key3", null);

        assertEquals(state.get(0), expectedTab1);

        // Verify the contents of tab 4
        final Map<String, String> expectedTab2 = new HashMap<>();
        expectedTab2.put("key4", "value4");

        assertEquals(state.get(1), expectedTab2);
    }

    @Test
    public void readObjectNullJson() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree("null");

        final GraphWriteMethods graph = mock(GraphWriteMethods.class);

        dataAccessStateIoProvider.readObject(ATTRIBUTE_ID, ELEMENT_ID, root, graph, null, null, null, null);

        final ArgumentCaptor<DataAccessState> captor = ArgumentCaptor.forClass(DataAccessState.class);

        verify(graph).setObjectValue(eq(ATTRIBUTE_ID), eq(ELEMENT_ID), captor.capture());

        final List<Map<String, String>> state = captor.getValue().getState();

        assertTrue(state.isEmpty());
    }

    @Test
    public void readObjectObjectJson() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree("{}");

        final GraphWriteMethods graph = mock(GraphWriteMethods.class);

        dataAccessStateIoProvider.readObject(ATTRIBUTE_ID, ELEMENT_ID, root, graph, null, null, null, null);

        final ArgumentCaptor<DataAccessState> captor = ArgumentCaptor.forClass(DataAccessState.class);

        verify(graph).setObjectValue(eq(ATTRIBUTE_ID), eq(ELEMENT_ID), captor.capture());

        final List<Map<String, String>> state = captor.getValue().getState();

        assertTrue(state.isEmpty());
    }

    @Test
    public void writeObject() throws IOException {
        final DataAccessState state = new DataAccessState();

        state.newTab();
        state.add("key1", "value1");
        state.add("key2", "value2");

        state.newTab();
        state.add("key3", "value3");

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

        dataAccessStateIoProvider.writeObject(attribute, ELEMENT_ID, jsonGenerator, graph, null, false);

        jsonGenerator.writeEndObject();

        jsonGenerator.flush();

        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode expected = objectMapper.readTree(
                new FileInputStream(getClass().getResource("resources/dataAccessStateWrite.json").getPath())
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

        dataAccessStateIoProvider.writeObject(attribute, ELEMENT_ID, jsonGenerator, graph, null, false);

        jsonGenerator.writeEndObject();

        jsonGenerator.flush();

        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode expected = objectMapper.readTree("""
                                                        {"ATTR NAME": null}""");

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

        dataAccessStateIoProvider.writeObject(attribute, ELEMENT_ID, jsonGenerator, graph, null, false);

        jsonGenerator.flush();

        assertEquals(new String(output.toByteArray(), StandardCharsets.UTF_8), "");
    }
}
