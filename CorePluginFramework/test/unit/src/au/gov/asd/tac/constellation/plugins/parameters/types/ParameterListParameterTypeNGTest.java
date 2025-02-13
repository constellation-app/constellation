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
package au.gov.asd.tac.constellation.plugins.parameters.types;

import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterListParameterType.ParameterList;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterListParameterType.ParameterListLockingPluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterListParameterType.ParameterListParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterListParameterType.PluginPaneFactory;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.layout.Pane;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
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
public class ParameterListParameterTypeNGTest {

    private static final Logger LOGGER = Logger.getLogger(ParameterListParameterTypeNGTest.class.getName());
    
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
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of build method, of class ParameterListParameterType.
     */
    @Test
    public void testBuild() {
        System.out.println("build");
        
        final PluginParameter<ParameterListParameterValue> parameterListParam = ParameterListParameterType.build("My Parameter List");
        
        assertEquals(parameterListParam.getId(), "My Parameter List");
        assertEquals(parameterListParam.getType().getId(), "parameterlist");
    }

    /**
     * Test of setPrototypeParameters method, of class ParameterListParameterType.
     */
    @Test
    public void testSetPrototypeParameters() {
        System.out.println("setPrototypeParameters");
        
        final PluginParameter<ParameterListParameterValue> parameterListParam = ParameterListParameterType.build("My Parameter List");
        assertNull(parameterListParam.getParameterValue().get());
        
        ParameterListParameterType.setPrototypeParameters(parameterListParam, new PluginParameters());
        assertNotNull(parameterListParam.getParameterValue().get());
    }

    /**
     * Test of addToList method, of class ParameterListParameterType.
     */
    @Test
    public void testAddToList() {
        System.out.println("addToList");
        
        final PluginParameter<ParameterListParameterValue> parameterListParam = ParameterListParameterType.build("My Parameter List");
        ParameterListParameterType.setPrototypeParameters(parameterListParam, new PluginParameters());
        assertTrue(parameterListParam.getParameterValue().getListOfPluginParameters().isEmpty());
        assertTrue(parameterListParam.getParameterValue().getPanes().isEmpty());
        
        final Pane pane = ParameterListParameterType.addToList(parameterListParam);
        assertEquals(parameterListParam.getParameterValue().getListOfPluginParameters().size(), 1);
        assertEquals(parameterListParam.getParameterValue().getPanes().size(), 1);
        assertTrue(parameterListParam.getParameterValue().getPanes().contains(pane));
        
    }

    /**
     * Test of removeFromList method, of class ParameterListParameterType.
     */
    @Test
    public void testRemoveFromList() {
        System.out.println("removeFromList");
        
        final PluginParameter<ParameterListParameterValue> parameterListParam = ParameterListParameterType.build("My Parameter List");
        ParameterListParameterType.setPrototypeParameters(parameterListParam, new PluginParameters());        
        final Pane pane = ParameterListParameterType.addToList(parameterListParam);
        assertEquals(parameterListParam.getParameterValue().getListOfPluginParameters().size(), 1);
        assertEquals(parameterListParam.getParameterValue().getPanes().size(), 1);
        assertTrue(parameterListParam.getParameterValue().getPanes().contains(pane));
        
        final Pane removedPane = ParameterListParameterType.removeFromList(parameterListParam, pane);
        assertTrue(parameterListParam.getParameterValue().getListOfPluginParameters().isEmpty());
        assertTrue(parameterListParam.getParameterValue().getPanes().isEmpty());
        assertEquals(removedPane, pane);
    }

