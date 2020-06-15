/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.JSONImportFileParser;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.InputSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.netbeans.api.templates.FileBuilder;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author serpens24
 */
public class JSONImportFielParserNGTest {

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
    public void checkPreviewInvalidJSON() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-invalidContent.json").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }

    @Test
    public void checkParseInvalidJSON() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            final List<String[]> data = parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-invalidContent.json").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }

    @Test
    public void checkPreviewEmptyJSON() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyContent.json").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }

    @Test
    public void checkParseEmptyJSON() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            final List<String[]> data = parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyContent.json").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }

    @Test
    public void checkPreviewEmptyObject() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyObject.json").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }

    @Test
    public void checkParseEmptyObject() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            final List<String[]> data = parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyObject.json").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }

    @Test
    public void checkPreviewEmptyList1() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyList1.json").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }

    @Test
    public void checkParseEmptyList1() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            final List<String[]> data = parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyList1.json").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }

    @Test
    public void checkPreviewEmptyList2() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyList2.json").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }

    @Test
    public void checkParseEmptyList2() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            final List<String[]> data = parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyList2.json").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }

    @Test
    public void checkPreviewEmptyList3() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyList3.json").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }

    @Test
    public void checkParseEmptyList3() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            final List<String[]> data = parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-emptyList3.json").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }

    @Test
    public void checkPreviewInconsistentListLength() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-inconsistentListLength.json").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }

    @Test
    public void checkParseInconsistentListLength() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            final List<String[]> data = parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-inconsistentListLength.json").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }

    @Test
    public void checkPreviewListWithInvalidObjects() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-complexMembersInList.json").getFile())), null, 100);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }

    @Test
    public void checkParseListWithInvalidObjects() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            final List<String[]> data = parser.parse(new InputSource(new File(this.getClass().getResource("./resources/JSON-complexMembersInList.json").getFile())), null);
            Assert.fail("Expected exception not received");
        } catch (IOException ex) {
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }

    @Test
    public void checkPreviewIgnoreInvalidNestedLists() throws InterruptedException {
        final JSONImportFileParser parser = new JSONImportFileParser();
        try {
            final ArrayList<String[]> expectedData = new ArrayList<>();
            expectedData.add(new String[]{"word"});
            expectedData.add(new String[]{"Hello"});
            expectedData.add(new String[]{"World"});
            final List<String[]> data = parser.preview(new InputSource(new File(this.getClass().getResource("./resources/JSON-nested-skipInvalidContent.json").getFile())), null, 100);
            Assert.assertEquals(data.size(), expectedData.size(), "Returned results size is not as expected");
            IntStream.range(0, data.size()).forEach(idx -> {
                Assert.assertEquals(data.get(idx), expectedData.get(idx), "message");
           });     
        } catch (Exception ex) {
            Assert.fail("Unexpected exception received");
        }
    }
    
    
}
