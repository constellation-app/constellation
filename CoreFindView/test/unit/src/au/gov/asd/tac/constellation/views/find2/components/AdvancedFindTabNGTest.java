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
package au.gov.asd.tac.constellation.views.find2.components;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.views.find2.FindViewController;
import au.gov.asd.tac.constellation.views.find2.FindViewTopComponent;
import au.gov.asd.tac.constellation.views.find2.components.advanced.AdvancedCriteriaBorderPane;
import au.gov.asd.tac.constellation.views.find2.components.advanced.StringCriteriaPanel;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FindCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.StringCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.utilities.AdvancedSearchParameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.util.Exceptions;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Atlas139mkm
 */
public class AdvancedFindTabNGTest {

    private Map<String, Graph> graphMap = new HashMap<>();
    private Graph graph;
    private Graph graph2;
    private GraphAttribute labelAttributeV, identifierAttributeV, xAtrributeV, labelAttributeT, identifierAttributeT;

    private int selectedV, selectedT;
    private int labelV, identifierV, xV, labelT, identiferT, widthT;
    private int vxId1, vxId2, vxId3, vxId4, vxId5UpperCase, vxId6, vxId7, vxId8, txId1, txId2, txId3, txId4;

    FindViewTopComponent findViewTopComponent;
    FindViewTopComponent spyTopComponent;

    BasicFindTab basicFindTab;
    ReplaceTab replaceTab;
    AdvancedFindTab advancedTab;
    FindViewPane findViewPane;
    FindViewTabs findViewTabs;
    private static final Logger LOGGER = Logger.getLogger(BasicFindTabNGTest.class.getName());

