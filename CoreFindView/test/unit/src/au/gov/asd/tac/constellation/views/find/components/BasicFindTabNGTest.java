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
package au.gov.asd.tac.constellation.views.find.components;

import au.gov.asd.tac.constellation.views.find.components.BasicFindTab;
import au.gov.asd.tac.constellation.views.find.components.ReplaceTab;
import au.gov.asd.tac.constellation.views.find.components.FindViewTabs;
import au.gov.asd.tac.constellation.views.find.components.FindViewPane;
import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.views.find.FindViewController;
import au.gov.asd.tac.constellation.views.find.FindViewTopComponent;
import au.gov.asd.tac.constellation.views.find.utilities.BasicFindReplaceParameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
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
import static org.testng.Assert.assertSame;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Atlas139mkm
 */
public class BasicFindTabNGTest {

    private Map<String, Graph> graphMap = new HashMap<>();
    private Graph graph;
    private Graph graph2;
    private GraphAttribute labelAttributeV, identifierAttributeV, labelAttributeT, identifierAttributeT;

    private int selectedV, selectedT;
    private int labelV, identifierV, xV, labelT, identiferT, widthT;
    private int vxId1, vxId2, vxId3, vxId4, vxId5UpperCase, vxId6, vxId7, vxId8, txId1, txId2, txId3, txId4;

    FindViewTopComponent findViewTopComponent;
    FindViewTopComponent spyTopComponent;

//    FindViewController findViewController;
    BasicFindTab basicFindTab;
    ReplaceTab replaceTab;
    FindViewPane findViewPane;
    FindViewTabs findViewTabs;
    private static final Logger LOGGER = Logger.getLogger(BasicFindTabNGTest.class.getName());

