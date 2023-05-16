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
import au.gov.asd.tac.constellation.views.find2.utilities.BasicFindReplaceParameters;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Atlas139mkm
 */
public class ReplaceTabNGTest {

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
    ReplaceTab spyReplaceTab;
    FindViewPane findViewPane;
    FindViewTabs findViewTabs;

    private static final Logger LOGGER = Logger.getLogger(ReplaceTabNGTest.class.getName());

    public ReplaceTabNGTest() {
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

        basicFindTab = mock(BasicFindTab.class);

        when(findViewTabs.getParentComponent()).thenReturn(findViewPane);
        when(findViewPane.getTabs()).thenReturn(findViewTabs);
        when(findViewTabs.getBasicFindTab()).thenReturn(basicFindTab);
        when(findViewTabs.getReplaceTab()).thenReturn(replaceTab);
        replaceTab = new ReplaceTab(findViewTabs);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of updateButtons method, of class ReplaceTab.
     */
    @Test
    public void testUpdateButtons() {
        System.out.println("updateButtons");

        replaceTab.buttonsHBox.getChildren().clear();
        replaceTab.buttonsHBox.getChildren().add(new Button("test"));

        /**
         * The updateButtons function should clear the existing elements (The
         * button added above) and add the replaceNextButton, replaceAllButton,
         * and getSearchAllGraphs checkbox.
         */
        replaceTab.updateButtons();
        assertEquals(replaceTab.buttonsHBox.getChildren().get(0), replaceTab.getReplaceAllButton());
        assertEquals(replaceTab.buttonsHBox.getChildren().get(1), replaceTab.getReplaceNextButton());
    }

    /**
     * Test of updateBasicReplaceParamters method, of class ReplaceTab.
     */
    @Test
    public void testUpdateBasicReplaceParamters() {
        System.out.println("updateBasicReplaceParamters");

        final BasicFindReplaceParameters controlllerParameters = FindViewController.getDefault().getCurrentBasicReplaceParameters();
        replaceTab.lookForChoiceBox.getSelectionModel().select(0);
        replaceTab.getFindTextField().setText("test");
        replaceTab.getReplaceTextField().setText("replace");
        final GraphElementType elementType = GraphElementType.getValue(replaceTab.lookForChoiceBox.getSelectionModel().getSelectedItem());

        replaceTab.postSearchChoiceBox.getSelectionModel().select(0);

        /**
         * Call the updateBasicFindParamters function. Check that each of the
         * javaFX elements passes their corresponding data correctly to the
         * controllers basicFindParamters
         */
        replaceTab.updateBasicReplaceParamters();

        /**
         * All parameters should equal the current value of the basicFindTabs
         * elements
         */
        assertEquals(controlllerParameters.getFindString(), replaceTab.getFindTextField().getText());
        assertEquals(controlllerParameters.getReplaceString(), replaceTab.getReplaceTextField().getText());
        assertEquals(controlllerParameters.getGraphElement(), elementType);
        assertEquals(controlllerParameters.getAttributeList(), replaceTab.getMatchingAttributeList(elementType));
        assertEquals(controlllerParameters.isStandardText(), replaceTab.standardRadioBtn.isSelected());
        assertEquals(controlllerParameters.isRegEx(), replaceTab.regExBtn.isSelected());
        assertEquals(controlllerParameters.isIgnoreCase(), replaceTab.ignoreCaseCB.isSelected());

        /**
         * All 4 should be false as currentSelectionChoiceBox is set to select
         * index 0 which is "Ignore"
         */
      //  assertEquals(controlllerParameters.isFindIn(), false);
        assertEquals(controlllerParameters.isAddTo(), false);
        assertEquals(controlllerParameters.isRemoveFrom(), false);
        assertEquals(controlllerParameters.isReplaceIn(), false);
    }

    /**
     * Test of replaceAllAction method, of class ReplaceTab.
     */
    @Test
    public void testReplaceAllAction() {
        System.out.println("replaceAllAction");

        setupGraph();

        //Create a controller mock and do nothing on retriveMatchingElements()
        FindViewController mockController = mock(FindViewController.class);
        mockController.init(spyTopComponent);
        doNothing().when(mockController).replaceMatchingElements(Mockito.eq(true), Mockito.eq(false));

        /**
         * Create a basicFindMock and adds a temporary choice box and textFild
         * for the functions to work.
         */
        ReplaceTab replaceMock = mock(ReplaceTab.class);
        final ChoiceBox<String> lookForChoiceBox = new ChoiceBox<>();
        lookForChoiceBox.getItems().add("Node");
        lookForChoiceBox.getSelectionModel().select(0);
        final TextField findTextField = new TextField("test");
        final TextField repalceTextField = new TextField("replace");

        //Mock the getters to return the newly made java fx element.
        when(replaceMock.getLookForChoiceBox()).thenReturn(lookForChoiceBox);
        when(replaceMock.getFindTextField()).thenReturn(findTextField);
        when(replaceMock.getReplaceTextField()).thenReturn(repalceTextField);

        //Do nothing on saveSelected() and updateBasicFindParamters()
        doCallRealMethod().when(replaceMock).replaceAllAction();
        doNothing().when(replaceMock).saveSelected(Mockito.any());
        doNothing().when(replaceMock).updateBasicReplaceParamters();

        /**
         * Create a static mock of the FindViewController. Call the
         * findAllAction() then verify that saveSelected,
         * updateBasicFindParameters and retrieveMatchingElements were all
         * called once.
         */
        try (MockedStatic<FindViewController> mockedStatic = Mockito.mockStatic(FindViewController.class)) {
            mockedStatic.when(() -> FindViewController.getDefault()).thenReturn(mockController);

            replaceMock.replaceAllAction();

            verify(replaceMock, times(1)).saveSelected(Mockito.eq(GraphElementType.VERTEX));
            verify(replaceMock, times(1)).updateBasicReplaceParamters();
            verify(mockController, times(1)).replaceMatchingElements(true, false);
        }

    }

    /**
     * Test of replaceNextAction method, of class ReplaceTab.
     */
    @Test
    public void testReplaceNextAction() {
        System.out.println("replaceNextAction");

        setupGraph();

        //Create a controller mock and do nothing on retriveMatchingElements()
        FindViewController mockController = mock(FindViewController.class);
        mockController.init(spyTopComponent);
        doNothing().when(mockController).replaceMatchingElements(Mockito.eq(false), Mockito.eq(true));

        /**
         * Create a basicFindMock and adds a temporary choice box and textFild
         * for the functions to work.
         */
        ReplaceTab replaceMock = mock(ReplaceTab.class);
        final ChoiceBox<String> lookForChoiceBox = new ChoiceBox<>();
        lookForChoiceBox.getItems().add("Node");
        lookForChoiceBox.getSelectionModel().select(0);
        final TextField findTextField = new TextField("test");
        final TextField repalceTextField = new TextField("replace");

        //Mock the getters to return the newly made java fx element.
        when(replaceMock.getLookForChoiceBox()).thenReturn(lookForChoiceBox);
        when(replaceMock.getFindTextField()).thenReturn(findTextField);
        when(replaceMock.getReplaceTextField()).thenReturn(repalceTextField);

        //Do nothing on saveSelected() and updateBasicFindParamters()
        doCallRealMethod().when(replaceMock).replaceNextAction();
        doNothing().when(replaceMock).saveSelected(Mockito.any());
        doNothing().when(replaceMock).updateBasicReplaceParamters();

        /**
         * Create a static mock of the FindViewController. Call the
         * findAllAction() then verify that saveSelected,
         * updateBasicFindParameters and retrieveMatchingElements were all
         * called once.
         */
        try (MockedStatic<FindViewController> mockedStatic = Mockito.mockStatic(FindViewController.class)) {
            mockedStatic.when(() -> FindViewController.getDefault()).thenReturn(mockController);

            replaceMock.replaceNextAction();

            verify(replaceMock, times(1)).saveSelected(Mockito.eq(GraphElementType.VERTEX));
            verify(replaceMock, times(1)).updateBasicReplaceParamters();
            verify(mockController, times(1)).replaceMatchingElements(false, true);
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

            /**
             * Get the label and the identifier vertex attributes and add them
             * to the attributes list
             */
            GraphElementType elementType = GraphElementType.VERTEX;
            // The label attribute
            int attributeInt = wg.getAttribute(elementType, 1);
            labelAttributeV = new GraphAttribute(wg, attributeInt);
            replaceTab.attributes.add(labelAttributeV);
            // The identifier attribute
            attributeInt = wg.getAttribute(elementType, 2);
            identifierAttributeV = new GraphAttribute(wg, attributeInt);
            replaceTab.attributes.add(identifierAttributeV);

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
