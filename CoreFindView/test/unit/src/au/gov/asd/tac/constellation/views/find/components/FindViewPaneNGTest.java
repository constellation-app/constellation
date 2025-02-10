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

import au.gov.asd.tac.constellation.views.find.FindViewTopComponent;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
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
public class FindViewPaneNGTest {

    private FindViewTopComponent findViewTopComponent;
    private FindViewTopComponent spyTopComponent;

    private BasicFindTab basicFindTab;
    private BasicFindTab spyBasicFindTab;
    private ReplaceTab replaceTab;
    private ReplaceTab spyReplaceTab;
    private FindViewPane findViewPane;
    private FindViewPane spyFindViewPane;
    private FindViewTabs findViewTabs;
    private FindViewTabs spyFindViewTabs;
    
    private static final Logger LOGGER = Logger.getLogger(FindViewPaneNGTest.class.getName());
    
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

        findViewPane = new FindViewPane(spyTopComponent);
        spyFindViewPane = spy(findViewPane);

        findViewTabs = mock(FindViewTabs.class);
        spyFindViewTabs = spy(findViewTabs);

        basicFindTab = mock(BasicFindTab.class);
        spyBasicFindTab = spy(basicFindTab);

        replaceTab = mock(ReplaceTab.class);
        spyReplaceTab = spy(replaceTab);

        when(spyFindViewTabs.getParentComponent()).thenReturn(spyFindViewPane);
        when(spyFindViewPane.getTabs()).thenReturn(spyFindViewTabs);
        when(spyFindViewPane.getParentComponent()).thenReturn(spyTopComponent);
        when(spyFindViewTabs.getBasicFindTab()).thenReturn(spyBasicFindTab);
        when(spyFindViewTabs.getReplaceTab()).thenReturn(spyReplaceTab);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getParentComponent method, of class FindViewPane.
     */
    @Test
    public void testGetParentComponent() {
        System.out.println("getParentComponent");
        
        assertEquals(spyFindViewPane.getParentComponent(), spyTopComponent);
    }

    /**
     * Test of getTabs method, of class FindViewPane.
     */
    @Test
    public void testGetTabs() {
        System.out.println("getTabs");
        
        assertEquals(spyFindViewPane.getTabs(), spyFindViewTabs);
    }
}
