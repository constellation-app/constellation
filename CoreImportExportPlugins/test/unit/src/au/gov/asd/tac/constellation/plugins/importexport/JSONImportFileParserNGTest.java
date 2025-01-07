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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.InputSource;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.JSONImportFileParser;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test code exercising JSONImportFileParser
 *
 * @author serpens24
 */
public class JSONImportFileParserNGTest {

    // Reflection used to view private fields in class under test.
    static JSONImportFileParser instance = new JSONImportFileParser();
    static Field privateInvalidJSONField = null;
    static Field privateNoValidListField = null;
    static String privateInvalidJSONMsg = "";
    static String privateNoValidListMsg = "";

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Store content of some private strings used in class under test to
        // allow verification of thrown exception content.
        privateInvalidJSONField = JSONImportFileParser.class.getDeclaredField("WARN_INVALID_JSON");
        privateNoValidListField = JSONImportFileParser.class.getDeclaredField("WARN_NO_VALID_LIST");
        privateInvalidJSONField.setAccessible(true);
        privateNoValidListField.setAccessible(true);
        privateInvalidJSONMsg = (String) privateInvalidJSONField.get(instance);
        privateNoValidListMsg = (String) privateNoValidListField.get(instance);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    @Test
    public void checkPreviewInvalidJSON() throws InterruptedException {
        // Confirm that attempts to preview invalid JSON return a clean
        // IOException exception.
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-invalidContent.json").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(privateInvalidJSONMsg));
        }
    }

    @Test
    public void checkParseInvalidJSON() throws InterruptedException {
        // Confirm that attempts to parse invalid JSON return a clean
        // IOException exception.
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-invalidContent.json").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(privateInvalidJSONMsg));
        }
    }

    @Test
    public void checkPreviewEmptyJSON() throws InterruptedException {
        // Confirm that attempts to preview empty JSON return a clean
        // IOException exception.
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyContent.json").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(privateNoValidListMsg));
        }
    }

    @Test
    public void checkParseEmptyJSON() throws InterruptedException {
        // Confirm that attempts to parse empty JSON return a clean
        // IOException exception.
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyContent.json").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(privateNoValidListMsg));
        }
    }

    @Test
    public void checkPreviewEmptyObject() throws InterruptedException {
        // Confirm that attempts to preview JSON containing only an empty object
        // return a clean IOException exception.
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyObject.json").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(privateNoValidListMsg));
        }
    }

    @Test
    public void checkParseEmptyObject() throws InterruptedException {
        // Confirm that attempts to parse JSON containing only an empty object
        // return a clean IOException exception.
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyObject.json").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(privateNoValidListMsg));
        }
    }

    @Test
    public void checkPreviewEmptyList1() throws InterruptedException {
        // Confirm that attempts to preview JSON containing only an empty list
        // return a clean IOException exception.
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyList1.json").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(privateNoValidListMsg));
        }
    }

    @Test
    public void checkParseEmptyList1() throws InterruptedException {
        // Confirm that attempts to parse JSON containing only an empty list
        // return a clean IOException exception.
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyList1.json").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(privateNoValidListMsg));
        }
    }

    @Test
    public void checkPreviewEmptyList2() throws InterruptedException {
        // Confirm that attempts to preview JSON containing a list containing
        // only empty lists return a clean IOException exception.
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyList2.json").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(privateNoValidListMsg));
        }
    }

    @Test
    public void checkParseEmptyList2() throws InterruptedException {
        // Confirm that attempts to parse JSON containing a list containing
        // only empty lists return a clean IOException exception.
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyList2.json").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(privateNoValidListMsg));
        }
    }

    @Test
    public void checkPreviewEmptyList3() throws InterruptedException {
        // Confirm that attempts to preview JSON containing a list containing
        // only empty objects return a clean IOException exception.
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyList3.json").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(privateNoValidListMsg));
        }
    }

    @Test
    public void checkParseEmptyList3() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyList3.json").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(privateNoValidListMsg));
        }
    }

    @Test
    public void checkPreviewInconsistentListLength() throws InterruptedException {
        // Confirm that attempts to preview JSON containing a list with rows
        // of different lengths return a clean IOException exception.
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-inconsistentListLength.json").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(privateNoValidListMsg));
        }
    }

    @Test
    public void checkParseInconsistentListLength() throws InterruptedException {
        // Confirm that attempts to parse JSON containing a list with rows
        // of different lengths return a clean IOException exception.
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-inconsistentListLength.json").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(privateNoValidListMsg));
        }
    }

    @Test
    public void checkPreviewListWithInvalidObjects() throws InterruptedException {
        // Confirm that attempts to preview JSON containing a list with rows
        // containing nested complex objects return a clean IOException
        // exception.
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-complexMembersInList.json").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(privateNoValidListMsg));
        }
    }

    @Test
    public void checkParseListWithInvalidObjects() throws InterruptedException {
        // Confirm that attempts to parse JSON containing a list with rows
        // containing nested complex objects return a clean IOException
        // exception.
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-complexMembersInList.json").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains(privateNoValidListMsg));
        }
    }

    @Test
    public void checkPreviewIgnoreInvalidNestedLists() throws InterruptedException, IOException {
        // Confirm that attempts to preview JSON containing an object containing
        // invalid nested lists do not return this list, but instrad skip over
        // them to find a suitable list, even thou8gh nested deeper.
        final JSONImportFileParser parser = new JSONImportFileParser();
        
        final List<String[]> expectedData = new ArrayList<>();
        expectedData.add(new String[]{"word"});
        expectedData.add(new String[]{"Hello"});
        expectedData.add(new String[]{"World"});
        
        final List<String[]> data = parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-nested-skipInvalidContent.json").getFile())), null, 100);
        Assert.assertEquals(data.size(), expectedData.size(), "Returned results size is not as expected");
        IntStream.range(0, data.size()).forEach(idx -> {
            Assert.assertEquals(data.get(idx), expectedData.get(idx));
        });
    }

    @Test
    public void checkParseIgnoreInvalidNestedLists() throws InterruptedException, IOException {
        // Confirm that attempts to parse JSON containing an object containing
        // invalid nested lists do not return this list, but instrad skip over
        // them to find a suitable list, even thou8gh nested deeper.
        final JSONImportFileParser parser = new JSONImportFileParser();
        
        final List<String[]> expectedData = new ArrayList<>();
        expectedData.add(new String[]{"word"});
        expectedData.add(new String[]{"Hello"});
        expectedData.add(new String[]{"World"});

        final List<String[]> data = parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-nested-skipInvalidContent.json").getFile())), null);
        Assert.assertEquals(data.size(), expectedData.size(), "Returned results size is not as expected");
        IntStream.range(0, data.size()).forEach(idx -> {
            Assert.assertEquals(data.get(idx), expectedData.get(idx));
        });
    }

    @Test
    public void checkPreviewFindShallowestList() throws InterruptedException, IOException {
        // Confirm that attempts to preview JSON containing multiple valid lists
        // return the shallowest of these lists.
        final JSONImportFileParser parser = new JSONImportFileParser();
        
        final List<String[]> expectedData = new ArrayList<>();
        expectedData.add(new String[]{"depth2a"});
        expectedData.add(new String[]{"2"});
        
        final List<String[]> data = parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-nested-getShallowest.json").getFile())), null, 100);
        Assert.assertEquals(data.size(), expectedData.size(), "Returned results size is not as expected");
        IntStream.range(0, data.size()).forEach(idx -> {
            Assert.assertEquals(data.get(idx), expectedData.get(idx));
        });
    }

    @Test
    public void checkParseFindShallowestList() throws InterruptedException, IOException {
        // Confirm that attempts to parse JSON containing multiple valid lists
        // return the shallowest of these lists.
        final JSONImportFileParser parser = new JSONImportFileParser();
        
        final List<String[]> expectedData = new ArrayList<>();
        expectedData.add(new String[]{"depth2a"});
        expectedData.add(new String[]{"2"});
        
        final List<String[]> data = parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-nested-getShallowest.json").getFile())), null);
        Assert.assertEquals(data.size(), expectedData.size(), "Returned results size is not as expected");
        IntStream.range(0, data.size()).forEach(idx -> {
            Assert.assertEquals(data.get(idx), expectedData.get(idx));
        });
    }

    @Test
    public void checkPreviewNestedObjects() throws InterruptedException, IOException {
        // Confirm that attempts to preview JSON containing multiple valid lists
        // return the shallowest of these lists.
        final JSONImportFileParser parser = new JSONImportFileParser();

        final ArrayList<String[]> expectedData = new ArrayList<>();
        expectedData.add(new String[]{"name", "age", "address.town", "address.state", "address.postcode", "address.commercial", "address.history.est", "address.history.population", "description", "address.latitude", "address.longitude"});
        expectedData.add(new String[]{"record1", "45", "Darwin", "NT", "0800", "[\"pub\",\"shop\"]", "2025", "600", null, null, null});
        
        final List<String[]> data = parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-nested-processNestedObjects.json").getFile())), null, 1);
        Assert.assertEquals(data.size(), expectedData.size(), "Returned results size is not as expected");
        IntStream.range(0, data.size()).forEach(idx -> {
            String[] dataRow = data.get(idx);
            String[] expectedRow = expectedData.get(idx);
            IntStream.range(0, dataRow.length).forEach(jdx -> {
                Assert.assertEquals(dataRow[jdx], expectedRow[jdx]);
            });
        });
    }

    @Test
    public void checkParseNestedObjects() throws InterruptedException, IOException {
        // Confirm that attempts to preview JSON containing multiple valid lists
        // return the shallowest of these lists.
        final JSONImportFileParser parser = new JSONImportFileParser();
        
        final List<String[]> expectedData = new ArrayList<>();
        expectedData.add(new String[]{"name", "age", "address.town", "address.state", "address.postcode", "address.commercial", "address.history.est", "address.history.population", "description", "address.latitude", "address.longitude"});
        expectedData.add(new String[]{"record1", "45", "Darwin", "NT", "0800", "[\"pub\",\"shop\"]", "2025", "600", null, null, null});
        expectedData.add(new String[]{"record2", null, "Hobart", "TAS", "7000", null, null, null, "this is a description", "-42.8821", "147.3272"});
        
        final List<String[]> data = parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-nested-processNestedObjects.json").getFile())), null);
        Assert.assertEquals(data.size(), expectedData.size(), "Returned results size is not as expected");
        IntStream.range(0, data.size()).forEach(idx -> {
            String[] dataRow = data.get(idx);
            String[] expectedRow = expectedData.get(idx);
            IntStream.range(0, dataRow.length).forEach(jdx -> {
                Assert.assertEquals(dataRow[jdx], expectedRow[jdx]);
            });
        });
    }
}