    public BasicFindTabNGTest() {
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
        findViewTopComponent = mock(FindViewTopComponent.class);
        spyTopComponent = spy(findViewTopComponent);

        findViewPane = mock(FindViewPane.class);
        findViewTabs = mock(FindViewTabs.class);
        FindViewController.getDefault();

        replaceTab = mock(ReplaceTab.class);

        when(findViewTabs.getParentComponent()).thenReturn(findViewPane);
        when(findViewPane.getTabs()).thenReturn(findViewTabs);
        when(findViewTabs.getBasicFindTab()).thenReturn(basicFindTab);
        when(findViewTabs.getReplaceTab()).thenReturn(replaceTab);

        basicFindTab = new BasicFindTab(findViewTabs);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getParentComponent method, of class BasicFindTab.
     */
    @Test
    public void testGetParentComponent() {
        System.out.println("getParentComponent");

        assertEquals(basicFindTab.getParentComponent(), findViewTabs);
    }

    /**
     * Test of updateButtons method, of class BasicFindTab.
     */
    @Test
    public void testUpdateButtons() {
        System.out.println("updateButtons");

        basicFindTab.buttonsHBox.getChildren().clear();
        basicFindTab.buttonsHBox.getChildren().add(new Button("test"));

        /**
         * The updateButtons function should clear the existing elements (The
         * button added above) and add the findPrevButton, findNextButton,
         * getFindAllButton and getSearchAllGraphs checkbox.
         */
        basicFindTab.updateButtons();
        assertEquals(basicFindTab.buttonsHBox.getChildren().get(0), basicFindTab.getDeleteResultsButton());
        assertEquals(basicFindTab.buttonsHBox.getChildren().get(1), basicFindTab.getFindAllButton());
        assertEquals(basicFindTab.buttonsHBox.getChildren().get(2), basicFindTab.getFindPrevButton());
        assertEquals(basicFindTab.buttonsHBox.getChildren().get(3), basicFindTab.getFindNextButton());
    }

    /**
     * Test of populateAttributes method, of class BasicFindTab.
     */
    @Test
    public void testPopulateAttributes() {
        System.out.println("populateAttributes");

        setupGraph();

        /**
         * Add the vertex label and identifier attribute to the selected vertex
         * attribute list
         */
        basicFindTab.selectedNodeAttributes.add(labelAttributeV);
        basicFindTab.selectedNodeAttributes.add(identifierAttributeV);

        /**
         * Add the transaction label attribute to the selected vertex attribute
         * list
         */
        basicFindTab.selectedTransAttributes.add(labelAttributeT);

        final GraphManager gm = Mockito.mock(GraphManager.class);
        when(gm.getAllGraphs()).thenReturn(graphMap);

        try (MockedStatic<GraphManager> mockedStatic = Mockito.mockStatic(GraphManager.class)) {
            mockedStatic.when(() -> GraphManager.getDefault()).thenReturn(gm);

            // Call the populateAttributes function with the type VERTEX
            GraphElementType elementType = GraphElementType.VERTEX;
            basicFindTab.populateAttributes(elementType);

            /**
             * As both the label and identifier attributes were in the selected
             * vertex attribute list. Both should be checked in the
             * inAttributesMenu
             */
            assertEquals(basicFindTab.attributeFilterMultiChoiceInput.getCheckModel().isChecked(labelAttributeV.getName()), true);
            assertEquals(basicFindTab.attributeFilterMultiChoiceInput.getCheckModel().isChecked(identifierAttributeV.getName()), true);

            /**
             * Clear the current selections, and calls the populateAttribute
             * function with Transactions
             */
            basicFindTab.attributeFilterMultiChoiceInput.getCheckModel().clearChecks();
            elementType = GraphElementType.TRANSACTION;
            basicFindTab.populateAttributes(elementType);

            /**
             * As only the label attribute was selected in the selected
             * transaction attributes list. Only the label should be selected
             */
            assertEquals(basicFindTab.attributeFilterMultiChoiceInput.getCheckModel().isChecked(labelAttributeT.getName()), true);
            assertEquals(basicFindTab.attributeFilterMultiChoiceInput.getCheckModel().isChecked(identifierAttributeT.getName()), false);

            /**
             * Repeat the same for vertex again to show the vertex attributes
             * remain selected when calling the populateAttribute function
             */
            basicFindTab.attributeFilterMultiChoiceInput.getCheckModel().clearChecks();
            elementType = GraphElementType.VERTEX;
            basicFindTab.populateAttributes(elementType);

            assertEquals(basicFindTab.attributeFilterMultiChoiceInput.getCheckModel().isChecked(labelAttributeV.getName()), true);
            assertEquals(basicFindTab.attributeFilterMultiChoiceInput.getCheckModel().isChecked(identifierAttributeV.getName()), true);

        }
    }

    /**
     * Test of updateSelectedAttributes method, of class BasicFindTab.
     */
    @Test
    public void testUpdateSelectedAttributes() {
        System.out.println("updateSelectedAttributes");

        setupGraph();

        // Create a list and add the first index of the attributes list (label)
        List<Attribute> selectedAttributes = new ArrayList<>();
        selectedAttributes.add(basicFindTab.attributes.get(0));

        // Add both the label and identifier attribute to inAttributeMenu
        basicFindTab.attributeFilterMultiChoiceInput.getItems().add(basicFindTab.attributes.get(0).getName());
        basicFindTab.attributeFilterMultiChoiceInput.getItems().add(basicFindTab.attributes.get(1).getName());

        /**
         * Call the updateSelectedAttributes method. This should check the check
         * boxes of all matching attributes from the list as a parameter
         */
        basicFindTab.updateSelectedAttributes(selectedAttributes);

        /**
         * Create a blank run later to guarantee the updateSelectedAttributes
         * function is called before assertEquals at the end
         */
        final CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            System.out.println("Queued platform task for test");
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        /**
         * In the inAttributeMenu the label attribute (index 0) should be
         * checked and the identifier should not
         */
        assertEquals(basicFindTab.attributeFilterMultiChoiceInput.getCheckModel().isChecked(basicFindTab.attributes.get(0).getName()), true);
        assertEquals(basicFindTab.attributeFilterMultiChoiceInput.getCheckModel().isChecked(basicFindTab.attributes.get(1).getName()), false);
    }

    /**
     * Test of getMatchingAttributeList method, of class BasicFindTab.
     */
    @Test

    public void testGetMatchingAttributeList() {
        System.out.println("getMatchingAttributeList");

        /**
         * Check each of the graph element types and call the
         * getMatchingAttributeList function to see if it returns the correct
         * corresponding list.
         */
        GraphElementType type = GraphElementType.VERTEX;
        assertSame(basicFindTab.getMatchingAttributeList(type), basicFindTab.selectedNodeAttributes);

        type = GraphElementType.TRANSACTION;
        assertSame(basicFindTab.getMatchingAttributeList(type), basicFindTab.selectedTransAttributes);

        type = GraphElementType.EDGE;
        assertSame(basicFindTab.getMatchingAttributeList(type), basicFindTab.selectedEdgeAttributes);

        type = GraphElementType.LINK;
        assertSame(basicFindTab.getMatchingAttributeList(type), basicFindTab.selectedLinkAttributes);

    }

    /**
     * Test of saveSelected method, of class BasicFindTab.
     */
    @Test
    public void testSaveSelected() {
        System.out.println("saveSelected");
        basicFindTab.onLoad = false;
        setupGraph();

        /**
         * Add the label and identifier attributes to the check box list and
         * check the label attribute in the checkBoxMenu inAttributesMenu
         */
        basicFindTab.attributeFilterMultiChoiceInput.getItems().add(basicFindTab.attributes.get(0).getName());
        basicFindTab.attributeFilterMultiChoiceInput.getItems().add(basicFindTab.attributes.get(1).getName());
        basicFindTab.attributeFilterMultiChoiceInput.getCheckModel().check(basicFindTab.attributes.get(0).getName());

        /**
         * Save the selected attributes in the inAttributeMenu to the
         * corresponding attribute list. In this case its the selectedNodeList.
         */
        final GraphElementType elementType = GraphElementType.VERTEX;
        basicFindTab.saveSelected(elementType);

        /**
         * The selectedNodeAttribute list should be of size one since only the
         * label attribute was set as selected. The attribute in index 0 should
         * also be the label attribute.
         */
        assertEquals(basicFindTab.selectedNodeAttributes.size(), 1);
        assertEquals(basicFindTab.selectedNodeAttributes.get(0).getName(), basicFindTab.attributes.get(0).getName());

    }

    /**
     * This test should test the updateBasicFindParamters function. It should
     * take all the UI element selections by the user and update the
     * basicFindParameters in the controller. To check this works the
     * controllers parameters should be the same as the UI elements.
     */
    @Test
    public void testUpdateBasicFindParamters() {
        System.out.println("updateBasicFindParamters");

        final BasicFindReplaceParameters controlllerParameters = FindViewController.getDefault().getCurrentBasicFindParameters();
        basicFindTab.lookForChoiceBox.getSelectionModel().select(0);
        basicFindTab.getFindTextField().setText("test");
        final GraphElementType elementType = GraphElementType.getValue(basicFindTab.lookForChoiceBox.getSelectionModel().getSelectedItem());

        basicFindTab.postSearchChoiceBox.getSelectionModel().select(0);

        /**
         * Call the updateBasicFindParamters function. Check that each of the
         * javaFX elements passes their corresponding data correctly to the
         * controllers basicFindParamters
         */
        basicFindTab.updateBasicFindParamters();

        /**
         * All parameters should equal the current value of the basicFindTabs
         * elements
         */
        assertEquals(controlllerParameters.getFindString(), basicFindTab.getFindTextField().getText());
        assertEquals(controlllerParameters.getReplaceString(), "");
        assertEquals(controlllerParameters.getGraphElement(), elementType);
        assertEquals(controlllerParameters.getAttributeList(), basicFindTab.getMatchingAttributeList(elementType));
        assertEquals(controlllerParameters.isStandardText(), basicFindTab.standardRadioBtn.isSelected());
        assertEquals(controlllerParameters.isRegEx(), basicFindTab.regExBtn.isSelected());
        assertEquals(controlllerParameters.isIgnoreCase(), basicFindTab.ignoreCaseCB.isSelected());

        /**
         * All 4 should be false as currentSelectionChoiceBox is set to select
         * index 0 which is "Ignore"
         */
        //assertEquals(controlllerParameters.isFindIn(), false);
        assertEquals(controlllerParameters.isAddTo(), false);
        assertEquals(controlllerParameters.isRemoveFrom(), false);
        assertEquals(controlllerParameters.isReplaceIn(), false);

    }

    /**
     * Test of updateSelectionFactors method, of class BasicFindTab.
     */
    @Test
    public void testUpdateSelectionFactors() {
        System.out.println("updateSelectionFactors");

        /**
         * Check the button status properly updates based on the
         * currentSelectionChoice box Index
         */
        // Ignore
        basicFindTab.postSearchChoiceBox.getSelectionModel().select(0);
        basicFindTab.updateSelectionFactors();
        assertEquals(basicFindTab.getFindNextButton().isDisable(), false);
        assertEquals(basicFindTab.getFindPrevButton().isDisable(), false);

        // Add to
        basicFindTab.postSearchChoiceBox.getSelectionModel().select(1);
        basicFindTab.updateSelectionFactors();
        assertEquals(basicFindTab.getFindNextButton().isDisable(), true);
        assertEquals(basicFindTab.getFindPrevButton().isDisable(), true);

        // Find In
        // Should disable both buttons when selecting find In currentSelection
        basicFindTab.postSearchChoiceBox.getSelectionModel().select(2);
        basicFindTab.updateSelectionFactors();
        assertEquals(basicFindTab.getFindNextButton().isDisable(), true);
        assertEquals(basicFindTab.getFindPrevButton().isDisable(), true);

        // Replace in
        basicFindTab.postSearchChoiceBox.getSelectionModel().select(3);
        basicFindTab.updateSelectionFactors();
        assertEquals(basicFindTab.getFindNextButton().isDisable(), true);
        assertEquals(basicFindTab.getFindPrevButton().isDisable(), true);
    }

    /**
     * Test of findAllAction method, of class BasicFindTab.
     */
    @Test
    public void testFindAllAction() {
        System.out.println("findAllAction");

        setupGraph();

        //Create a controller mock and do nothing on retriveMatchingElements()
        FindViewController mockController = mock(FindViewController.class);
        mockController.init(spyTopComponent);
        doNothing().when(mockController).retriveMatchingElements(Mockito.eq(true), Mockito.eq(false));
        Button mockButton = mock(Button.class);

        /**
         * Create a basicFindMock and adds a temporary choice box and textFild
         * for the functions to work.
         */
        BasicFindTab basicFindMock = mock(BasicFindTab.class);
        final ChoiceBox<String> lookForChoiceBox = new ChoiceBox<>();
        lookForChoiceBox.getItems().add("Node");
        lookForChoiceBox.getSelectionModel().select(0);
        final TextField findTextField = new TextField("test");

        //Mock the getters to return the newly made java fx element.
        when(basicFindMock.getLookForChoiceBox()).thenReturn(lookForChoiceBox);
        when(basicFindMock.getFindTextField()).thenReturn(findTextField);

        //Do nothing on saveSelected() and updateBasicFindParamters()
        doCallRealMethod().when(basicFindMock).findAllAction();
        doNothing().when(basicFindMock).saveSelected(Mockito.any());
        doNothing().when(basicFindMock).updateBasicFindParamters();
        when(basicFindMock.getDeleteResultsButton()).thenReturn(mockButton);

        /**
         * Create a static mock of the FindViewController. Call the
         * findAllAction() then verify that saveSelected,
         * updateBasicFindParameters and retrieveMatchingElements were all
         * called once.
         */
        try (MockedStatic<FindViewController> mockedStatic = Mockito.mockStatic(FindViewController.class)) {
            mockedStatic.when(() -> FindViewController.getDefault()).thenReturn(mockController);

            basicFindMock.findAllAction();

            verify(basicFindMock, times(1)).saveSelected(Mockito.eq(GraphElementType.VERTEX));
            verify(basicFindMock, times(1)).updateBasicFindParamters();
            verify(mockController, times(1)).retriveMatchingElements(true, false);
        }
    }

    /**
     * Test of findNextAction method, of class BasicFindTab.
     */
    @Test
    public void testFindNextAction() {
        System.out.println("findNextAction");

        // Refer to testFindAllAction for comments
        setupGraph();

        FindViewController mockController = mock(FindViewController.class);
        mockController.init(spyTopComponent);
        doNothing().when(mockController).retriveMatchingElements(Mockito.eq(false), Mockito.eq(true));

        BasicFindTab basicFindMock = mock(BasicFindTab.class);

        final ChoiceBox<String> lookForChoiceBox = new ChoiceBox<>();
        lookForChoiceBox.getItems().add("Node");
        lookForChoiceBox.getSelectionModel().select(0);
        final TextField findTextField = new TextField("test");

        when(basicFindMock.getLookForChoiceBox()).thenReturn(lookForChoiceBox);
        when(basicFindMock.getFindTextField()).thenReturn(findTextField);

        doCallRealMethod().when(basicFindMock).findNextAction();
        doNothing().when(basicFindMock).saveSelected(Mockito.any());
        doNothing().when(basicFindMock).updateBasicFindParamters();

        try (MockedStatic<FindViewController> mockedStatic = Mockito.mockStatic(FindViewController.class)) {
            mockedStatic.when(() -> FindViewController.getDefault()).thenReturn(mockController);

            basicFindMock.findNextAction();

            verify(basicFindMock, times(1)).saveSelected(Mockito.eq(GraphElementType.VERTEX));
            verify(basicFindMock, times(1)).updateBasicFindParamters();
            verify(mockController, times(1)).retriveMatchingElements(false, true);
        }
    }

    /**
     * Test of findPrevAction method, of class BasicFindTab.
     */
    @Test
    public void testFindPrevAction() {
        System.out.println("findPrevAction");

        // Refer to testFindAllAction for comments
        setupGraph();

        FindViewController mockController = mock(FindViewController.class);
        mockController.init(spyTopComponent);
        doNothing().when(mockController).retriveMatchingElements(Mockito.eq(false), Mockito.eq(false));

        BasicFindTab basicFindMock = mock(BasicFindTab.class);

        final ChoiceBox<String> lookForChoiceBox = new ChoiceBox<>();
        lookForChoiceBox.getItems().add("Node");
        lookForChoiceBox.getSelectionModel().select(0);
        final TextField findTextField = new TextField("test");

        when(basicFindMock.getLookForChoiceBox()).thenReturn(lookForChoiceBox);
        when(basicFindMock.getFindTextField()).thenReturn(findTextField);

        doCallRealMethod().when(basicFindMock).findPrevAction();
        doNothing().when(basicFindMock).saveSelected(Mockito.any());
        doNothing().when(basicFindMock).updateBasicFindParamters();

        try (MockedStatic<FindViewController> mockedStatic = Mockito.mockStatic(FindViewController.class)) {
            mockedStatic.when(() -> FindViewController.getDefault()).thenReturn(mockController);

            basicFindMock.findPrevAction();

            verify(basicFindMock, times(1)).saveSelected(Mockito.eq(GraphElementType.VERTEX));
            verify(basicFindMock, times(1)).updateBasicFindParamters();
            verify(mockController, times(1)).retriveMatchingElements(false, false);
        }
    }

    /**
     * Test of requestTextFieldFocus method, of class BasicFindTab.
     */
    @Test
    public void testRequestTextFieldFocus() {
        System.out.println("requestTextFieldFocus");

//        setupGraph();
//
//        basicFindTab.requestTextFieldFocus();
//        assertEquals(basicFindTab.getFindTextField().isFocused(), true);
//        basicFindTab.getFindTextField().requestFocus();
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

            /**
             * Get the label and the identifier vertex attributes and add them
             * to the attributes list
             */
            GraphElementType elementType = GraphElementType.VERTEX;
            // The label attribute
            int attributeInt = wg.getAttribute(elementType, 1);
            labelAttributeV = new GraphAttribute(wg, attributeInt);
            basicFindTab.attributes.add(labelAttributeV);
            // The identifier attribute
            attributeInt = wg.getAttribute(elementType, 2);
            identifierAttributeV = new GraphAttribute(wg, attributeInt);
            basicFindTab.attributes.add(identifierAttributeV);

            elementType = GraphElementType.TRANSACTION;
            attributeInt = wg.getAttribute(elementType, 1);
            labelAttributeT = new GraphAttribute(wg, attributeInt);

            attributeInt = wg.getAttribute(elementType, 2);
            identifierAttributeT = new GraphAttribute(wg, attributeInt);

            wg.commit();

        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        }
    }

}
