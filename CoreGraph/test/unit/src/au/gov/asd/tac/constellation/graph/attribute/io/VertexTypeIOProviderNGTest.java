/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.attribute.io;

import au.gov.asd.tac.constellation.graph.schema.SchemaVertexType;
import au.gov.asd.tac.constellation.visual.color.ConstellationColor;
import au.gov.asd.tac.constellation.visual.icons.CharacterIconProvider;
import au.gov.asd.tac.constellation.visual.icons.DefaultIconProvider;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Vertex Type IOProvider Test.
 *
 * @author arcturus
 */
public class VertexTypeIOProviderNGTest {

    public VertexTypeIOProviderNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

//    /**
//     * Test of getName method, of class VertexTypeIOProvider.
//     */
//    @Test
//    public void testGetName() {
//        System.out.println("getName");
//        VertexTypeIOProvider instance = new VertexTypeIOProvider();
//        String expResult = "";
//        String result = instance.getName();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of readObject method, of class VertexTypeIOProvider.
//     */
//    @Test
//    public void testReadObject() throws Exception {
//        System.out.println("readObject");
//        int attributeId = 0;
//        int elementId = 0;
//        JsonNode jnode = null;
//        GraphWriteMethods graph = null;
//        Map<Integer, Integer> vertexMap = null;
//        Map<Integer, Integer> transactionMap = null;
//        GraphByteReader byteReader = null;
//        ImmutableObjectCache cache = null;
//        VertexTypeIOProvider instance = new VertexTypeIOProvider();
//        instance.readObject(attributeId, elementId, jnode, graph, vertexMap, transactionMap, byteReader, cache);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of writeObject method, of class VertexTypeIOProvider.
//     */
//    @Test
//    public void testWriteObject() throws Exception {
//        System.out.println("writeObject");
//        Attribute attribute = null;
//        int elementId = 0;
//        JsonGenerator jsonGenerator = null;
//        GraphReadMethods graph = null;
//        GraphByteWriter byteWriter = null;
//        boolean verbose = false;
//        VertexTypeIOProvider instance = new VertexTypeIOProvider();
//        instance.writeObject(attribute, elementId, jsonGenerator, graph, byteWriter, verbose);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of readTypeObject method, of class VertexTypeIOProvider.
//     */
//    @Test
//    public void testReadTypeObject() {
//        System.out.println("readTypeObject");
//        JsonNode type = null;
//        SchemaVertexType expResult = null;
//        SchemaVertexType result = VertexTypeIOProvider.readTypeObject(type);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    @Test
    public void testWriteTypeObject() throws Exception {
        final SchemaVertexType type = new SchemaVertexType.Builder("type")
                .setDescription("description")
                .setColor(ConstellationColor.GREEN)
                .setForegroundIcon(CharacterIconProvider.CHAR_0020)
                .setBackgroundIcon(DefaultIconProvider.FLAT_CIRCLE)
                .setDetectionRegex(Pattern.compile("\\+?([0-9]{8,13})", Pattern.CASE_INSENSITIVE))
                .setValidationRegex(Pattern.compile("\\+?([0-9]{8,15})", Pattern.CASE_INSENSITIVE))
                .setProperty("my key", "my value")
                .setIncomplete(true)
                .build();

        final ByteArrayOutputStream actual = new ByteArrayOutputStream();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(actual, JsonEncoding.UTF8);

        jsonGenerator.writeStartObject();
        VertexTypeIOProvider.writeTypeObject(type, jsonGenerator);
        jsonGenerator.writeEndObject();
        jsonGenerator.close();

        final ByteArrayOutputStream expected = new ByteArrayOutputStream();
        jsonGenerator = new JsonFactory().createGenerator(expected, JsonEncoding.UTF8);

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("Name", "type");
        jsonGenerator.writeStringField("Description", "description");

        jsonGenerator.writeObjectFieldStart("Color");
        jsonGenerator.writeStringField("name", "Green");
        jsonGenerator.writeEndObject();

        jsonGenerator.writeStringField("Foreground Icon", "Character.Space");
        jsonGenerator.writeStringField("Background Icon", "Background.Flat Circle");
        jsonGenerator.writeStringField("Detection Regex", "\\+?([0-9]{8,13})");
        jsonGenerator.writeStringField("Validation Regex", "\\+?([0-9]{8,15})");

        jsonGenerator.writeObjectFieldStart("Properties");
        jsonGenerator.writeStringField("my key", "my value");
        jsonGenerator.writeEndObject();

        jsonGenerator.writeBooleanField("Incomplete", true);

        jsonGenerator.writeEndObject();
        jsonGenerator.close();

        Assert.assertEquals(actual.toString(), expected.toString());
    }

