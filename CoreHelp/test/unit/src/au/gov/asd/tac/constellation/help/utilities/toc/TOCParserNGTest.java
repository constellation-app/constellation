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
package au.gov.asd.tac.constellation.help.utilities.toc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author aldebaran30701
 */
public class TOCParserNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
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

    /**
     * Test of parse method, of class TOCParser.
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    @Test
    public void testParse() throws SAXException, IOException, ParserConfigurationException {
        System.out.println("parse single file");

        final String fileContents = """
                                    <?xml version="1.0" encoding="UTF-8"?>
                                    <!DOCTYPE toc PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 2.0//EN" "http://java.sun.com/products/javahelp/toc_2_0.dtd">
                                    <toc version="2.0">
                                        <tocitem text="Views" mergetype="javax.help.SortMerge">
                                            <tocitem text="Layers View" mergetype="javax.help.SortMerge">
                                                <tocitem text="Layers View" target="au.gov.asd.tac.constellation.views.layers.LayersViewTopComponent" />
                                            </tocitem>
                                        </tocitem>
                                    </toc>
                                    """;

        File tempFile = null;
        try {
            tempFile = File.createTempFile("testfile", ".xml");

            // try with resources
            try (final FileWriter fw = new FileWriter(tempFile)) {
                fw.append(fileContents);
            }

            final TreeNode root = new TreeNode(new TOCItem("root", ""));
            TOCParser.parse(tempFile, root);

            final TreeNode child1 = (TreeNode) root.getChildren().get(0); // Views
            final TreeNode child11 = (TreeNode) child1.getChildren().get(0); // Layers View
            final TreeNode child111 = (TreeNode) child11.getChildren().get(0); // Layers View (Experimental)

            final TreeNode expectedChild1 = new TreeNode(new TOCItem("Views", ""));
            final TreeNode expectedChild11 = new TreeNode(new TOCItem("Layers View", ""));
            final TreeNode expectedChild111 = new TreeNode(new TOCItem("Layers View", "au.gov.asd.tac.constellation.views.layers.LayersViewTopComponent"));

            // Check amount of children
            assertEquals(root.getChildren().size(), 1);
            assertEquals(child1.getChildren().size(), 1);
            assertEquals(child11.getChildren().size(), 1);
            assertEquals(child111.getChildren().size(), 0);

            // Check child equality
            assertEquals(child1, expectedChild1);
            assertEquals(child11, expectedChild11);
            assertEquals(child111, expectedChild111);

        } finally {
            // Cleanup
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }

    }

    /**
     * Test of parse method, of class TOCParser.
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    @Test
    public void testParseNonNested() throws SAXException, IOException, ParserConfigurationException {
        System.out.println("parse Non nested single file");

        final String fileContents = """
                                    <?xml version="1.0" encoding="UTF-8"?>
                                    <!DOCTYPE toc PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 2.0//EN" "http://java.sun.com/products/javahelp/toc_2_0.dtd">
                                    <toc version="2.0">
                                        <tocitem text="Views" mergetype="javax.help.SortMerge">
                                            <tocitem text="Layers View" mergetype="javax.help.SortMerge">
                                                <tocitem text="Layers View" target="au.gov.asd.tac.constellation.views.layers.LayersViewTopComponent" />
                                            </tocitem>
                                        </tocitem>
                                        <tocitem text="Jupyter">
                                            <tocitem text="About The Jupyter Notebook Server" target="au.gov.asd.tac.constellation.utilities.jupyter"/>
                                            <tocitem text="About The Constellation REST Server" target="au.gov.asd.tac.constellation.utilities.rest"/>
                                        </tocitem>
                                    </toc>
                                    """;

        File tempFile = null;
        try {
            tempFile = File.createTempFile("testfile", ".xml");

            // try with resources
            try (final FileWriter fw = new FileWriter(tempFile)) {
                fw.append(fileContents);
            }

            final TreeNode root = new TreeNode(new TOCItem("root", ""));
            TOCParser.parse(tempFile, root);

            final TreeNode child1 = (TreeNode) root.getChildren().get(0); // Views
            final TreeNode child11 = (TreeNode) child1.getChildren().get(0); // Layers View
            final TreeNode child111 = (TreeNode) child11.getChildren().get(0); // Layers View (Experimental)
            final TreeNode child2 = (TreeNode) root.getChildren().get(1); // Jupyter
            final TreeNode child21 = (TreeNode) child2.getChildren().get(0); // About The Jupyter Notebook Server
            final TreeNode child22 = (TreeNode) child2.getChildren().get(1); // About The Constellation REST Server

            final TreeNode expectedChild1 = new TreeNode(new TOCItem("Views", ""));
            final TreeNode expectedChild11 = new TreeNode(new TOCItem("Layers View", ""));
            final TreeNode expectedChild111 = new TreeNode(new TOCItem("Layers View", "au.gov.asd.tac.constellation.views.layers.LayersViewTopComponent"));
            final TreeNode expectedChild2 = new TreeNode(new TOCItem("Jupyter", "")); // Jupyter
            final TreeNode expectedChild21 = new TreeNode(new TOCItem("About The Jupyter Notebook Server", "au.gov.asd.tac.constellation.utilities.jupyter")); // About The Jupyter Notebook Server
            final TreeNode expectedChild22 = new TreeNode(new TOCItem("About The Constellation REST Server", "au.gov.asd.tac.constellation.utilities.rest")); // About The Constellation REST Server

            // Check amount of children
            assertEquals(root.getChildren().size(), 2);
            assertEquals(child1.getChildren().size(), 1);
            assertEquals(child11.getChildren().size(), 1);
            assertEquals(child111.getChildren().size(), 0);

            assertEquals(child2.getChildren().size(), 2);
            assertEquals(child21.getChildren().size(), 0);
            assertEquals(child22.getChildren().size(), 0);

            // Check child equality
            assertEquals(child1, expectedChild1);
            assertEquals(child11, expectedChild11);
            assertEquals(child111, expectedChild111);

            assertEquals(child2, expectedChild2);
            assertEquals(child21, expectedChild21);
            assertEquals(child22, expectedChild22);

        } finally {
            // Cleanup
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }


    /**
     * Test of parse method, of class TOCParser.
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    @Test
    public void testParseMultipleFiles() throws SAXException, IOException, ParserConfigurationException {
        System.out.println("parse multiple file");

        final String fileContents = """
                                    <?xml version="1.0" encoding="UTF-8"?>
                                    <!DOCTYPE toc PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 2.0//EN" "http://java.sun.com/products/javahelp/toc_2_0.dtd">
                                    <toc version="2.0">
                                        <tocitem text="Views" mergetype="javax.help.SortMerge">
                                            <tocitem text="Layers View" mergetype="javax.help.SortMerge">
                                                <tocitem text="Layers View" target="au.gov.asd.tac.constellation.views.layers.LayersViewTopComponent" />
                                            </tocitem>
                                        </tocitem>
                                    </toc>
                                    """;
        final String fileContents2 = """
                                     <?xml version="1.0" encoding="UTF-8"?>
                                     <!DOCTYPE toc PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 2.0//EN" "http://java.sun.com/products/javahelp/toc_2_0.dtd">
                                     <toc version="2.0">
                                         <tocitem text="Views" mergetype="javax.help.SortMerge">
                                             <tocitem text="Notes View" mergetype="javax.help.SortMerge">
                                                 <tocitem text="Notes View" target="au.gov.asd.tac.constellation.views.notes.NotesViewTopComponent" />
                                             </tocitem>
                                         </tocitem>
                                     </toc>
                                     """;
        final String fileContents3 = """
                                     <?xml version="1.0" encoding="UTF-8"?>
                                     <!DOCTYPE toc PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 2.0//EN" "http://java.sun.com/products/javahelp/toc_2_0.dtd">
                                     <toc version="2.0">
                                         <tocitem text="Views" mergetype="javax.help.SortMerge">
                                             <tocitem text="Layers View" mergetype="javax.help.SortMerge">
                                                 <tocitem text="Layers View Extra" target="au.gov.asd.tac.constellation.views.layers.LayersViewTopComponent.Extra" />
                                                 <tocitem text="Layers View Extra2" target="au.gov.asd.tac.constellation.views.layers.LayersViewTopComponent.Extra2" />
                                             </tocitem>
                                         </tocitem>
                                     </toc>
                                     """;
        final String fileContents4 = """
                                     <?xml version="1.0" encoding="UTF-8"?>
                                     <!DOCTYPE toc PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 2.0//EN" "http://java.sun.com/products/javahelp/toc_2_0.dtd">
                                     <toc version="2.0">
                                         <tocitem text="Features" mergetype="javax.help.SortMerge">
                                             <tocitem text="Selection" mergetype="javax.help.SortMerge">
                                                 <tocitem text="Node Selection" target="nodeselectiontarget" />
                                                 <tocitem text="Transaction Selection" target="transactionselectiontarget" />
                                             </tocitem>
                                         </tocitem>
                                     </toc>
                                     """;

        File tempFile = null;
        File tempFile2 = null;
        File tempFile3 = null;
        File tempFile4 = null;
        try {
            tempFile = File.createTempFile("testfile", ".xml");
            tempFile2 = File.createTempFile("testfile2", ".xml");
            tempFile3 = File.createTempFile("testfile3", ".xml");
            tempFile4 = File.createTempFile("testfile4", ".xml");

            // try with resources
            try (final FileWriter fw = new FileWriter(tempFile)) {
                fw.append(fileContents);
            }
            // try with resources
            try (final FileWriter fw = new FileWriter(tempFile2)) {
                fw.append(fileContents2);
            }
            // try with resources
            try (final FileWriter fw = new FileWriter(tempFile3)) {
                fw.append(fileContents3);
            }
            // try with resources
            try (final FileWriter fw = new FileWriter(tempFile4)) {
                fw.append(fileContents4);
            }

            final TreeNode root = new TreeNode(new TOCItem("root", ""));

            // Parse files into root node
            TOCParser.parse(tempFile, root);
            TOCParser.parse(tempFile2, root);
            TOCParser.parse(tempFile3, root);
            TOCParser.parse(tempFile4, root);

            final TreeNode child1a = (TreeNode) root.getChildren().get(0); // Views
            final TreeNode child1b = (TreeNode) root.getChildren().get(1); // Features

            final TreeNode child11a = (TreeNode) child1a.getChildren().get(0); // Layers View
            final TreeNode child111a = (TreeNode) child11a.getChildren().get(0); // Layers View link
            final TreeNode child111aa = (TreeNode) child11a.getChildren().get(1); // Layers View Extra
            final TreeNode child111aaa = (TreeNode) child11a.getChildren().get(2); // Layers View Extra2

            final TreeNode child11b = (TreeNode) child1a.getChildren().get(1); // Notes View
            final TreeNode child111b = (TreeNode) child11b.getChildren().get(0); // Notes View link

            final TreeNode child11c = (TreeNode) child1b.getChildren().get(0); // Selection
            final TreeNode child111c = (TreeNode) child11c.getChildren().get(0); // node selection link
            final TreeNode child111d = (TreeNode) child11c.getChildren().get(1); // transaction selection link

            final TreeNode expectedChild1a = new TreeNode(new TOCItem("Views", ""));
            final TreeNode expectedChild1b = new TreeNode(new TOCItem("Features", ""));
            final TreeNode expectedChild11a = new TreeNode(new TOCItem("Layers View", ""));
            final TreeNode expectedChild11b = new TreeNode(new TOCItem("Notes View", ""));
            final TreeNode expectedChild11c = new TreeNode(new TOCItem("Selection", ""));
            final TreeNode expectedChild111a = new TreeNode(new TOCItem("Layers View", "au.gov.asd.tac.constellation.views.layers.LayersViewTopComponent"));
            final TreeNode expectedChild111aa = new TreeNode(new TOCItem("Layers View Extra", "au.gov.asd.tac.constellation.views.layers.LayersViewTopComponent.Extra"));
            final TreeNode expectedChild111aaa = new TreeNode(new TOCItem("Layers View Extra2", "au.gov.asd.tac.constellation.views.layers.LayersViewTopComponent.Extra2"));
            final TreeNode expectedChild111b = new TreeNode(new TOCItem("Notes View", "au.gov.asd.tac.constellation.views.notes.NotesViewTopComponent"));
            final TreeNode expectedChild111c = new TreeNode(new TOCItem("Node Selection", "nodeselectiontarget"));
            final TreeNode expectedChild111d = new TreeNode(new TOCItem("Transaction Selection", "transactionselectiontarget"));

            // Check amount of children
            assertEquals(root.getChildren().size(), 2);
            assertEquals(child1a.getChildren().size(), 2);
            assertEquals(child1b.getChildren().size(), 1);
            assertEquals(child11a.getChildren().size(), 3);
            assertEquals(child111a.getChildren().size(), 0);
            assertEquals(child111aa.getChildren().size(), 0);
            assertEquals(child111aaa.getChildren().size(), 0);
            assertEquals(child11b.getChildren().size(), 1);
            assertEquals(child111b.getChildren().size(), 0);
            assertEquals(child11c.getChildren().size(), 2);
            assertEquals(child111c.getChildren().size(), 0);
            assertEquals(child111d.getChildren().size(), 0);

            // Check data equality
            assertEquals(child1a, expectedChild1a);
            assertEquals(child1b, expectedChild1b);
            assertEquals(child11a, expectedChild11a);
            assertEquals(child11b, expectedChild11b);
            assertEquals(child11c, expectedChild11c);
            assertEquals(child111a, expectedChild111a);
            assertEquals(child111aa, expectedChild111aa);
            assertEquals(child111aaa, expectedChild111aaa);
            assertEquals(child111b, expectedChild111b);
            assertEquals(child111c, expectedChild111c);
            assertEquals(child111d, expectedChild111d);

        } finally {
            // Cleanup
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            if (tempFile2 != null && tempFile2.exists()) {
                tempFile2.delete();
            }
            if (tempFile3 != null && tempFile3.exists()) {
                tempFile3.delete();
            }
            if (tempFile4 != null && tempFile4.exists()) {
                tempFile4.delete();
            }
        }
    }

    /**
     * Test of parse method, of class TOCParser.
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    @Test
    public void testParseNull() throws SAXException, IOException, ParserConfigurationException {
        System.out.println("parse null file");

        File tempFile = null;
        final TreeNode root = new TreeNode(new TOCItem("root", ""));
        final int childrenBefore = root.getChildren().size();

        TOCParser.parse(tempFile, root);

        // Ensure root has no extra elements
        assertEquals(root, new TreeNode(new TOCItem("root", "")));
        assertEquals(root.getChildren().size(), childrenBefore);
    }

    /**
     * Test of parse method, of class TOCParser. Expects an exception to be
     * thrown when the path is invalid
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    @Test(expectedExceptions = FileNotFoundException.class)
    public void testParseFail() throws SAXException, IOException, ParserConfigurationException {
        System.out.println("parse failed file");

        File tempFile = new File("invalid/path");
        final TreeNode root = new TreeNode(new TOCItem("root", ""));

        TOCParser.parse(tempFile, root);
    }
}
