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
package au.gov.asd.tac.constellation.graph.schema.analytic.attribute.io;

import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.CharacterIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
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
}