    public AdvancedFindTabNGTest() throws Exception {
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
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        findViewTopComponent = mock(FindViewTopComponent.class);
        spyTopComponent = spy(findViewTopComponent);

        findViewPane = mock(FindViewPane.class);
        findViewTabs = mock(FindViewTabs.class);
        FindViewController.getDefault();

        basicFindTab = mock(BasicFindTab.class);
        replaceTab = mock(ReplaceTab.class);

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
     * Test of updateButtons method, of class AdvancedFindTab.
     */
    @Test
    public void testUpdateButtons() {
        System.out.println("updateButtons");

        advancedTab.buttonsHBox.getChildren().clear();
        advancedTab.buttonsHBox.getChildren().add(new Button("test"));

        /**
         * The updateButtons function should clear the existing elements (The
         * button added above) and add the findPrevButton, findNextButton,
         * getFindAllButton and getSearchAllGraphs checkbox.
         */
        advancedTab.updateButtons();
        assertEquals(advancedTab.buttonsHBox.getChildren().get(0), advancedTab.getDeleteResultsButton());
        assertEquals(advancedTab.buttonsHBox.getChildren().get(1), advancedTab.getFindAllButton());
        assertEquals(advancedTab.buttonsHBox.getChildren().get(2), advancedTab.getFindPrevButton());
        assertEquals(advancedTab.buttonsHBox.getChildren().get(3), advancedTab.getFindNextButton());

    }

    /**
     * Test of getSelectedGraphElementType method, of class AdvancedFindTab.
     */
    @Test
    public void testGetSelectedGraphElementType() {
        System.out.println("getSelectedGraphElementType");

        advancedTab.getLookForChoiceBox().getSelectionModel().select("Node");

        GraphElementType result = advancedTab.getSelectedGraphElementType();
        GraphElementType expResult = GraphElementType.VERTEX;
        assertEquals(result, expResult);

        assertNotEquals(result, GraphElementType.TRANSACTION);

    }

    /**
     * Test of updateSelectionFactors method, of class AdvancedFindTab.
     */
    @Test
    public void testUpdateSelectionFactors() {
        System.out.println("updateSelectionFactors");

        /**
         * The updateSelectionFactors function is called via a change listener.
         * When index 2 or 3 is selected the findNextButton and the
         * FindPreviousButton will be disabled
         */
        advancedTab.getPostSearchChoiceBox().getSelectionModel().select(0);
        assertEquals(false, advancedTab.getFindNextButton().isDisabled());
        assertEquals(false, advancedTab.getFindPrevButton().isDisabled());

        advancedTab.getPostSearchChoiceBox().getSelectionModel().select(1);
        assertEquals(true, advancedTab.getFindNextButton().isDisabled());
        assertEquals(true, advancedTab.getFindPrevButton().isDisabled());

        advancedTab.getPostSearchChoiceBox().getSelectionModel().select(2);
        assertEquals(true, advancedTab.getFindNextButton().isDisabled());
        assertEquals(true, advancedTab.getFindPrevButton().isDisabled());

        advancedTab.getPostSearchChoiceBox().getSelectionModel().select(3);
        assertEquals(true, advancedTab.getFindNextButton().isDisabled());
        assertEquals(true, advancedTab.getFindPrevButton().isDisabled());

    }

    /**
     * Test of addCriteriaPane method, of class AdvancedFindTab.
     */
    @Test
    public void testAddCriteriaPane() {
        System.out.println("addCriteriaPane");

        setupGraph();

        GraphElementType type = GraphElementType.VERTEX;

        /**
         * Check the the criteriaList increases in size as we add a new
         * criteriaPane
         */
        assertEquals(advancedTab.getCorrespondingCriteriaList(type).size(), 0);
        advancedTab.addCriteriaPane(type);
        assertEquals(advancedTab.getCorrespondingCriteriaList(type).size(), 1);
        advancedTab.addCriteriaPane(type);
        assertEquals(advancedTab.getCorrespondingCriteriaList(type).size(), 2);

        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of deleteCriteriaPane method, of class AdvancedFindTab.
     */
    @Test
    public void testDeleteCriteriaPane() {
        System.out.println("deleteCriteriaPane");

        setupGraph();

        GraphElementType type = GraphElementType.VERTEX;

        /**
         * Check the the criteriaList increases in size as we add a new
         * criteriaPane
         */
        assertEquals(advancedTab.getCorrespondingCriteriaList(type).size(), 0);
        advancedTab.addCriteriaPane(type);
        assertEquals(advancedTab.getCorrespondingCriteriaList(type).size(), 1);
        advancedTab.addCriteriaPane(type);
        assertEquals(advancedTab.getCorrespondingCriteriaList(type).size(), 2);

        advancedTab.deleteCriteriaPane(advancedTab.getCorrespondingCriteriaList(type).get(1), type, 1);
        assertEquals(advancedTab.getCorrespondingCriteriaList(type).size(), 1);

    }

    /**
     * Test of changeCriteriaPane method, of class AdvancedFindTab.
     *
     * TODO fix this
     */
//    @Test
//    public void testChangeCriteriaPane() {
//        System.out.println("changeCriteriaPane");
//
//        setupGraph();
//
//        GraphElementType type = GraphElementType.VERTEX;
//
//        advancedTab.addCriteriaPane(type);
//
//        AdvancedFindTab advancedFindSpy = spy(advancedTab);
//
//        AdvancedCriteriaBorderPane tempPane1 = advancedTab.getCorrespondingCriteriaList(type).get(0);
//        AdvancedCriteriaBorderPane spyTempPane = spy(tempPane1);
//
//        final List<AdvancedCriteriaBorderPane> criteriaPaneList = new ArrayList<>();
//        criteriaPaneList.add(spyTempPane);
//
//        ReadableGraph rg = graph.getReadableGraph();
//
//        int attributeInt = rg.getAttribute(type, 1);
//        labelAttributeV = new GraphAttribute(rg, attributeInt);
//        attributeInt = rg.getAttribute(type, 2);
//        identifierAttributeV = new GraphAttribute(rg, attributeInt);
//        attributeInt = rg.getAttribute(type, 3);
//        xAtrributeV = new GraphAttribute(rg, attributeInt);
//        rg.close();
//
//        List<Attribute> attributeList = new ArrayList<>();
//        attributeList.add(labelAttributeV);
//        attributeList.add(identifierAttributeV);
//        attributeList.add(xAtrributeV);
//        when(spyTempPane.getAttributesList()).thenReturn(attributeList);
//        when(advancedFindSpy.getCorrespondingCriteriaList(type)).thenReturn(criteriaPaneList);
//
//        advancedFindSpy.changeCriteriaPane(spyTempPane, type, "x", false);
//        assertEquals(advancedFindSpy.getCorrespondingCriteriaList(type).get(0).getType(), FloatAttributeDescription.ATTRIBUTE_NAME);
//    }

    /**
     * Test of getCorrespondingCriteriaList method, of class AdvancedFindTab.
     */
    @Test
    public void testGetCorrespondingCriteriaList() {
        System.out.println("getCorrespondingCriteriaList");

        GraphElementType type = GraphElementType.VERTEX;
        assertEquals(advancedTab.getNodeFindCriteriaList(), advancedTab.getCorrespondingCriteriaList(type));

        type = GraphElementType.TRANSACTION;
        assertEquals(advancedTab.getTransactionFindCriteriaList(), advancedTab.getCorrespondingCriteriaList(type));

    }

    /**
     * Test of getCriteriaValues method, of class AdvancedFindTab.
     */
    @Test
    public void testGetCriteriaValues() {
        System.out.println("getCriteriaValues");

        setupGraph();

        GraphElementType type = GraphElementType.VERTEX;

        List<AdvancedCriteriaBorderPane> criteriaList = new ArrayList<>();
        AdvancedCriteriaBorderPane pane1 = new StringCriteriaPanel(advancedTab, "Identifier", type);
        AdvancedCriteriaBorderPane pane2 = new StringCriteriaPanel(advancedTab, "Label", type);
        criteriaList.add(pane1);
        criteriaList.add(pane2);
        List<FindCriteriaValues> resultsList = advancedTab.getCriteriaValues(criteriaList);

        assertEquals(resultsList.get(0).getAttribute(), "Identifier");
        assertEquals(resultsList.get(1).getAttribute(), "Label");

    }

    /**
     * Test of updateAdvancedSearchParameters method, of class AdvancedFindTab.
     */
    @Test
    public void testUpdateAdvancedSearchParameters() {
        System.out.println("updateAdvancedSearchParameters");

        final AdvancedSearchParameters controlllerParameters = FindViewController.getDefault().getCurrentAdvancedSearchParameters();

        final GraphElementType elementType = GraphElementType.VERTEX;

        /**
         * Call the updateBasicFindParamters function. Check that each of the
         * javaFX elements passes their corresponding data correctly to the
         * controllers basicFindParamters
         */
        advancedTab.updateAdvancedSearchParameters(elementType);

        /**
         * All parameters should equal the current value of the advancedFindTabs
         * elements
         */
        assertEquals(controlllerParameters.getCriteriaValuesList().size(), advancedTab.getCorrespondingCriteriaList(elementType).size());
        assertEquals(controlllerParameters.getGraphElementType(), elementType);
        assertEquals(controlllerParameters.getAllOrAny(), advancedTab.getMatchCriteriaChoiceBox().getSelectionModel().getSelectedItem());
        assertEquals(controlllerParameters.getPostSearchAction(), advancedTab.getPostSearchChoiceBox().getSelectionModel().getSelectedItem());

    }

    /**
     * Test of findAllAction method, of class AdvancedFindTab.
     */
    @Test
    public void testFindAllAction() {
        System.out.println("findAllAction");

        setupGraph();

        //Create a controller mock and do nothing on retriveMatchingElements()
        FindViewController mockController = mock(FindViewController.class);
        mockController.init(spyTopComponent);
        doNothing().when(mockController).retrieveAdvancedSearch(Mockito.eq(true), Mockito.eq(false));
        Button mockButton = mock(Button.class);

        GraphElementType graphElementType = GraphElementType.VERTEX;

        /**
         * Create temporary advancedFindMock, pane, criteriaPaneList,
         * LookForChoiceBox, findCriteriaValuesList and a stringCriteriaValue
         */
        AdvancedFindTab advancedFindMock = mock(AdvancedFindTab.class);

        StringCriteriaPanel tempPane = new StringCriteriaPanel(advancedTab, "Identifer", graphElementType);
        tempPane.setSearchFieldText("hello");

        final List<AdvancedCriteriaBorderPane> criteriaPaneList = new ArrayList<>();
        criteriaPaneList.add(tempPane);

        final ChoiceBox<String> lookForChoiceBox = new ChoiceBox<>();
        lookForChoiceBox.getItems().add("Node");
        lookForChoiceBox.getSelectionModel().select(0);

        final List<FindCriteriaValues> findCriteriaValues = new ArrayList<>();
        final StringCriteriaValues stringCriteriaValue = new StringCriteriaValues("string", "Identifer", "Is", "hello", false, false);
        findCriteriaValues.add(stringCriteriaValue);

        //When each function is called return the temporarily created elements above
        when(advancedFindMock.getCorrespondingCriteriaList(graphElementType)).thenReturn(criteriaPaneList);
        when(advancedFindMock.getCriteriaValues(criteriaPaneList)).thenReturn(findCriteriaValues);
        when(advancedFindMock.getLookForChoiceBox()).thenReturn(lookForChoiceBox);
        when(advancedFindMock.getDeleteResultsButton()).thenReturn(mockButton);

        //Do real call on findAllAction
        doCallRealMethod().when(advancedFindMock).findAllAction();
        //Do nothing on updateAdvancedSearchParameters()
        doNothing().when(advancedFindMock).updateAdvancedSearchParameters(graphElementType);

        /**
         * Create a static mock of the FindViewController. Call the
         * findAllAction() then verify that updateAdvancedSearchParameters and
         * retrieveAdvancedSearch were all called once.
         */
        try (MockedStatic<FindViewController> mockedStatic = Mockito.mockStatic(FindViewController.class)) {
            mockedStatic.when(() -> FindViewController.getDefault()).thenReturn(mockController);

            advancedFindMock.findAllAction();
            verify(advancedFindMock, times(1)).updateAdvancedSearchParameters(graphElementType);
            verify(mockController, times(1)).retrieveAdvancedSearch(true, false);
        }
    }

    /**
     * Test of findNextAction method, of class AdvancedFindTab.
     */
    @Test
    public void testFindNextAction() {
        System.out.println("findNextAction");

        setupGraph();

        //Create a controller mock and do nothing on retriveMatchingElements()
        FindViewController mockController = mock(FindViewController.class);
        mockController.init(spyTopComponent);
        doNothing().when(mockController).retrieveAdvancedSearch(Mockito.eq(false), Mockito.eq(true));

        GraphElementType graphElementType = GraphElementType.VERTEX;

        /**
         * Create temporary advancedFindMock, pane, criteriaPaneList,
         * LookForChoiceBox, findCriteriaValuesList and a stringCriteriaValue
         */
        AdvancedFindTab advancedFindMock = mock(AdvancedFindTab.class);

        StringCriteriaPanel tempPane = new StringCriteriaPanel(advancedTab, "Identifer", graphElementType);
        tempPane.setSearchFieldText("hello");

        final List<AdvancedCriteriaBorderPane> criteriaPaneList = new ArrayList<>();
        criteriaPaneList.add(tempPane);

        final ChoiceBox<String> lookForChoiceBox = new ChoiceBox<>();
        lookForChoiceBox.getItems().add("Node");
        lookForChoiceBox.getSelectionModel().select(0);

        final List<FindCriteriaValues> findCriteriaValues = new ArrayList<>();
        final StringCriteriaValues stringCriteriaValue = new StringCriteriaValues("string", "Identifer", "Is", "hello", false, false);
        findCriteriaValues.add(stringCriteriaValue);

        //When each function is called return the temporarily created elements above
        when(advancedFindMock.getCorrespondingCriteriaList(graphElementType)).thenReturn(criteriaPaneList);
        when(advancedFindMock.getCriteriaValues(criteriaPaneList)).thenReturn(findCriteriaValues);
        when(advancedFindMock.getLookForChoiceBox()).thenReturn(lookForChoiceBox);

        //Do real call on findAllAction
        doCallRealMethod().when(advancedFindMock).findNextAction();
        //Do nothing on updateAdvancedSearchParameters()
        doNothing().when(advancedFindMock).updateAdvancedSearchParameters(graphElementType);

        /**
         * Create a static mock of the FindViewController. Call the
         * findNextAction() then verify that updateAdvancedSearchParameters and
         * retrieveAdvancedSearch were all called once.
         */
        try (MockedStatic<FindViewController> mockedStatic = Mockito.mockStatic(FindViewController.class)) {
            mockedStatic.when(() -> FindViewController.getDefault()).thenReturn(mockController);

            advancedFindMock.findNextAction();
            verify(advancedFindMock, times(1)).updateAdvancedSearchParameters(graphElementType);
            verify(mockController, times(1)).retrieveAdvancedSearch(false, true);
        }
    }

    /**
     * Test of findPreviousAction method, of class AdvancedFindTab.
     */
    @Test
    public void testFindPreviousAction() {
        System.out.println("findPreviousAction");

        setupGraph();

        //Create a controller mock and do nothing on retriveMatchingElements()
        FindViewController mockController = mock(FindViewController.class);
        mockController.init(spyTopComponent);
        doNothing().when(mockController).retrieveAdvancedSearch(Mockito.eq(false), Mockito.eq(false));

        GraphElementType graphElementType = GraphElementType.VERTEX;

        /**
         * Create temporary advancedFindMock, pane, criteriaPaneList,
         * LookForChoiceBox, findCriteriaValuesList and a stringCriteriaValue
         */
        AdvancedFindTab advancedFindMock = mock(AdvancedFindTab.class);

        StringCriteriaPanel tempPane = new StringCriteriaPanel(advancedTab, "Identifer", graphElementType);
        tempPane.setSearchFieldText("hello");

        final List<AdvancedCriteriaBorderPane> criteriaPaneList = new ArrayList<>();
        criteriaPaneList.add(tempPane);

        final ChoiceBox<String> lookForChoiceBox = new ChoiceBox<>();
        lookForChoiceBox.getItems().add("Node");
        lookForChoiceBox.getSelectionModel().select(0);

        final List<FindCriteriaValues> findCriteriaValues = new ArrayList<>();
        final StringCriteriaValues stringCriteriaValue = new StringCriteriaValues("string", "Identifer", "Is", "hello", false, false);
        findCriteriaValues.add(stringCriteriaValue);

        //When each function is called return the temporarily created elements above
        when(advancedFindMock.getCorrespondingCriteriaList(graphElementType)).thenReturn(criteriaPaneList);
        when(advancedFindMock.getCriteriaValues(criteriaPaneList)).thenReturn(findCriteriaValues);
        when(advancedFindMock.getLookForChoiceBox()).thenReturn(lookForChoiceBox);

        //Do real call on findAllAction
        doCallRealMethod().when(advancedFindMock).findPreviousAction();
        //Do nothing on updateAdvancedSearchParameters()
        doNothing().when(advancedFindMock).updateAdvancedSearchParameters(graphElementType);

        /**
         * Create a static mock of the FindViewController. Call the
         * findNextAction() then verify that updateAdvancedSearchParameters and
         * retrieveAdvancedSearch were all called once.
         */
        try (MockedStatic<FindViewController> mockedStatic = Mockito.mockStatic(FindViewController.class)) {
            mockedStatic.when(() -> FindViewController.getDefault()).thenReturn(mockController);

            advancedFindMock.findPreviousAction();
            verify(advancedFindMock, times(1)).updateAdvancedSearchParameters(graphElementType);
            verify(mockController, times(1)).retrieveAdvancedSearch(false, false);
        }
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

            selectedT = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);
            labelT = VisualConcept.TransactionAttribute.LABEL.ensure(wg);
            identiferT = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(wg);
            widthT = VisualConcept.TransactionAttribute.WIDTH.ensure(wg);

            vxId1 = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId1, false);
            wg.setStringValue(labelV, vxId1, "label name");
            wg.setStringValue(identifierV, vxId1, "identifer name");
            wg.setIntValue(xV, vxId1, 1);

            wg.commit();

        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        }
    }

}
