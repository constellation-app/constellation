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
package au.gov.asd.tac.constellation.graph.schema.visual.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.preferences.GraphPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.awt.Color;
import java.util.BitSet;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.util.Pair;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mockStatic;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class BlazeUtilitiesNGTest {
    
    private static final Logger LOGGER = Logger.getLogger(BlazeUtilitiesNGTest.class.getName());
    
    private final FxRobot robot = new FxRobot();
    
    private Schema schema;
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    
    private int vertexBlazeAttribute;
    private int vertexSelectedAttribute;
    
    public BlazeUtilitiesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
         try {
            FxToolkit.cleanupStages();
        } catch (final TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);
        
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();
        
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        vertexBlazeAttribute = VisualConcept.VertexAttribute.BLAZE.ensure(graph);
        
        graph.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, true);
        
        graph.setStringValue(vertexBlazeAttribute, vxId3, "45;Blue");
        graph.setStringValue(vertexBlazeAttribute, vxId4, "60;Red");
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getSelection method, of class BlazeUtilities. Blazes on the graph
     */
    @Test
    public void testGetSelection() {
        System.out.println("getSelection");
        
        final Graph g = new DualGraph(schema, graph);
        
        final Pair<BitSet, ConstellationColor> result = BlazeUtilities.getSelection(g, null);
        final BitSet resultBitSet = result.getKey();
        final ConstellationColor resultColor = result.getValue();
        
        assertEquals(resultBitSet.cardinality(), 3);
        assertTrue(resultBitSet.get(vxId1));
        assertFalse(resultBitSet.get(vxId2));
        assertTrue(resultBitSet.get(vxId3));
        assertTrue(resultBitSet.get(vxId4));
        assertEquals(resultColor, ConstellationColor.BLUE);
    }
    
    /**
     * Test of getSelection method, of class BlazeUtilities. No blazes on the graph
     */
    @Test
    public void testGetSelectionNoBlazes() {
        System.out.println("getSelectionNoBlazes");
        
        graph.setObjectValue(vertexBlazeAttribute, vxId3, null);
        graph.setObjectValue(vertexBlazeAttribute, vxId4, null);
        
        final Graph g = new DualGraph(schema, graph);
        
        final Pair<BitSet, ConstellationColor> result = BlazeUtilities.getSelection(g, null);
        final BitSet resultBitSet = result.getKey();
        final ConstellationColor resultColor = result.getValue();
        
        assertEquals(resultBitSet.cardinality(), 3);
        assertTrue(resultBitSet.get(vxId1));
        assertFalse(resultBitSet.get(vxId2));
        assertTrue(resultBitSet.get(vxId3));
        assertTrue(resultBitSet.get(vxId4));
        assertEquals(resultColor, ConstellationColor.LIGHT_BLUE);
    }
    
    /**
     * Test of getSelection method, of class BlazeUtilities. Color input added
     */
    @Test
    public void testGetSelectionColorInput() {
        System.out.println("getSelectionColorInput");
        
        final Graph g = new DualGraph(schema, graph);
        
        final Pair<BitSet, ConstellationColor> result = BlazeUtilities.getSelection(g, ConstellationColor.BANANA);
        final BitSet resultBitSet = result.getKey();
        final ConstellationColor resultColor = result.getValue();
        
        assertEquals(resultBitSet.cardinality(), 3);
        assertTrue(resultBitSet.get(vxId1));
        assertFalse(resultBitSet.get(vxId2));
        assertTrue(resultBitSet.get(vxId3));
        assertTrue(resultBitSet.get(vxId4));
        assertEquals(resultColor, ConstellationColor.BANANA);
    }

    /**
     * Test of savePreset method, of class BlazeUtilities. One parameter implementation
     * @throws java.util.prefs.BackingStoreException
     */
    @Test
    public void testSavePresetOneParameter() throws BackingStoreException {
        System.out.println("savePresetOneParameter");
        
        // set up by ensuring Preferences for this test do not exist
        final Preferences p = Preferences.userNodeForPackage(BlazeUtilitiesNGTest.class);
        
        // keep the original application preference for blaze preset defaults so it can be restored later
        final String defaultBlazePresetColors = GraphPreferenceKeys.getBlazePresetsColorsDefault();
        
        try (final MockedStatic<BlazeUtilities> blazeUtilitiesMockedStatic = mockStatic(BlazeUtilities.class, Mockito.CALLS_REAL_METHODS)) {
            blazeUtilitiesMockedStatic.when(() -> BlazeUtilities.getGraphPreferences()).thenReturn(p);                     
            
            p.put(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.getBlazePresetsColorsDefault());
            final String presetsBefore = p.get(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.getBlazePresetsColorsDefault());
            assertEquals(presetsBefore, "#FF0000;#0000FF;#FFFF00;");
            
            // add a color to end of default list of presets
            BlazeUtilities.savePreset(Color.CYAN);            
            final String presetsAfter1 = p.get(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.getBlazePresetsColorsDefault());
            assertEquals(presetsAfter1, "#FF0000;#0000FF;#FFFF00;#00ffff;null;null;null;null;null;null;");
            
            // add a color to first null value in presets
            BlazeUtilities.savePreset(Color.GREEN);            
            final String presetsAfter2 = p.get(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.getBlazePresetsColorsDefault());
            assertEquals(presetsAfter2, "#FF0000;#0000FF;#FFFF00;#00ffff;#00ff00;null;null;null;null;null;");
            
            // fill up list of presets
            for (int i = 0; i < 5; i++) {
                BlazeUtilities.savePreset(Color.MAGENTA);                                      
            }                    
            final String presetsAfter3 = p.get(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.getBlazePresetsColorsDefault());
            assertEquals(presetsAfter3, "#FF0000;#0000FF;#FFFF00;#00ffff;#00ff00;#ff00ff;#ff00ff;#ff00ff;#ff00ff;#ff00ff;");
            
            // add a color to after the preset list has been filled
            // The same list should remain since the behaviour has been changed to not replace the last preset automatically when there are 10
            BlazeUtilities.savePreset(Color.WHITE);
            final String presetsAfter4 = p.get(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.getBlazePresetsColorsDefault());
            assertEquals(presetsAfter4, "#FF0000;#0000FF;#FFFF00;#00ffff;#00ff00;#ff00ff;#ff00ff;#ff00ff;#ff00ff;#ff00ff;");
        } finally {
            // clean up, first remove Preferences nodes this test plays with
            p.removeNode();
            // and set the graph Preference back to its original setting
            GraphPreferenceKeys.setBlazePresetsColorsDefault(defaultBlazePresetColors);
        }
        
    }

    /**
     * Test of savePreset method, of class BlazeUtilities. Two parameter implementation
     * @throws java.util.prefs.BackingStoreException
     */
    @Test
    public void testSavePresetTwoParameters() throws BackingStoreException {
        System.out.println("savePresetTwoParameters");
        
        // set up by ensuring Preferences for this test do not exist
        final Preferences p = Preferences.userNodeForPackage(BlazeUtilitiesNGTest.class);
        
        // keep the original application preference for blaze preset defaults so it can be restored later
        final String defaultBlazePresetColors = GraphPreferenceKeys.getBlazePresetsColorsDefault();
        
        try (final MockedStatic<BlazeUtilities> blazeUtilitiesMockedStatic = mockStatic(BlazeUtilities.class, Mockito.CALLS_REAL_METHODS)) {
            blazeUtilitiesMockedStatic.when(() -> BlazeUtilities.getGraphPreferences()).thenReturn(p);
            
            p.put(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.getBlazePresetsColorsDefault());
            final String presetsBefore = p.get(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.getBlazePresetsColorsDefault());
            assertEquals(presetsBefore, "#FF0000;#0000FF;#FFFF00;");
            
            // add a color to an invalid part of the presets list
            BlazeUtilities.savePreset(Color.CYAN, -1);            
            final String presetsAfter1 = p.get(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.getBlazePresetsColorsDefault());
            assertEquals(presetsAfter1, "#FF0000;#0000FF;#FFFF00;");
            
            BlazeUtilities.savePreset(Color.CYAN, 10);            
            final String presetsAfter2 = p.get(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.getBlazePresetsColorsDefault());
            assertEquals(presetsAfter2, "#FF0000;#0000FF;#FFFF00;");
            
            // add a color to the middle of the presets list
            BlazeUtilities.savePreset(Color.CYAN, 4);            
            final String presetsAfter3 = p.get(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.getBlazePresetsColorsDefault());
            assertEquals(presetsAfter3, "#FF0000;#0000FF;#FFFF00;null;#00ffff;null;null;null;null;null;");
            
            // override an existing color
            BlazeUtilities.savePreset(null, 1);            
            final String presetsAfter4 = p.get(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.getBlazePresetsColorsDefault());
            assertEquals(presetsAfter4, "#FF0000;null;#FFFF00;null;#00ffff;null;null;null;null;null;");
        } finally {
            // clean up, first remove Preferences nodes this test plays with
            p.removeNode();
            // and set the graph Preference back to its original setting
            GraphPreferenceKeys.setBlazePresetsColorsDefault(defaultBlazePresetColors);
        }
    }

    /**
     * Test of getHTMLColor method, of class BlazeUtilities.
     */
    @Test
    public void testGetHTMLColor() {
        System.out.println("getHTMLColor");
        
        final String htmlColor1 = BlazeUtilities.getHTMLColor(null);
        assertNull(htmlColor1);
        
        final String htmlColor2 = BlazeUtilities.getHTMLColor(Color.CYAN);
        assertEquals(htmlColor2, "#00ffff");
    }
}
