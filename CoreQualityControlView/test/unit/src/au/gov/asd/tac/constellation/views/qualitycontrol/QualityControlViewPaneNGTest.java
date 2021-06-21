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
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlEvent.QualityCategory;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlViewPane.DeleteQualityControlEvents;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlViewPane.DeselectQualityControlEvents;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlViewPane.SelectQualityControlEvents;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlState;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.IdentifierInconsistentWithTypeRule;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.MissingTypeRule;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.QualityControlRule;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.UnknownTypeRule;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javafx.embed.swing.JFXPanel;
import org.openide.util.Lookup;
import static org.testng.Assert.assertEquals;
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
    
    public QualityControlViewPaneNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
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
     * @throws InterruptedException
     */
    @Test
    public void testRefreshQualityControlViewNullState() throws InterruptedException {
        System.out.println("refreshQualityControlViewNullState");
        
        // TODO: Find a way to instantiate toolkit in a headless environment
        // This unit test should pass locally but will hold up in a headless environment (e.g. CI)
        // Putting it in this if loop is a temp fix for the hold up in CI but it does it by skipping the test 
        // (so CI won't actually tell you whether this is passing or not)
        if (!GraphicsEnvironment.isHeadless()) {
            //needed to initialise the toolkit
            new JFXPanel();
            
            final QualityControlViewPane instance = new QualityControlViewPane();

            instance.refreshQualityControlView(null);

            //not ideal but needed in order to allow the code running in JFX thread to complete
            Thread.sleep(100);

            final int noOfItems = instance.getQualityTable().getItems().size();
            assertEquals(noOfItems, 0);
        }      
    }
    
    /**
     * Test of refreshQualityControlView method, passing in an empty state.
     * @throws InterruptedException
     */
    @Test
    public void testRefreshQualityControlViewEmptyState() throws InterruptedException {
        System.out.println("refreshQualityControlViewEmptyState");
        
        // TODO: Find a way to instantiate toolkit in a headless environment
        // This unit test should pass locally but will hold up in a headless environment (e.g. CI)
        // Putting it in this if loop is a temp fix for the hold up in CI but it does it by skipping the test 
        // (so CI won't actually tell you whether this is passing or not)
        if (!GraphicsEnvironment.isHeadless()) {
            //needed to initialise the toolkit
            new JFXPanel();

            final QualityControlViewPane instance = new QualityControlViewPane();
            final QualityControlState state = new QualityControlState(graph.getId(), new ArrayList<>(), new ArrayList<>());

            instance.refreshQualityControlView(state);

            //not ideal but needed in order to allow the code running in JFX thread to complete
            Thread.sleep(100);

            final int noOfItems = instance.getQualityTable().getItems().size();
            assertEquals(noOfItems, 0);
        }
    }
    
    /**
     * Test of refreshQualityControlView method.
     * @throws InterruptedException
     */
    @Test
    public void testRefreshQualityControlView() throws InterruptedException {
        System.out.println("refreshQualityControlView");
        
        // TODO: Find a way to instantiate toolkit in a headless environment
        // This unit test should pass locally but will hold up in a headless environment (e.g. CI)
        // Putting it in this if loop is a temp fix for the hold up in CI but it does it by skipping the test 
        // (so CI won't actually tell you whether this is passing or not)
        if (!GraphicsEnvironment.isHeadless()) {
            //needed to initialise the toolkit
            new JFXPanel();

            final QualityControlViewPane instance = new QualityControlViewPane();
            final QualityControlState state = new QualityControlState(graph.getId(), events, rules);

            instance.refreshQualityControlView(state);

            //not ideal but needed in order to allow the code running in JFX thread to complete
            Thread.sleep(100);

            final int noOfItems = instance.getQualityTable().getItems().size();
            assertEquals(noOfItems, 2);           
        }       
    }

    /**
     * Test of qualityStyle method, of class QualityControlViewPane.
     */
    @Test
    public void testQualityStyle() {
        System.out.println("qualityStyle");

        final String defaultStyle = QualityControlViewPane.qualityStyle(QualityCategory.DEFAULT);
        assertEquals(defaultStyle, String.format("-fx-text-fill: rgb(0,0,0);-fx-background-color: rgba(%d,%d,255,%f);", 253, 253, 0.75f));       
        final String infoStyle = QualityControlViewPane.qualityStyle(QualityCategory.INFO);
        assertEquals(infoStyle, String.format("-fx-text-fill: rgb(0,0,0);-fx-background-color: rgba(%d,%d,255,%f);", 179, 179, 0.75f));
        final String warningStyle = QualityControlViewPane.qualityStyle(QualityCategory.WARNING);
        assertEquals(warningStyle, String.format("-fx-text-fill: rgb(255,255,255);-fx-background-color: rgba(%d,%d,255,%f);", 102, 102, 0.75f));
        final String severeStyle = QualityControlViewPane.qualityStyle(QualityCategory.SEVERE);
        assertEquals(severeStyle, String.format("-fx-text-fill: rgb(0,0,0);-fx-background-color: rgba(255,%d,%d,%f);", 26, 26, 0.75f));
        final String fatalStyle = QualityControlViewPane.qualityStyle(QualityCategory.FATAL);
        assertEquals(fatalStyle, String.format("-fx-text-fill: rgb(255,255,255);-fx-background-color: rgba(0,%d,%d,%f);", 13, 13, 0.75f));
    }

    /**
     * Test of getPriorities method, of class QualityControlViewPane.
     */
    @Test
    public void testGetPriorities() {
        System.out.println("getPriorities");

        final Map<QualityControlRule, QualityCategory> result = QualityControlViewPane.getPriorities();
        assertEquals(result.size(), rules.size());
        
        for (final QualityControlRule rule: result.keySet()) {
            rule.clearResults();
        }
        
        final IdentifierInconsistentWithTypeRule iiRule = new IdentifierInconsistentWithTypeRule();
        final MissingTypeRule mRule = new MissingTypeRule();
        final UnknownTypeRule uRule = new UnknownTypeRule();
        
        assertEquals(result.get(iiRule), QualityCategory.INFO);
        assertEquals(result.get(mRule), QualityCategory.SEVERE);
        assertEquals(result.get(uRule), QualityCategory.DEFAULT);
    }
    
    /**
     * Test of DeleteQualityControlEvents plugin with no events.
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
