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
package au.gov.asd.tac.constellation.views.qualitycontrol;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.json.JsonUtilities;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlEvent.QualityCategory;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlViewPane.DeleteQualityControlEvents;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlViewPane.DeselectQualityControlEvents;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlViewPane.SelectQualityControlEvents;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlState;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.IdentifierInconsistentWithTypeRule;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.MissingTypeRule;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.QualityControlRule;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.UnknownTypeRule;
import com.fasterxml.jackson.core.JsonFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import org.openide.util.Lookup;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
public class QualityControlViewPaneNGTest {
    private static final Logger LOGGER = Logger.getLogger(QualityControlViewPaneNGTest.class.getName());

    private int vertexIdentifierAttribute;
    private int vertexTypeAttribute;
    private int vertexSelectedAttribute;

    private int vxId1;
    private int vxId2;
    private int vxId3;

    private StoreGraph graph;

    private List<QualityControlEvent> events;
    private List<QualityControlRule> rules;
    private List<Integer> vertices;

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
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // create an analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);

        // add attributes
        vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        // add vertices
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();

        // set all vertices to be selected
        graph.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, false);

        rules = new ArrayList<>(Lookup.getDefault().lookupAll(QualityControlRule.class));
        vertices = Arrays.asList(vxId1, vxId2);

        for (final QualityControlRule rule : rules) {
            rule.executeRule(graph, vertices);
        }

        events = new ArrayList<>();
        for (final int vertexId : vertices) {
            final String type = graph.getStringValue(vertexTypeAttribute, vertexId);
            events.add(new QualityControlEvent(
                    vertexId,
                    graph.getStringValue(vertexIdentifierAttribute, vertexId),
                    SchemaVertexTypeUtilities.getType(type),
                    rules
            ));
        }
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of refreshQualityControlView method, passing in a null state.
     *
     * @throws InterruptedException
     */
    @Test
    public void testRefreshQualityControlViewNullState() throws InterruptedException {
        System.out.println("refreshQualityControlViewNullState");

        final QualityControlViewPane instance = new QualityControlViewPane();

        instance.refreshQualityControlView(null);

        //not ideal but needed in order to allow the code running in JFX thread to complete
        Thread.sleep(100);

        final int noOfItems = instance.getQualityTable().getItems().size();
        assertEquals(noOfItems, 0);
    }

    /**
     * Test of refreshQualityControlView method, passing in an empty state.
     *
     * @throws InterruptedException
     */
    @Test
    public void testRefreshQualityControlViewEmptyState() throws InterruptedException {
        System.out.println("refreshQualityControlViewEmptyState");

        final QualityControlViewPane instance = new QualityControlViewPane();
        final QualityControlState state = new QualityControlState(graph.getId(), new ArrayList<>(), new ArrayList<>());

        instance.refreshQualityControlView(state);

        //not ideal but needed in order to allow the code running in JFX thread to complete
        Thread.sleep(100);

        final int noOfItems = instance.getQualityTable().getItems().size();
        assertEquals(noOfItems, 0);
    }

    /**
     * Test of refreshQualityControlView method.
     *
     * @throws InterruptedException
     */
    @Test
    public void testRefreshQualityControlView() throws InterruptedException {
        System.out.println("refreshQualityControlView");

        final QualityControlViewPane instance = new QualityControlViewPane();
        final QualityControlState state = new QualityControlState(graph.getId(), events, rules);

        instance.refreshQualityControlView(state);

        //not ideal but needed in order to allow the code running in JFX thread to complete
        Thread.sleep(100);

        final int noOfItems = instance.getQualityTable().getItems().size();
        assertEquals(noOfItems, 2);
    }

    /**
     * Test of qualityStyle method, of class QualityControlViewPane.
     */
    @Test
    public void testQualityStyle() {
        System.out.println("qualityStyle");

        final String okStyle = QualityControlViewPane.qualityStyle(QualityCategory.OK);
        assertEquals(okStyle, String.format("-fx-text-fill: rgb(0,0,0);-fx-background-color: rgba(0,200,0,%f);", 0.75f));
        final String minorStyle = QualityControlViewPane.qualityStyle(QualityCategory.MINOR);
        assertEquals(minorStyle, String.format("-fx-text-fill: rgb(0,0,0);-fx-background-color: rgba(90,150,255,%f);", 0.75f));
        final String mediumStyle = QualityControlViewPane.qualityStyle(QualityCategory.MEDIUM);
        assertEquals(mediumStyle, String.format("-fx-text-fill: rgb(0,0,0);-fx-background-color: rgba(255,215,0,%f);", 0.75f));
        final String majorStyle = QualityControlViewPane.qualityStyle(QualityCategory.MAJOR);
        assertEquals(majorStyle, String.format("-fx-text-fill: rgb(255,255,255);-fx-background-color: rgba(255,%d,0,%f);", 102, 0.75f));
        final String severeStyle = QualityControlViewPane.qualityStyle(QualityCategory.SEVERE);
        assertEquals(severeStyle, String.format("-fx-text-fill: rgb(0,0,0);-fx-background-color: rgba(255,%d,%d,%f);", 26, 26, 0.75f));
        final String criticalStyle = QualityControlViewPane.qualityStyle(QualityCategory.CRITICAL);
        assertEquals(criticalStyle, String.format("-fx-text-fill: rgb(255,255,255);-fx-background-color: rgba(150,%d,%d,%f);", 13, 13, 0.75f));
    }
    
    /**
     * Test of readSerializedRuleEnabledStatuses method, of class QualityControlViewPane.
     */
    @Test
    public void testReadSerializedRuleEnabledStatuses() {
        System.out.println("readSerializedRuleEnabledStatuses");
        
        try (
                final MockedStatic<JsonUtilities> jsonUtilitiesMockedStatic = mockStatic(JsonUtilities.class);
                final MockedStatic<QualityControlViewPane> qualityControlViewPaneMockedStatic = mockStatic(QualityControlViewPane.class)) {
            final Map<String, String> jsonMap = new HashMap<>();
            jsonMap.put("Missing type", "false");
            jsonMap.put("Unknown type", "true");
            
            jsonUtilitiesMockedStatic.when(() -> JsonUtilities.getStringAsMap(any(JsonFactory.class), anyString()))
                    .thenReturn(jsonMap);
            
            final MissingTypeRule mRule = new MissingTypeRule();
            final UnknownTypeRule uRule = new UnknownTypeRule();
            
            final Map<QualityControlRule, Boolean> enabledMap = new HashMap<>();
            
            qualityControlViewPaneMockedStatic.when(() -> QualityControlViewPane.getEnablementStatuses())
                    .thenReturn(enabledMap);
            qualityControlViewPaneMockedStatic.when(() -> QualityControlViewPane.readSerializedRuleEnabledStatuses())
                    .thenCallRealMethod();
            
            QualityControlViewPane.readSerializedRuleEnabledStatuses();
            
            // clearing results so that we can successfully grab the rules
            // other tests requiring results should be executing the rules anyway
            for (final QualityControlRule rule : enabledMap.keySet()) {
                rule.clearResults();
            }
            
            assertFalse(enabledMap.get(mRule));
            assertTrue(enabledMap.get(uRule));
        }
        
    }

    /**
     * Test of getPriorities method, of class QualityControlViewPane.
     */
    @Test
    public void testGetPriorities() {
        System.out.println("getPriorities");

        final Map<QualityControlRule, QualityCategory> result = QualityControlViewPane.getPriorities();
        assertEquals(result.size(), rules.size());

        for (final QualityControlRule rule : result.keySet()) {
            rule.clearResults();
        }

        final IdentifierInconsistentWithTypeRule iiRule = new IdentifierInconsistentWithTypeRule();
        final MissingTypeRule mRule = new MissingTypeRule();
        final UnknownTypeRule uRule = new UnknownTypeRule();

        assertEquals(result.get(iiRule), QualityCategory.MEDIUM);
        assertEquals(result.get(mRule), QualityCategory.SEVERE);
        assertEquals(result.get(uRule), QualityCategory.MINOR);
    }
    
    /**
     * Test of getPriorities method, of class QualityControlViewPane.
     */
    @Test
    public void testGetEnablementStatuses() {
        System.out.println("getEnablementStatuses");
        
        final Map<QualityControlRule, Boolean> result = QualityControlViewPane.getEnablementStatuses();
        assertEquals(result.size(), rules.size());

        for (final QualityControlRule rule : result.keySet()) {
            rule.clearResults();
        }

        final IdentifierInconsistentWithTypeRule iiRule = new IdentifierInconsistentWithTypeRule();
        final MissingTypeRule mRule = new MissingTypeRule();
        final UnknownTypeRule uRule = new UnknownTypeRule();

        // all rules should be enabled by default
        assertTrue(result.get(iiRule));
        assertTrue(result.get(mRule));
        assertTrue(result.get(uRule));
    }  

    /**
     * Test of DeleteQualityControlEvents plugin with no events.
     *
     * @throws InterruptedException
     * @throws PluginException
     */
    @Test
    public void testDeleteQualityControlEventsNoEvents() throws InterruptedException, PluginException {
        final DeleteQualityControlEvents plugin = new DeleteQualityControlEvents(new ArrayList<>());

        assertEquals(graph.getVertexCount(), 3);

        PluginExecution.withPlugin(plugin).executeNow(graph);

        assertEquals(graph.getVertexCount(), 3);
    }

    /**
     * Test of DeleteQualityControlEvents plugin with events.
     *
     * @throws InterruptedException
     * @throws PluginException
     */
    @Test
    public void testDeleteQualityControlEventsWithEvents() throws InterruptedException, PluginException {
        final DeleteQualityControlEvents plugin = new DeleteQualityControlEvents(events);

        assertEquals(graph.getVertexCount(), 3);

        PluginExecution.withPlugin(plugin).executeNow(graph);

        assertEquals(graph.getVertexCount(), 1);
    }

    /**
     * Test of SelectQualityControlEvents plugin with no events.
     *
     * @throws InterruptedException
     * @throws PluginException
     */
    @Test
    public void testSelectQualityControlEventsNoEvents() throws InterruptedException, PluginException {
        final SelectQualityControlEvents plugin = new SelectQualityControlEvents(new ArrayList<>());

        graph.setBooleanValue(vertexSelectedAttribute, vxId1, false);
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, false);

        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId1), false);
        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId2), false);
        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId3), false);

        PluginExecution.withPlugin(plugin).executeNow(graph);

        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId1), false);
        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId2), false);
        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId3), false);
    }

    /**
     * Test of SelectQualityControlEvents plugin with events.
     *
     * @throws InterruptedException
     * @throws PluginException
     */
    @Test
    public void testSelectQualityControlEventsWithEvents() throws InterruptedException, PluginException {
        final SelectQualityControlEvents plugin = new SelectQualityControlEvents(events);

        graph.setBooleanValue(vertexSelectedAttribute, vxId1, false);
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, false);

        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId1), false);
        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId2), false);
        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId3), false);

        PluginExecution.withPlugin(plugin).executeNow(graph);

        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId1), true);
        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId2), true);
        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId3), false);
    }

    /**
     * Test of DeselectQualityControlEvents plugin with no events.
     *
     * @throws InterruptedException
     * @throws PluginException
     */
    @Test
    public void testDeselectQualityControlEventsNoEvents() throws InterruptedException, PluginException {
        final DeselectQualityControlEvents plugin = new DeselectQualityControlEvents(new ArrayList<>());

        PluginExecution.withPlugin(plugin).executeNow(graph);

        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId1), true);
        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId2), true);
        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId3), false);
    }

    /**
     * Test of DeselectQualityControlEvents plugin with events.
     *
     * @throws InterruptedException
     * @throws PluginException
     */
    @Test
    public void testDeselectQualityControlEventsWithEvents() throws InterruptedException, PluginException {
        final DeselectQualityControlEvents plugin = new DeselectQualityControlEvents(events);

        PluginExecution.withPlugin(plugin).executeNow(graph);

        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId1), false);
        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId2), false);
        assertEquals(graph.getBooleanValue(vertexSelectedAttribute, vxId3), false);
    }
}
