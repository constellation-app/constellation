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
package au.gov.asd.tac.constellation.help.utilities.toc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
 public class TOCGeneratorNGTest {

    public TOCGeneratorNGTest() {
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
    public void testCreateTOCFile() {
        System.out.println("createTOCFile");

        File tempFileTOC = null;
        String validPath = "";
        try {
            try {
                // Create a temp file to grab a valid path on the filesystem
                tempFileTOC = File.createTempFile("tempFileTOC", ".md");
                validPath = tempFileTOC.getAbsolutePath();

            } catch (final IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } finally {
            if (tempFileTOC != null && tempFileTOC.exists()) {
                tempFileTOC.delete();
            }
        }
        // make a POJO at the path, check if a real file exists. There shouldn't be.
        final File tempFileAtValidPath = new File(validPath);
        assertFalse(tempFileAtValidPath.exists());

        // run the method, assert that it returns true for success, and that there is now a file at that location.
        assertTrue(TOCGenerator.createTOCFile(validPath));
        assertTrue(tempFileAtValidPath.exists());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateTOCFileFail() {
        System.out.println("createTOCFileFail");
        final String invalidPath = null;
        TOCGenerator.createTOCFile(invalidPath);
    }

    @Test
    public void testCreateTOCFileExists() {
        System.out.println("createTOCFile");

        File tempFileTOC = null;
        File tempFileAtValidPath = null;
        try {
            try {
                // Create a temp file to grab a valid path on the filesystem
                tempFileTOC = File.createTempFile("tempFileTOC", ".md");
                // assert file is empty beforehand
                assertTrue(tempFileTOC.length() == 0);

                // write into file, assert it has content
                FileWriter fw = new FileWriter(tempFileTOC);
                fw.write("this file now has content");
                fw.close();
                assertTrue(tempFileTOC.length() != 0);

                final String validPath = tempFileTOC.getAbsolutePath();

                // make a POJO at the path, check if a real file exists. There should be.
                tempFileAtValidPath = new File(validPath);
                assertTrue(tempFileAtValidPath.exists());

                // run the method, assert that it returns true for success, and that there is now a file at that location.
                assertTrue(TOCGenerator.createTOCFile(validPath));
                assertTrue(tempFileAtValidPath.exists());

                // assert file is empty
                assertTrue(tempFileTOC.length() == 0);

            } catch (final IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } finally {
            if (tempFileTOC != null && tempFileTOC.exists()) {
                tempFileTOC.delete();
            }
            if (tempFileAtValidPath != null && tempFileAtValidPath.exists()) {
                tempFileAtValidPath.delete();
            }
        }

    }

    /**
     * Test of convertXMLMappings method, of class TOCGenerator.
     */
    @Test
    public void testConvertXMLMappings_List_TreeNode() {
        System.out.println("convertXMLMappings");
        List<File> xmlsFromFile = new ArrayList<>();

        final TreeNode root = new TreeNode(new TOCItem("root", ""));
        File tempFileTOC = null;
        try {
            try {
                tempFileTOC = File.createTempFile("tempFileTOC", ".md");
                TOCGenerator.createTOCFile(tempFileTOC.getPath());
                assertFalse(tempFileTOC.length() != 0);
                TOCGenerator.convertXMLMappings(xmlsFromFile, root);

                // Assert file is not empty, meaning a TOC file has been created
                assertTrue(tempFileTOC.length() != 0);

            } catch (final IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } finally {
            if (tempFileTOC != null && tempFileTOC.exists()) {
                tempFileTOC.delete();
            }
        }
    }

    /**
     * Test of convertXMLMappings method, of class TOCGenerator.
     *
     * @throws java.io.IOException
     */
    @Test(expectedExceptions = IOException.class)
    public void testConvertXMLMappings_List_TreeNodeFail() throws IOException {
        System.out.println("convertXMLMappingsFail");
        List<File> xmlsFromFile = new ArrayList<>();
        final TreeNode root = new TreeNode(new TOCItem("root", ""));
        TOCGenerator.createTOCFile("incorrect/path/to/toc");
        TOCGenerator.convertXMLMappings(xmlsFromFile, root);
    }

    /**
     * Test of convertXMLMappings method, of class TOCGenerator.
     */
    @Test
    public void testConvertXMLMappings_3args() {
        System.out.println("convertXMLMappings");

        final String fileContents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE toc PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 2.0//EN\" \"http://java.sun.com/products/javahelp/toc_2_0.dtd\">\n"
                + "<toc version=\"2.0\">\n"
                + "    <tocitem text=\"Views\" mergetype=\"javax.help.SortMerge\">\n"
                + "        <tocitem text=\"Layers View\" mergetype=\"javax.help.SortMerge\">\n"
                + "            <tocitem text=\"Layers View\" target=\"au.gov.asd.tac.constellation.views.layers.LayersViewTopComponent\" />\n"
                + "        </tocitem>\n"
                + "    </tocitem>\n"
                + "</toc>\n"
                + "";
        final String fileContents2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE toc PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 2.0//EN\" \"http://java.sun.com/products/javahelp/toc_2_0.dtd\">\n"
                + "<toc version=\"2.0\">\n"
                + "    <tocitem text=\"Views\" mergetype=\"javax.help.SortMerge\">\n"
                + "        <tocitem text=\"Notes View\" mergetype=\"javax.help.SortMerge\">\n"
                + "            <tocitem text=\"Notes View\" target=\"au.gov.asd.tac.constellation.views.notes.NotesViewTopComponent\" />\n"
                + "        </tocitem>\n"
                + "    </tocitem>\n"
                + "</toc>\n"
                + "";

        File tempFile = null;
        File tempFile2 = null;
        File tempFileTOC = null;

        try {
            try {
                tempFile = File.createTempFile("testfile", ".xml");
                tempFile2 = File.createTempFile("testfile2", ".xml");
                tempFileTOC = File.createTempFile("tempFileTOC", ".md");

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            // try with resources
            try (final FileWriter fw = new FileWriter(tempFile)) {
                fw.append(fileContents);
            }
            // try with resources
            try (final FileWriter fw = new FileWriter(tempFile2)) {
                fw.append(fileContents2);
            }

            final List<File> xmlsFromFile = new ArrayList<>();
            xmlsFromFile.add(tempFile);
            xmlsFromFile.add(tempFile2);

            final TreeNode root = new TreeNode(new TOCItem("root", ""));

            // try with resources
            try (final FileWriter fw = new FileWriter(tempFileTOC)) {
                TOCGenerator.convertXMLMappings(xmlsFromFile, fw, root);
            }
            assertEquals(root.getChildren().size(), 1);

            final TreeNode child1 = (TreeNode) root.getChildren().get(0); // Views
            final TreeNode child11a = (TreeNode) child1.getChildren().get(0); // Layers View
            final TreeNode child111a = (TreeNode) child11a.getChildren().get(0); // Layers View link
            final TreeNode child11b = (TreeNode) child1.getChildren().get(1); // Notes View
            final TreeNode child111b = (TreeNode) child11b.getChildren().get(0); // Notes View link

            assertEquals(child11a.getChildren().size(), 1);
            assertEquals(child11b.getChildren().size(), 1);
            assertEquals(child111a.getChildren().size(), 0);
            assertEquals(child111b.getChildren().size(), 0);

            final TreeNode expectedChild1 = new TreeNode(new TOCItem("Views", ""));
            final TreeNode expectedChild11a = new TreeNode(new TOCItem("Layers View", ""));
            final TreeNode expectedChild111a = new TreeNode(new TOCItem("Layers View", "au.gov.asd.tac.constellation.views.layers.LayersViewTopComponent"));
            final TreeNode expectedChild11b = new TreeNode(new TOCItem("Notes View", ""));
            final TreeNode expectedChild111b = new TreeNode(new TOCItem("Notes View", "au.gov.asd.tac.constellation.views.notes.NotesViewTopComponent"));

            assertEquals(child1, expectedChild1);
            assertEquals(child11a, expectedChild11a);
            assertEquals(child111a, expectedChild111a);
            assertEquals(child11b, expectedChild11b);
            assertEquals(child111b, expectedChild111b);

            assertNotNull(tempFileTOC);
            assertTrue(tempFileTOC.exists());

            BufferedReader reader = new BufferedReader(new FileReader(tempFileTOC));

            assertEquals(reader.readLine(), String.format("<div class=\"%s\">", "container"));
            assertEquals(reader.readLine(), String.format("<div id=\"%s\">", "accordion"));

            // ensure following lines are not empty - if they are, this means that the
            // files did not get parsed correctly and therefore did not write into the file.
            assertNotNull(reader.readLine());
            assertNotNull(reader.readLine());

        } catch (final IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            // Cleanup
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            if (tempFile2 != null && tempFile2.exists()) {
                tempFile2.delete();
            }
            if (tempFileTOC != null && tempFileTOC.exists()) {
                tempFileTOC.delete();
            }
        }
    }

    /**
     * Test of generateLink method, of class TOCGenerator.
     */
    @Test
    public void testGenerateLink() {
        System.out.println("generateLink");

        final String title = "click here";
        final String url = "www.link.com/to/follow.aspx";
        final String expResult = String.format("[%s](%s)", title, url);
        final String result = TOCGenerator.generateLink(title, url);
        assertEquals(result, expResult);

        final String title1 = null;
        final String url1 = null;
        final String expResult1 = String.format("[%s](%s)", title1, url1);
        final String result1 = TOCGenerator.generateLink(title1, url1);
        assertEquals(result1, expResult1);
    }

    /**
     * Test of generateHTMLLink method, of class TOCGenerator.
     */
    @Test
    public void testGenerateHTMLLink() {
        System.out.println("generateHTMLLink");

        final String title = "click here";
        final String url = "www.link.com/to/follow.aspx";
        final String expResult = String.format("<a href=\"%s\">%s</a><br/>", url, title);
        final String result = TOCGenerator.generateHTMLLink(title, url);
        assertEquals(result, expResult);

        final String title1 = null;
        final String url1 = null;
        final String expResult1 = String.format("<a href=\"%s\">%s</a><br/>", url1, title1);
        final String result1 = TOCGenerator.generateHTMLLink(title1, url1);
        assertEquals(result1, expResult1);
    }

    /**
     * Test of writeAccordionItem method, of class TOCGenerator. TODO: This test
     * method is not implemented as the implementation of the method is not
     * finalised.
     */
    @Test
    public void testWriteAccordionItem() {
        System.out.println("writeAccordionItem");
        System.out.println("TODO: This test method is not implemented as the "
                + "implementation of the method is not finalised");
    }

    /**
     * Test of writeText method, of class TOCGenerator.
     */
    @Test
    public void testWriteText() {
        System.out.println("writeText");
        File tempFile = null;
        try {
            tempFile = File.createTempFile("testfile", ".xml");

            // contents of file
            final String text = "This should be written into the file.\n";
            final String text2 = "this is the second line\n";
            final String text3 = "</> this will be the final line </>";
            final List<String> fileContents = new ArrayList<>();

            fileContents.add(text);
            fileContents.add(text2);
            fileContents.add(text3);

            // try with resources
            try (final FileWriter fw = new FileWriter(tempFile)) {
                fileContents.forEach(str -> {
                    TOCGenerator.writeText(fw, str);
                });
            }

            BufferedReader reader = new BufferedReader(new FileReader(tempFile));
            String line;

            int linecount = 0;
            while ((line = reader.readLine()) != null) {
                assertEquals(line, fileContents.get(linecount++).replace("\n", ""));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            // Cleanup
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Test(expectedExceptions = IOException.class)
    public void testWriteTextFail() throws IOException {
        System.out.println("testWriteText Fail");

        File tempFile = new File("invalid/path");
        // try with resources
        try (final FileWriter fw = new FileWriter(tempFile)) {
            TOCGenerator.writeText(fw, "text");
        }
    }

    /**
     * Test of writeItem method, of class TOCGenerator.
     */
    @Test
    public void testWriteItem() {
        System.out.println("writeItem");
        File tempFile = null;
        try {
            tempFile = File.createTempFile("testfile", ".xml");

            // contents of file
            final String text = "This should be written into the file.\n";
            final String text2 = "this is the second line\n";
            final String text3 = "</> this will be the final line </>";
            final List<String> fileContents = new ArrayList<>();

            fileContents.add(text);
            fileContents.add(text2);
            fileContents.add(text3);

            // try with resources
            try (final FileWriter fw = new FileWriter(tempFile)) {
                fileContents.forEach(str -> {
                    TOCGenerator.writeItem(fw, str, 0);
                });
            }

            BufferedReader reader = new BufferedReader(new FileReader(tempFile));
            String line;

            int linecount = 0;
            while ((line = reader.readLine()) != null) {
                assertEquals(line, "" + fileContents.get(linecount++).replace("\n", ""));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            // Cleanup
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Test of writeItem method, of class TOCGenerator.
     */
    @Test
    public void testWriteItemMultipleIndents() {
        System.out.println("writeItem");
        File tempFile = null;
        try {
            tempFile = File.createTempFile("testfile", ".xml");

            // contents of file
            final String text = "This should be written into the file.\n";
            final String text2 = "this is the second line\n";
            final String text3 = "</> this will be the final line </>";
            final List<String> fileContents = new ArrayList<>();

            fileContents.add(text);
            fileContents.add(text2);
            fileContents.add(text3);

            // try with resources
            try (final FileWriter fw = new FileWriter(tempFile)) {
                fileContents.forEach(str -> {
                    TOCGenerator.writeItem(fw, str, 2);
                });
            }

            BufferedReader reader = new BufferedReader(new FileReader(tempFile));
            String line;

            int linecount = 0;
            while ((line = reader.readLine()) != null) {
                assertEquals(line, "    " + fileContents.get(linecount++).replace("\n", ""));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            // Cleanup
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Test of writeItem method, of class TOCGenerator.
     */
    @Test(expectedExceptions = IOException.class)
    public void testWriteItemFail() throws IOException {
        System.out.println("testWriteItem Fail");

        File tempFile = new File("invalid/path");
        // try with resources
        try (final FileWriter fw = new FileWriter(tempFile)) {
            TOCGenerator.writeItem(fw, "text", 0);
        }
    }

}