    @Test
    public void testWriteTypeObjectWithParent() throws Exception {
        final SchemaVertexType parent = new SchemaVertexType.Builder("parent")
                .setDescription("parent description")
                .setColor(ConstellationColor.GREEN)
                .setForegroundIcon(CharacterIconProvider.CHAR_0020)
                .setBackgroundIcon(DefaultIconProvider.FLAT_CIRCLE)
                .setDetectionRegex(Pattern.compile("\\+?([0-9]{8,13})", Pattern.CASE_INSENSITIVE))
                .setValidationRegex(Pattern.compile("\\+?([0-9]{8,15})", Pattern.CASE_INSENSITIVE))
                .setProperty("my key", "my value")
                .setIncomplete(true)
                .build();

        final SchemaVertexType child = new SchemaVertexType.Builder("child")
                .setDescription("child description")
                .setColor(ConstellationColor.GREEN)
                .setForegroundIcon(CharacterIconProvider.CHAR_0020)
                .setBackgroundIcon(DefaultIconProvider.FLAT_CIRCLE)
                .setDetectionRegex(Pattern.compile("\\+?([0-9]{8,13})", Pattern.CASE_INSENSITIVE))
                .setValidationRegex(Pattern.compile("\\+?([0-9]{8,15})", Pattern.CASE_INSENSITIVE))
                .setSuperType(parent)
                .setProperty("my key", "my value")
                .setIncomplete(true)
                .build();

        final ByteArrayOutputStream actual = new ByteArrayOutputStream();
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(actual, JsonEncoding.UTF8);
//        jsonGenerator.useDefaultPrettyPrinter();

        jsonGenerator.writeStartObject();
        VertexTypeIOProvider.writeTypeObject(child, jsonGenerator);
        jsonGenerator.writeEndObject();

        jsonGenerator.close();

        final ByteArrayOutputStream expected = new ByteArrayOutputStream();
        jsonGenerator = new JsonFactory().createGenerator(expected, JsonEncoding.UTF8);
//        jsonGenerator.useDefaultPrettyPrinter();

        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("Name", "child");
        jsonGenerator.writeStringField("Description", "child description");

        jsonGenerator.writeObjectFieldStart("Color");
        jsonGenerator.writeStringField("name", "Green");
        jsonGenerator.writeEndObject();

        jsonGenerator.writeStringField("Foreground Icon", "Character.Space");
        jsonGenerator.writeStringField("Background Icon", "Background.Flat Circle");
        jsonGenerator.writeStringField("Detection Regex", "\\+?([0-9]{8,13})");
        jsonGenerator.writeStringField("Validation Regex", "\\+?([0-9]{8,15})");

        // supertype start
        jsonGenerator.writeObjectFieldStart("Super Type");
        jsonGenerator.writeStringField("Name", "parent");
        jsonGenerator.writeStringField("Description", "parent description");

        jsonGenerator.writeObjectFieldStart("Color");
        jsonGenerator.writeStringField("name", "Green");
        jsonGenerator.writeEndObject();

        jsonGenerator.writeStringField("Foreground Icon", "Character.Space");
        jsonGenerator.writeStringField("Background Icon", "Background.Flat Circle");
        jsonGenerator.writeStringField("Detection Regex", "\\+?([0-9]{8,13})");
        jsonGenerator.writeStringField("Validation Regex", "\\+?([0-9]{8,15})");

        jsonGenerator.writeObjectFieldStart("Properties");
        jsonGenerator.writeStringField("my key", "my value");
        jsonGenerator.writeEndObject();

        jsonGenerator.writeBooleanField("Incomplete", true);

        jsonGenerator.writeEndObject();
        // supertype end

        jsonGenerator.writeObjectFieldStart("Properties");
        jsonGenerator.writeStringField("my key", "my value");
        jsonGenerator.writeEndObject();

        jsonGenerator.writeBooleanField("Incomplete", true);

        jsonGenerator.writeEndObject();

        jsonGenerator.close();

        Assert.assertEquals(actual.toString(), expected.toString());
    }

//    /**
//     * Test of readColorObject method, of class VertexTypeIOProvider.
//     */
//    @Test
//    public void testReadColorObject() {
//        System.out.println("readColorObject");
//        JsonNode color = null;
//        ColorValue expResult = null;
//        ColorValue result = VertexTypeIOProvider.readColorObject(color);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of writeColorObject method, of class VertexTypeIOProvider.
//     */
//    @Test
//    public void testWriteColorObject() throws Exception {
//        System.out.println("writeColorObject");
//        ColorValue color = null;
//        JsonGenerator jsonGenerator = null;
//        VertexTypeIOProvider.writeColorObject(color, jsonGenerator);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