    /**
     * Test of moveUp method, of class ParameterListParameterType.
     */
    @Test
    public void testMoveUp() {
        System.out.println("moveUp");
        
        final PluginParameter<ParameterListParameterValue> parameterListParam = ParameterListParameterType.build("My Parameter List");
        ParameterListParameterType.setPrototypeParameters(parameterListParam, new PluginParameters());        
        final Pane pane1 = ParameterListParameterType.addToList(parameterListParam);
        final Pane pane2 = ParameterListParameterType.addToList(parameterListParam);
        final Pane pane3 = ParameterListParameterType.addToList(parameterListParam);
        assertEquals(parameterListParam.getParameterValue().getPanes().indexOf(pane1), 0);
        assertEquals(parameterListParam.getParameterValue().getPanes().indexOf(pane2), 1);
        assertEquals(parameterListParam.getParameterValue().getPanes().indexOf(pane3), 2);
        
        ParameterListParameterType.moveUp(parameterListParam, pane2);
        assertEquals(parameterListParam.getParameterValue().getPanes().indexOf(pane1), 1);
        assertEquals(parameterListParam.getParameterValue().getPanes().indexOf(pane2), 0);
        assertEquals(parameterListParam.getParameterValue().getPanes().indexOf(pane3), 2);
    }

    /**
     * Test of moveDown method, of class ParameterListParameterType.
     */
    @Test
    public void testMoveDown() {
        System.out.println("moveDown");
        
        final PluginParameter<ParameterListParameterValue> parameterListParam = ParameterListParameterType.build("My Parameter List");
        ParameterListParameterType.setPrototypeParameters(parameterListParam, new PluginParameters());        
        final Pane pane1 = ParameterListParameterType.addToList(parameterListParam);
        final Pane pane2 = ParameterListParameterType.addToList(parameterListParam);
        final Pane pane3 = ParameterListParameterType.addToList(parameterListParam);
        assertEquals(parameterListParam.getParameterValue().getPanes().indexOf(pane1), 0);
        assertEquals(parameterListParam.getParameterValue().getPanes().indexOf(pane2), 1);
        assertEquals(parameterListParam.getParameterValue().getPanes().indexOf(pane3), 2);
        
        ParameterListParameterType.moveDown(parameterListParam, pane2);
        assertEquals(parameterListParam.getParameterValue().getPanes().indexOf(pane1), 0);
        assertEquals(parameterListParam.getParameterValue().getPanes().indexOf(pane2), 2);
        assertEquals(parameterListParam.getParameterValue().getPanes().indexOf(pane3), 1);
    }
    
    /**
     * Test of copy method, of class ParameterListLockingPluginParameters.
     */
    @Test
    public void testCopy() {
        System.out.println("copy");
        
        final ParameterListLockingPluginParameters params = new ParameterListLockingPluginParameters();
        final PluginParameters paramsCopy = params.copy();
        assertTrue(params.getParameters().equals(paramsCopy.getParameters()));
        
        params.addParameter(new PluginParameter<>(new ParameterListParameterValue(), new ParameterListParameterType(), "test"));
        assertFalse(params.getParameters().equals(paramsCopy.getParameters()));
    }
    
    /**
     * Test of startParameterLoading method, of class ParameterListLockingPluginParameters.
     */
    @Test
    public void testStartParameterLoading() {
        System.out.println("startParameterLoading");
        
        final ParameterListLockingPluginParameters params = new ParameterListLockingPluginParameters();
        final PluginParameter<ParameterListParameterValue> parameterListParam = ParameterListParameterType.build("test1");
        ParameterListParameterType.setPrototypeParameters(parameterListParam, new PluginParameters());
        params.addParameter(parameterListParam);
        params.addParameter(StringParameterType.build("test2"));
        
        assertFalse(parameterListParam.getParameterListValue().isLocked());
        assertNull(parameterListParam.getParameterListValue().getCachedValue());
        params.startParameterLoading();
        assertTrue(parameterListParam.getParameterListValue().isLocked());
        assertEquals(parameterListParam.getParameterListValue().getCachedValue(), "");
    }
    
