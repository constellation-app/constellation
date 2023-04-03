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
package au.gov.asd.tac.constellation.views.find2.components.advanced;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.find2.FindViewController;
import au.gov.asd.tac.constellation.views.find2.FindViewTopComponent;
import au.gov.asd.tac.constellation.views.find2.components.AdvancedFindTab;
import au.gov.asd.tac.constellation.views.find2.components.BasicFindTab;
import au.gov.asd.tac.constellation.views.find2.components.FindViewPane;
import au.gov.asd.tac.constellation.views.find2.components.FindViewTabs;
import au.gov.asd.tac.constellation.views.find2.components.ReplaceTab;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.ColorCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FindCriteriaValues;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.layout.GridPane;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.openide.util.Exceptions;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Atlas139mkm
 */
public class ColorCriteriaPanelNGTest {

    private Map<String, Graph> graphMap = new HashMap<>();
    private Graph graph;
    private Graph graph2;
    private GraphAttribute labelAttributeV, identifierAttributeV, xAtrributeV, labelAttributeT, identifierAttributeT;

    private int selectedV, selectedT;
    private int labelV, identifierV, xV, dimV, colorV, iconV, dateTimeT, labelT, identiferT, widthT;
    private int vxId1, vxId2, vxId3, vxId4, vxId5UpperCase, vxId6, vxId7, vxId8, txId1, txId2, txId3, txId4;

    FindViewTopComponent findViewTopComponent;
    FindViewTopComponent spyTopComponent;

    BasicFindTab basicFindTab;
    ReplaceTab replaceTab;
    AdvancedFindTab advancedTab;
    FindViewPane findViewPane;
    FindViewTabs findViewTabs;
    private static final Logger LOGGER = Logger.getLogger(FloatCriteriaPanelNGTest.class.getName());

    public ColorCriteriaPanelNGTest() {
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
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        findViewTopComponent = mock(FindViewTopComponent.class
        );
        spyTopComponent = spy(findViewTopComponent);

        findViewPane = mock(FindViewPane.class
        );
        findViewTabs = mock(FindViewTabs.class
        );
        FindViewController.getDefault();

        basicFindTab = mock(BasicFindTab.class
        );
        replaceTab = mock(ReplaceTab.class
        );

        when(findViewTabs.getParentComponent()).thenReturn(findViewPane);
        when(findViewPane.getTabs()).thenReturn(findViewTabs);
        when(findViewTabs.getBasicFindTab()).thenReturn(basicFindTab);
        when(findViewTabs.getReplaceTab()).thenReturn(replaceTab);
        when(findViewTabs.getAdvancedFindTab()).thenReturn(advancedTab);

        advancedTab = new AdvancedFindTab(findViewTabs);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getCriteriaValues method, of class ColorCriteriaPanel.
     */
    @Test
    public void testGetCriteriaValues() {
        System.out.println("getCriteriaValues");

        setupGraph();

        AdvancedFindTab parentComponent = spy(advancedTab);
        final GraphElementType type = GraphElementType.VERTEX;

        ColorCriteriaPanel colorCriteriaPanel = new ColorCriteriaPanel(parentComponent, "color", type);
        colorCriteriaPanel.getFilterChoiceBox().getSelectionModel().select("Is");

        final List<AdvancedCriteriaBorderPane> tempList = new ArrayList<>();
        final GridPane tempGrid = new GridPane();
        tempList.add(colorCriteriaPanel);
        tempGrid.add(tempList.get(0), 0, 0);

        when(parentComponent.getCorrespondingCriteriaList(Mockito.eq(type))).thenReturn(tempList);
        when(parentComponent.getCorrespondingGridPane(Mockito.eq(type))).thenReturn(tempGrid);

        final List<AdvancedCriteriaBorderPane> criteriaList = parentComponent.getCorrespondingCriteriaList(type);

        final FindCriteriaValues result = criteriaList.get(0).getCriteriaValues();
        final ColorCriteriaValues colorResult = (ColorCriteriaValues) result;

        assertEquals(colorResult.getAttribute(), "color");
        assertEquals(colorResult.getAttributeType(), "color");
        assertEquals(colorResult.getFilter(), "Is");
        assertEquals(colorResult.getColorValue(), ConstellationColor.BLUE);
    }

    /**
     * Test of getType method, of class ColorCriteriaPanel.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");

        setupGraph();

        AdvancedFindTab parentComponent = spy(advancedTab);
        final GraphElementType type = GraphElementType.VERTEX;

        ColorCriteriaPanel colorCriteriaPanel = new ColorCriteriaPanel(parentComponent, "color", type);
        assertEquals(colorCriteriaPanel.getType(), ColorAttributeDescription.ATTRIBUTE_NAME);
    }

    private void setupGraph() {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        graph2 = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());

        graphMap.put(graph.getId(), graph);
        graphMap.put(graph2.getId(), graph2);
        try {

            WritableGraph wg = graph.getWritableGraph("", true);

            // Create Selected Attributes
            selectedV = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            labelV = VisualConcept.VertexAttribute.LABEL.ensure(wg);
            identifierV = VisualConcept.VertexAttribute.IDENTIFIER.ensure(wg);
            xV = VisualConcept.VertexAttribute.X.ensure(wg);
            colorV = VisualConcept.VertexAttribute.COLOR.ensure(wg);

            selectedT = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);
            labelT = VisualConcept.TransactionAttribute.LABEL.ensure(wg);
            identiferT = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(wg);
            widthT = VisualConcept.TransactionAttribute.WIDTH.ensure(wg);

            vxId1 = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId1, false);
            wg.setStringValue(labelV, vxId1, "label name");
            wg.setStringValue(identifierV, vxId1, "identifer name");
            wg.setFloatValue(xV, vxId1, 1);
            wg.setObjectValue(colorV, vxId1, ConstellationColor.BLUE);

            wg.commit();

        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        }
    }

}
