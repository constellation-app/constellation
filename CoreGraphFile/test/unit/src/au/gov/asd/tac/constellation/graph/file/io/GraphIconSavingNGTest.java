/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.file.io;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.gui.TextIoProgress;
import au.gov.asd.tac.constellation.utilities.icon.AnalyticIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.DefaultCustomIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.utilities.icon.ImageIconData;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openide.util.ImageUtilities;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Graph Saving Test with custom icons stored in star file.
 *
 * @author OrionsGuardian
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class GraphIconSavingNGTest extends ConstellationTest {

    private int attrX, attrY, attrZ;
    private int vxId1, vxId2, vxId3, vxId4, vxId5, vxId6, vxId7;
    private int txId1, txId2, txId3, txId4, txId5;
    private int vNameAttr, tNameAttr, vSelAttr, tSelAttr, vIconAttr;
    private Graph graph;

    final static String TEST_ICON_NAME = "Category1.TestIcon1";
    
    @BeforeMethod
    public void setUpMethod() throws Exception {
        graph = new DualGraph(null);
        WritableGraph wg = graph.getWritableGraph("add", true);
        try {
            attrX = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", 0.0, null);
            if (attrX == Graph.NOT_FOUND) {
                fail();
            }

            attrY = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", 0.0, null);
            if (attrY == Graph.NOT_FOUND) {
                fail();
            }

            attrZ = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z", "z", 0.0, null);
            if (attrZ == Graph.NOT_FOUND) {
                fail();
            }

            vNameAttr = wg.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "name", "descr", "", null);
            if (vNameAttr == Graph.NOT_FOUND) {
                fail();
            }

            tNameAttr = wg.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "name", "descr", "", null);
            if (tNameAttr == Graph.NOT_FOUND) {
                fail();
            }

            vSelAttr = wg.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "selected", false, null);
            if (vSelAttr == Graph.NOT_FOUND) {
                fail();
            }

            tSelAttr = wg.addAttribute(GraphElementType.TRANSACTION, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "selected", false, null);
            if (tSelAttr == Graph.NOT_FOUND) {
                fail();
            }

            vIconAttr = wg.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "icon", "icon", "Unknown", null);
            if (vIconAttr == Graph.NOT_FOUND) {
                fail();
            }
            // Note that vIconAttr is not being set to a correct representation of the icon attribute
            // It should be using IconAttributeDescription.ATTRIBUTE_NAME, but that is a Visual Schema class which is not accessible from this module
            // The AttributeRegistry, when testing from this module, only contains the base type attributes.
            // Fortunately, the code that is being tested doesn't require specific access to the schema class,
            // so we can perform the tests using the string type to store the icon name data            

            vxId1 = wg.addVertex();
            wg.setFloatValue(attrX, vxId1, 1.0f);
            wg.setFloatValue(attrY, vxId1, 1.1f);
            wg.setBooleanValue(vSelAttr, vxId1, false);
            wg.setStringValue(vNameAttr, vxId1, "name1");
            vxId2 = wg.addVertex();
            wg.setFloatValue(attrX, vxId2, 2.0f);
            wg.setFloatValue(attrY, vxId2, 2.2f);
            wg.setBooleanValue(vSelAttr, vxId2, true);
            wg.setStringValue(vNameAttr, vxId2, "name2");
            vxId3 = wg.addVertex();
            wg.setFloatValue(attrX, vxId3, 3.0f);
            wg.setFloatValue(attrY, vxId3, 3.3f);
            wg.setBooleanValue(vSelAttr, vxId3, false);
            wg.setStringValue(vNameAttr, vxId3, "name3");
            vxId4 = wg.addVertex();
            wg.setFloatValue(attrX, vxId4, 4.0f);
            wg.setFloatValue(attrY, vxId4, 4.4f);
            wg.setBooleanValue(vSelAttr, vxId4, true);
            wg.setStringValue(vNameAttr, vxId4, "name4");
            vxId5 = wg.addVertex();
            wg.setFloatValue(attrX, vxId5, 5.0f);
            wg.setFloatValue(attrY, vxId5, 5.5f);
            wg.setBooleanValue(vSelAttr, vxId5, false);
            wg.setStringValue(vNameAttr, vxId5, "name5");
            vxId6 = wg.addVertex();
            wg.setFloatValue(attrX, vxId6, 6.0f);
            wg.setFloatValue(attrY, vxId6, 6.60f);
            wg.setBooleanValue(vSelAttr, vxId6, true);
            wg.setStringValue(vNameAttr, vxId6, "name6");
            vxId7 = wg.addVertex();
            wg.setFloatValue(attrX, vxId7, 7.0f);
            wg.setFloatValue(attrY, vxId7, 7.7f);
            wg.setBooleanValue(vSelAttr, vxId7, false);
            wg.setStringValue(vNameAttr, vxId7, "name7");
            wg.setStringValue(vIconAttr, vxId7, "test_bagel_blue");

            txId1 = wg.addTransaction(vxId1, vxId2, false);
            wg.setBooleanValue(tSelAttr, txId1, false);
            wg.setStringValue(tNameAttr, txId1, "name101");
            txId2 = wg.addTransaction(vxId1, vxId3, false);
            wg.setBooleanValue(tSelAttr, txId2, true);
            wg.setStringValue(tNameAttr, txId2, "name102");
            txId3 = wg.addTransaction(vxId2, vxId4, true);
            wg.setBooleanValue(tSelAttr, txId3, false);
            wg.setStringValue(tNameAttr, txId3, "name103");
            txId4 = wg.addTransaction(vxId4, vxId2, true);
            wg.setBooleanValue(tSelAttr, txId4, true);
            wg.setStringValue(tNameAttr, txId4, "name104");
            txId5 = wg.addTransaction(vxId5, vxId6, false);
            wg.setBooleanValue(tSelAttr, txId5, false);
            wg.setStringValue(tNameAttr, txId5, "name105");
        } finally {
            wg.commit();
        }
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    @Test
    public void writeThenReadIconGraphTest() throws Exception {
        final String name = "tmp1";
        File graphFile = File.createTempFile(name, ".star");

        try (MockedStatic<DefaultCustomIconProvider> defaultCustomIconProviderMock = Mockito.mockStatic(DefaultCustomIconProvider.class);) {
            URL iconResourcePath = GraphIconSavingNGTest.class.getResource("resources/");
            File resourcePathFile = new File(iconResourcePath.toURI());
            defaultCustomIconProviderMock.when(() -> DefaultCustomIconProvider.getIconDirectory()).thenReturn(resourcePathFile);
            defaultCustomIconProviderMock.when(() -> DefaultCustomIconProvider.containsIcon(Mockito.any())).thenCallRealMethod();
            defaultCustomIconProviderMock.when(() -> DefaultCustomIconProvider.reloadIcons()).thenCallRealMethod();
            defaultCustomIconProviderMock.when(() -> DefaultCustomIconProvider.loadIcons()).thenCallRealMethod();
            
            // Create a test icon to be added and saved with the graph data
            final ConstellationColor ICON_COLOR = ConstellationColor.BLUEBERRY;
            final ConstellationIcon ICON_BACKGROUND = DefaultIconProvider.FLAT_SQUARE;
            final ConstellationIcon ICON_SYMBOL = AnalyticIconProvider.STAR;

            ConstellationIcon icon = new ConstellationIcon.Builder(TEST_ICON_NAME,
                    new ImageIconData((BufferedImage) ImageUtilities.mergeImages(
                            ICON_BACKGROUND.buildBufferedImage(16, ICON_COLOR.getJavaColor()),
                            ICON_SYMBOL.buildBufferedImage(16), 0, 0)))
                    .build();
            icon.setEditable(true);
            System.out.println("===== INITIALISE TEST ENVIRONMENT =====");
            
            prepareFileDir(resourcePathFile);
            DefaultCustomIconProvider.reloadIcons();
            
            System.out.println(" __ Defining custom Icon: " + icon.getExtendedName()+ " __");
            IconManager.addIcon(icon);

            // there should be 2 custom icons in the resource folder
            assertEquals(resourcePathFile.listFiles().length, 2);
            DefaultCustomIconProvider.reloadIcons();
            
            System.out.println(" __ Add custom icon to vertex 0 __");
            WritableGraph wg = graph.getWritableGraph("add", true);
            try {
                int vertex0 = wg.getVertex(0);
                wg.setStringValue(vIconAttr, vertex0, icon.getName());
            } finally {
                wg.commit();
            }
            System.out.println(" __ Write a sample graph with icons to a star file __");
            
            final ReadableGraph rg = graph.getReadableGraph();
            try {
                GraphJsonWriter writer = new GraphJsonWriter();
                writer.writeGraphToZip(rg, graphFile.getPath(), new TextIoProgress(false));
            } finally {
                rg.release();
            }
            System.out.print("\nTEST: Confirm the star file has been created: ");
            assertTrue("graph(star) file created", graphFile.exists());
            System.out.println(" *PASSED*\n");

            boolean containsIcons = false;
            // confirm the presence of an Icons folder containing an image file.
            try (ZipFile zFile = new ZipFile(graphFile.getPath())) {
                for (final ZipEntry entry : Collections.list(zFile.entries())) {
                    // Check for Icon entries in the source star/zip file
                    if (entry.getName().startsWith(DefaultCustomIconProvider.USER_ICON_DIR) && !entry.isDirectory()) {
                        containsIcons = true;
                    }
                }
            }
            System.out.print("\nTEST: Confirm the graph(star) file has Icons included in the archive: ");
            assertTrue(containsIcons);
            System.out.println(" *PASSED*\n");
            
            System.out.println(" __ Reset the test environment and Reset the icon cache __");
            prepareFileDir(resourcePathFile);
            DefaultCustomIconProvider.reloadIcons();

            System.out.print("\nTEST: Confirm only 1 custom icon exists after the reset: ");
            
            Set<String> iconSet = IconManager.getIconNames(true);
            assertEquals(1, iconSet.size());
            assertEquals(1, resourcePathFile.listFiles().length);
            System.out.println(" *PASSED*\n");

            System.out.print("TEST: Confirm there is no custom icon entry for: " + icon.getName());            
            assertTrue(!IconManager.iconExists(icon.getName()));
            System.out.println(" :  *PASSED*\n");
            
            System.out.println(" __ Read the star file and load in any missing icons __");

            final Graph newGraph = new GraphJsonReader().readGraphZip(graphFile, new TextIoProgress(false));
            
            ReadableGraph rgr = newGraph.getReadableGraph();
            try {
                System.out.print("TEST: Confirm we have read back the correct graph: ");
                assertEquals("num nodes", 7, rgr.getVertexCount());
                assertEquals("num transactions", 5, rgr.getTransactionCount());
                assertTrue("node: 'name1' has correct icon", nodeIconFound(rgr, "name1", TEST_ICON_NAME));
                System.out.println(" *PASSED*\n");
            } finally {
                rgr.release();
            }

            System.out.print("TEST: Confirm correct number of icon files, and correct icon entries in cache: ");            
            assertEquals(2, resourcePathFile.listFiles().length);
            assertTrue(IconManager.iconExists(icon.getName()));
            System.out.println(" *PASSED*\n");
        }        
        graphFile.delete();
        System.out.println("===== <<<< TEST COMPLETE >>>> =====");

    }
    
    // determine whether the node of the specified name exists in the graph and contains an icon value
    private boolean nodeIconFound(ReadableGraph graph, String base_name, String iconName) {
        int nameAttrId = graph.getAttribute(GraphElementType.VERTEX, "name");
        int iconAttrId = graph.getAttribute(GraphElementType.VERTEX, "icon");
        boolean found = false;
        for (int i = 0; i < graph.getVertexCount(); i++) {
            int id = graph.getVertex(i);
            String name = graph.getStringValue(nameAttrId, id);
            if (base_name.equals(name)) {
                String loadedIconName = graph.getStringValue(iconAttrId, id);
                if (iconName.equals(loadedIconName)) {
                    found = true;
                }
            }
        }
        return found;
    }

    public void prepareFileDir(final File testFile){
        // reset the icon resource folder to only contain the test_bagel_blue.png file
        List<String> filenames = new ArrayList<>();
        for(File f : testFile.listFiles()){
            String path = f.getAbsolutePath();
            filenames.add(path);
        }
        for(String path : filenames){
            if (!path.contains("test_bagel_blue")){
                File f = new File(path);
                f.delete();
            }
        }
    }
    
}