    /**
     * Test of endParameterLoading method, of class ParameterListLockingPluginParameters.
     */
    @Test
    public void testEndParameterLoading() {
        System.out.println("endParameterLoading");
        
        final ParameterListLockingPluginParameters params = new ParameterListLockingPluginParameters();
        final PluginParameter<ParameterListParameterValue> parameterListParam = ParameterListParameterType.build("test1");
        ParameterListParameterType.setPrototypeParameters(parameterListParam, new PluginParameters());
        params.addParameter(parameterListParam);
        params.addParameter(StringParameterType.build("test2"));
        params.startParameterLoading();
        
        assertTrue(parameterListParam.getParameterListValue().isLocked());
        assertEquals(parameterListParam.getParameterListValue().getCachedValue(), "");
        params.endParameterLoading();
        assertFalse(parameterListParam.getParameterListValue().isLocked());
        assertNull(parameterListParam.getParameterListValue().getCachedValue());
    }
    
    /**
     * Test of setObjectValue method, of class ParameterListParameterValue.
     */
    @Test(expectedExceptions = UnsupportedOperationException.class, 
            expectedExceptionsMessageRegExp = "Not supported.")
    public void testSetObjectValue() {
        System.out.println("setObjectValue");
        
        final ParameterListParameterValue instance = new ParameterListParameterValue();
        instance.setObjectValue("");
    }
    
    /**
     * Test of set method, of class ParameterListParameterValue.
     */
    @Test
    public void testSet() {
        System.out.println("set");
        
        final ParameterListParameterValue parameterListValue = new ParameterListParameterValue();
        assertNull(parameterListValue.getObjectValue());
        // new and old are both null so will fail
        assertFalse(parameterListValue.set(null));
        assertNull(parameterListValue.getObjectValue());
        
        final ParameterList paramList = mock(ParameterList.class);
        assertTrue(parameterListValue.set(paramList));
        assertEquals(parameterListValue.getObjectValue(), paramList);
        // new and old are both the same value so will fail
        assertFalse(parameterListValue.set(paramList));
        assertEquals(parameterListValue.getObjectValue(), paramList);
    }
    
    /**
     * Test of equals method, of class ParameterListParameterValue.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final ParameterListParameterValue parameterListValue = new ParameterListParameterValue();
        final PluginPaneFactory factory = mock(PluginPaneFactory.class);
        final Pane pane = mock(Pane.class);
        final PluginParameter<ParameterListParameterValue> paramListParam = mock(PluginParameter.class);
        
        when(factory.getNewPane(any(PluginParameters.class))).thenReturn(pane);
        doNothing().when(paramListParam).fireChangeEvent(ParameterChange.VALUE);
        
        parameterListValue.set(new ParameterList(factory, null, paramListParam));
        
        final ParameterList paramList1 = new ParameterList(null, null, paramListParam);
        final ParameterList paramList2 = new ParameterList(factory, new PluginParameters(), paramListParam);
        final ParameterList paramList3 = new ParameterList(factory, null, null);
        final ParameterList paramList4 = new ParameterList(factory, null, paramListParam);
        paramList4.append(new PluginParameters());
        
        final ParameterListParameterValue comp1 = new ParameterListParameterValue();
        comp1.set(paramList1);
        final ParameterListParameterValue comp2 = new ParameterListParameterValue();
        comp2.set(paramList2);
        final ParameterListParameterValue comp3 = new ParameterListParameterValue();
        comp3.set(paramList3);
        final ParameterListParameterValue comp4 = new ParameterListParameterValue();
        comp4.set(paramList4);
        final ParameterListParameterValue comp5 = new ParameterListParameterValue();
        comp5.set(new ParameterList(factory, null, paramListParam));
        
        assertFalse(parameterListValue.equals(null));
        assertFalse(parameterListValue.equals(true));
        assertFalse(parameterListValue.equals(comp1));
        assertFalse(parameterListValue.equals(comp2));
        assertFalse(parameterListValue.equals(comp3));
        assertFalse(parameterListValue.equals(comp4));
        assertTrue(parameterListValue.equals(comp5));
    }
    
    /**
     * Test of createCopy method, of class ParameterListParameterValue.
     */
    @Test
    public void testCreateCopy() {
        System.out.println("createCopy");
        
        final ParameterListParameterValue parameterListValue = new ParameterListParameterValue();
        parameterListValue.set(new ParameterList(null, null, null));
        final ParameterValue parameterListCopy = parameterListValue.createCopy();
        assertTrue(parameterListValue.equals(parameterListCopy));
        
        parameterListValue.set(new ParameterList(null, new PluginParameters(), null));
        assertFalse(parameterListValue.equals(parameterListCopy));
    }
    
