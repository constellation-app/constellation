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
package au.gov.asd.tac.constellation.views.qualitycontrol.daemon;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlEvent;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.QualityControlRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.util.Lookup;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class QualityControlStateUpdaterNGTest {

    private int attrX, attrY, attrZ;
    private int vxId1, vxId2;
    private int txId1;
    private int vSelectedAttrId, tSelectedAttrId, vxIdentifierAttrId, typeAttrId;
    private Graph graph;

    public QualityControlStateUpdaterNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
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
    public void testReadNoNodesNoAttributes() throws Exception {
        System.out.println("read No Nodes");

        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        WritableGraph wg = graph.getWritableGraph("Add Elements", true);
        try {
            // Add X,Y,Z vertex attributes
            attrX = VisualConcept.VertexAttribute.X.ensure(wg);
            attrY = VisualConcept.VertexAttribute.Y.ensure(wg);
            attrZ = VisualConcept.VertexAttribute.Z.ensure(wg);
        } finally {
            wg.commit();
        }

        // Expected rules and events
        final List<QualityControlEvent> expectedQualityControlEvents = new ArrayList<>();
        final List<QualityControlRule> expectedRegisteredRules = new ArrayList<>();
        final List<QualityControlRule> uExpectedRegisteredRules = Collections.unmodifiableList(expectedRegisteredRules);

        // Call update state to trigger checking of rules
        PluginExecution.withPlugin(new QualityControlStateUpdater()).executeNow(graph);

        // get the state and events
        final QualityControlState state = QualityControlAutoVetter.getInstance().getQualityControlState();
        final List<QualityControlEvent> qualityControlEvents = state.getQualityControlEvents();
        final List<QualityControlRule> registeredRules = state.getRegisteredRules();

        // check equality of the events and rules
        assertEquals(qualityControlEvents, expectedQualityControlEvents);
        assertEquals(registeredRules, uExpectedRegisteredRules);
    }

    @Test
    public void testReadNoNodes() throws Exception {
        System.out.println("read No Nodes");

        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        WritableGraph wg = graph.getWritableGraph("Add Elements", true);
        try {
            // Add X,Y,Z vertex attributes
            attrX = VisualConcept.VertexAttribute.X.ensure(wg);
            attrY = VisualConcept.VertexAttribute.Y.ensure(wg);
            attrZ = VisualConcept.VertexAttribute.Z.ensure(wg);

            // Add vertex and transaction SELECTED attributes
            vSelectedAttrId = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            tSelectedAttrId = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);

            // Add vertex IDENTIFIER attribute and label each vertice.
            vxIdentifierAttrId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(wg);

            // Add vertex TYPE attribute and set each type to unknown
            typeAttrId = AnalyticConcept.VertexAttribute.TYPE.ensure(wg);
        } finally {
            wg.commit();
        }

        // Expected rules and events
        final List<QualityControlEvent> expectedQualityControlEvents = new ArrayList<>();
        final List<QualityControlRule> expectedRegisteredRules = new ArrayList<>();
        final List<QualityControlRule> uExpectedRegisteredRules = Collections.unmodifiableList(expectedRegisteredRules);

        // Call update state to trigger checking of rules
        PluginExecution.withPlugin(new QualityControlStateUpdater()).executeNow(graph);

        // get the state and events
        final QualityControlState state = QualityControlAutoVetter.getInstance().getQualityControlState();
        final List<QualityControlEvent> qualityControlEvents = state.getQualityControlEvents();
        final List<QualityControlRule> registeredRules = state.getRegisteredRules();

        // check equality of the events and rules
        assertEquals(qualityControlEvents, expectedQualityControlEvents);
        assertEquals(registeredRules, uExpectedRegisteredRules);
    }

    @Test
    public void testReadSelectedNodesWithRules() throws Exception {
        System.out.println("read Selected Nodes With Rules");

        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        WritableGraph wg = graph.getWritableGraph("Add Elements", true);
        try {
            // Add X,Y,Z vertex attributes
            attrX = VisualConcept.VertexAttribute.X.ensure(wg);
            attrY = VisualConcept.VertexAttribute.Y.ensure(wg);
            attrZ = VisualConcept.VertexAttribute.Z.ensure(wg);

            // Add vertex and transaction SELECTED attributes
            vSelectedAttrId = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            tSelectedAttrId = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);

            // Add two vertices
            vxId1 = wg.addVertex();
            vxId2 = wg.addVertex();

            // Add one transaction between the two vertices
            txId1 = wg.addTransaction(vxId1, vxId2, false);

            wg.setFloatValue(attrX, vxId1, 1.0f);
            wg.setFloatValue(attrY, vxId1, 1.0f);
            wg.setBooleanValue(vSelectedAttrId, vxId1, false);

            wg.setFloatValue(attrX, vxId2, 2.0f);
            wg.setFloatValue(attrY, vxId2, 2.0f);
            wg.setBooleanValue(vSelectedAttrId, vxId1, true);

            // Add vertex IDENTIFIER attribute and label each vertice.
            vxIdentifierAttrId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(wg);
            wg.setStringValue(vxIdentifierAttrId, vxId1, "Vertex1");
            wg.setStringValue(vxIdentifierAttrId, vxId2, "Vertex2");

            // Add vertex TYPE attribute and set each type to unknown
            typeAttrId = AnalyticConcept.VertexAttribute.TYPE.ensure(wg);
            wg.setObjectValue(typeAttrId, vxId1, SchemaVertexType.unknownType());
            wg.setObjectValue(typeAttrId, vxId2, SchemaVertexType.unknownType());

        } finally {
            wg.commit();
        }

        // Expected rules
        final List<QualityControlRule> expectedRegisteredRules = new ArrayList<>(Lookup.getDefault().lookupAll(QualityControlRule.class));

        // generate list of vertex ids
        final List<Integer> vertexIds = new ArrayList<>();
        vertexIds.add(0);
        vertexIds.add(1);

        // Check rules against vertex ids
        for (final QualityControlRule rule : expectedRegisteredRules) {
            rule.executeRule(graph.getReadableGraph(), vertexIds);
        }

        final List<QualityControlEvent> expectedQualityControlEvents = new ArrayList<>();
        expectedQualityControlEvents.add(new QualityControlEvent(0, "Vertex1", SchemaVertexType.unknownType(), expectedRegisteredRules));

        // Call update state to trigger checking of rules
        PluginExecution.withPlugin(new QualityControlStateUpdater()).executeNow(graph);

        // get the state and events
        final QualityControlState state = QualityControlAutoVetter.getInstance().getQualityControlState();
        final List<QualityControlEvent> qualityControlEvents = state.getQualityControlEvents();
        final List<QualityControlRule> registeredRules = state.getRegisteredRules();

        // Loop all events and check equality for each item specifically. Testing equality of the list was taken literally.
        assertEquals(qualityControlEvents.size(), expectedQualityControlEvents.size());
        int i = 0;
        for (QualityControlEvent event : expectedQualityControlEvents) {
            if (qualityControlEvents.size() >= i) {
                assertEquals(qualityControlEvents.get(i).getReasons(), event.getReasons());
                assertEquals(qualityControlEvents.get(i).getQuality(), event.getQuality());
                assertEquals(qualityControlEvents.get(i).getVertex(), event.getVertex());
                assertEquals(qualityControlEvents.get(i).getRules(), event.getRules());
                assertEquals(qualityControlEvents.get(i).getType(), event.getType());
            }
            i++;
        }

        // check equality of the rules
        assertEquals(registeredRules, expectedRegisteredRules);
    }
    
    @Test
    public void testReadSelectedNodesWithDisabledRules() throws Exception {
        System.out.println("read Selected Nodes With Disabled Rules");

        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        WritableGraph wg = graph.getWritableGraph("Add Elements", true);
        try {
            // Add X,Y,Z vertex attributes
            attrX = VisualConcept.VertexAttribute.X.ensure(wg);
            attrY = VisualConcept.VertexAttribute.Y.ensure(wg);
            attrZ = VisualConcept.VertexAttribute.Z.ensure(wg);

            // Add vertex and transaction SELECTED attributes
            vSelectedAttrId = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            tSelectedAttrId = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);

            // Add two vertices
            vxId1 = wg.addVertex();
            vxId2 = wg.addVertex();

            // Add one transaction between the two vertices
            txId1 = wg.addTransaction(vxId1, vxId2, false);

            wg.setFloatValue(attrX, vxId1, 1.0f);
            wg.setFloatValue(attrY, vxId1, 1.0f);
            wg.setBooleanValue(vSelectedAttrId, vxId1, false);

            wg.setFloatValue(attrX, vxId2, 2.0f);
            wg.setFloatValue(attrY, vxId2, 2.0f);
            wg.setBooleanValue(vSelectedAttrId, vxId1, true);

            // Add vertex IDENTIFIER attribute and label each vertice.
            vxIdentifierAttrId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(wg);
            wg.setStringValue(vxIdentifierAttrId, vxId1, "Vertex1");
            wg.setStringValue(vxIdentifierAttrId, vxId2, "Vertex2");

            // Add vertex TYPE attribute and set each type to unknown
            typeAttrId = AnalyticConcept.VertexAttribute.TYPE.ensure(wg);
            wg.setObjectValue(typeAttrId, vxId1, SchemaVertexType.unknownType());
            wg.setObjectValue(typeAttrId, vxId2, SchemaVertexType.unknownType());

        } finally {
            wg.commit();
        }

        // Expected rules
        final List<QualityControlRule> expectedRegisteredRules = new ArrayList<>(Lookup.getDefault().lookupAll(QualityControlRule.class));

        // Disable all the rules
        for (final QualityControlRule rule : expectedRegisteredRules) {
            rule.setEnabled(false);
        }

        final List<QualityControlEvent> expectedQualityControlEvents = new ArrayList<>();
        expectedQualityControlEvents.add(new QualityControlEvent(0, "Vertex1", SchemaVertexType.unknownType(), Collections.emptyList()));

        // Call update state to trigger checking of rules
        PluginExecution.withPlugin(new QualityControlStateUpdater()).executeNow(graph);

        // get the state and events
        final QualityControlState state = QualityControlAutoVetter.getInstance().getQualityControlState();
        final List<QualityControlEvent> qualityControlEvents = state.getQualityControlEvents();
        final List<QualityControlRule> registeredRules = state.getRegisteredRules();

        // Loop all events and check equality for each item specifically. Testing equality of the list was taken literally.
        assertEquals(qualityControlEvents.size(), expectedQualityControlEvents.size());
        int i = 0;
        for (QualityControlEvent event : expectedQualityControlEvents) {
            if (qualityControlEvents.size() >= i) {
                assertEquals(qualityControlEvents.get(i).getReasons(), event.getReasons());
                assertEquals(qualityControlEvents.get(i).getQuality(), event.getQuality());
                assertEquals(qualityControlEvents.get(i).getVertex(), event.getVertex());
                assertEquals(qualityControlEvents.get(i).getRules(), event.getRules());
                assertEquals(qualityControlEvents.get(i).getType(), event.getType());
            }
            i++;
        }

        // check equality of the rules
        // since all the rules have been disabled, this should be empty
        assertEquals(registeredRules, Collections.emptyList());
        
        // Enable all the rules again to clean up
        for (final QualityControlRule rule : expectedRegisteredRules) {
            rule.setEnabled(true);
        }
    }

    /**
     * Test of getName method, of class QualityControlStateUpdater.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        final QualityControlStateUpdater instance = new QualityControlStateUpdater();
        assertEquals(instance.getName(), QualityControlStateUpdater.PLUGIN_NAME);
    }

}