    /**
     * Test of toString method, of class ParameterListParameterValue.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final PluginParameter<ParameterListParameterValue> parameterListParam = ParameterListParameterType.build("My Parameter List");
        ParameterListParameterType.setPrototypeParameters(parameterListParam, new PluginParameters());
        assertEquals(parameterListParam.getParameterValue().toString(), "");
        
        final PluginParameters params = new PluginParameters();
        params.addParameter(StringParameterType.build("test"));
        params.addParameter(BooleanParameterType.build("test2"));
        ParameterListParameterType.setPrototypeParameters(parameterListParam, params);
        ParameterListParameterType.addToList(parameterListParam);
        assertEquals(parameterListParam.getParameterValue().toString(), "1::test;;;;test2;;false;;");
        ParameterListParameterType.addToList(parameterListParam);
        assertEquals(parameterListParam.getParameterValue().toString(), "2::test;;;;test2;;false;;::test;;;;test2;;false;;");
    }
    
    /**
     * Test of setStringValue method, of class ParameterListParameterValue.
     */
    @Test
    public void testSetStringValue() {
        System.out.println("setStringValue");
        
        final PluginParameter<ParameterListParameterValue> parameterListParam = ParameterListParameterType.build("My Parameter List");
        final PluginParameters params = new PluginParameters();
        params.addParameter(StringParameterType.build("test"));
        params.addParameter(BooleanParameterType.build("test2"));
        ParameterListParameterType.setPrototypeParameters(parameterListParam, params);
        assertTrue(parameterListParam.getParameterValue().getListOfPluginParameters().isEmpty());
        assertTrue(parameterListParam.getParameterValue().getPanes().isEmpty());
        
        assertTrue(parameterListParam.getParameterValue().setStringValue("1::test;;a string;;test2;;true;;"));
        assertEquals(parameterListParam.getParameterValue().getListOfPluginParameters().size(), 1);
        assertEquals(parameterListParam.getParameterValue().getPanes().size(), 1);
        // values attempting to add match what is already there
        assertFalse(parameterListParam.getParameterValue().setStringValue("1::test;;a string;;test2;;true;;"));
        assertEquals(parameterListParam.getParameterValue().getListOfPluginParameters().size(), 1);
        assertEquals(parameterListParam.getParameterValue().getPanes().size(), 1);
        
        // lock the value so subsequent changes only applied to the cached value
        parameterListParam.getParameterValue().lockValue();
        assertTrue(parameterListParam.getParameterValue().setStringValue("2::test;;a string;;test2;;true;;::test;;b string;;test2;;false;;"));
        // cached value has changed but since we are locked, nothing else is altered
        assertEquals(parameterListParam.getParameterValue().getListOfPluginParameters().size(), 1);
        assertEquals(parameterListParam.getParameterValue().getPanes().size(), 1);
        // fails as it matches the cached value (even though it is different to actual values) 
        assertFalse(parameterListParam.getParameterValue().setStringValue("2::test;;a string;;test2;;true;;::test;;b string;;test2;;false;;"));
        assertEquals(parameterListParam.getParameterValue().getListOfPluginParameters().size(), 1);
        assertEquals(parameterListParam.getParameterValue().getPanes().size(), 1);       
    }
}
