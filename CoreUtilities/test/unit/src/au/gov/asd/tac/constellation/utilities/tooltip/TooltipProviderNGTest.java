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
package au.gov.asd.tac.constellation.utilities.tooltip;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.Pane;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.stubbing.Answer;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class TooltipProviderNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
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
     * Test of getTooltips method, of class TooltipProvider.
     */
    @Test
    public void testGetTooltips() {
        System.out.println("getTooltips");
        
        String content = "";
        int activePosition = 0;
        
        try(final MockedStatic<TooltipProvider> ttpStatic = mockStatic(TooltipProvider.class, CALLS_REAL_METHODS)){
            ttpStatic.when(() -> TooltipProvider.init()).thenAnswer((Answer<Void>) invocation -> null);
            
            // Mock list of tooltip providers
            final Pane pane1 = mock(Pane.class);
            final Pane pane2 = mock(Pane.class);
            final Pane pane3 = mock(Pane.class);
            final Pane pane4 = mock(Pane.class);
            
            final List<Pane> panesList = new ArrayList<>();
            panesList.add(pane1);
            panesList.add(pane2);
            panesList.add(pane3);
            panesList.add(pane4);
            
            TooltipProviderImpl ttp1 = spy(new TooltipProviderImpl());
            ttp1.setPane(pane1);
            TooltipProviderImpl ttp2 = spy(new TooltipProviderImpl());
            ttp2.setPane(pane2);
            TooltipProviderImpl ttp3 = spy(new TooltipProviderImpl());
            ttp3.setPane(pane3);
            TooltipProviderImpl ttp4 = spy(new TooltipProviderImpl());
            ttp4.setPane(pane4);
            
            final List<TooltipProvider> ttpList = new ArrayList<>();
            ttpList.add(ttp1);
            ttpList.add(ttp2);
            ttpList.add(ttp3);
            ttpList.add(ttp4);
            
            ttpStatic.when(() -> TooltipProvider.getTooltipProviders()).thenReturn(ttpList);
            List<TooltipProvider.TooltipDefinition> result = TooltipProvider.getTooltips(content, activePosition);
            
            assertEquals(result.size(), 4);
            
            // assert that all definitions are present
            for(final TooltipProvider.TooltipDefinition ttd : result){
                assertTrue(panesList.contains(ttd.getNode()));
            }
            
            verify(ttp1, times(1)).createTooltip(Mockito.eq(content), Mockito.eq(activePosition));
            verify(ttp2, times(1)).createTooltip(Mockito.eq(content), Mockito.eq(activePosition));
            verify(ttp3, times(1)).createTooltip(Mockito.eq(content), Mockito.eq(activePosition));
            verify(ttp4, times(1)).createTooltip(Mockito.eq(content), Mockito.eq(activePosition));
        }
    }

    /**
     * Test of getAllTooltips method, of class TooltipProvider.
     */
    @Test
    public void testGetAllTooltips() {
        System.out.println("getAllTooltips");
                
        String content = "";
        
        try(final MockedStatic<TooltipProvider> ttpStatic = mockStatic(TooltipProvider.class, CALLS_REAL_METHODS)){
            ttpStatic.when(() -> TooltipProvider.init()).thenAnswer((Answer<Void>) invocation -> null);
            
            // Mock list of tooltip providers
            final Pane pane1 = mock(Pane.class);
            final Pane pane2 = mock(Pane.class);
            final Pane pane3 = mock(Pane.class);
            final Pane pane4 = mock(Pane.class);
            
            final List<Pane> panesList = new ArrayList<>();
            panesList.add(pane1);
            panesList.add(pane2);
            panesList.add(pane3);
            panesList.add(pane4);
            
            TooltipProviderImpl ttp1 = spy(new TooltipProviderImpl());
            ttp1.setPane(pane1);
            TooltipProviderImpl ttp2 = spy(new TooltipProviderImpl());
            ttp2.setPane(pane2);
            TooltipProviderImpl ttp3 = spy(new TooltipProviderImpl());
            ttp3.setPane(pane3);
            TooltipProviderImpl ttp4 = spy(new TooltipProviderImpl());
            ttp4.setPane(pane4);
            
            final List<TooltipProvider> ttpList = new ArrayList<>();
            ttpList.add(ttp1);
            ttpList.add(ttp2);
            ttpList.add(ttp3);
            ttpList.add(ttp4);
            
            ttpStatic.when(() -> TooltipProvider.getTooltipProviders()).thenReturn(ttpList);
            List<TooltipProvider.TooltipDefinition> result = TooltipProvider.getAllTooltips(content);
            
            assertEquals(result.size(), 4);
            
            // assert that all definitions are present
            for(final TooltipProvider.TooltipDefinition ttd : result){
                assertTrue(panesList.contains(ttd.getNode()));
            }
            
            verify(ttp1, times(1)).createTooltips(Mockito.eq(content));
            verify(ttp2, times(1)).createTooltips(Mockito.eq(content));
            verify(ttp3, times(1)).createTooltips(Mockito.eq(content));
            verify(ttp4, times(1)).createTooltips(Mockito.eq(content));
        }
    }

    /**
     * Testing implementation of the TooltipProvider.
     */
    private class TooltipProviderImpl extends TooltipProvider {
        
        private Pane pane = null;
        
        protected void setPane(final Pane pane){
            this.pane = pane;
        }
        
        @Override
        public TooltipDefinition createTooltip(final String content, final int activePosition) {
            return new TooltipDefinition(pane);
        }
        
        @Override
        public TooltipDefinition createTooltips(final String content) {
            return new TooltipDefinition(pane);
        }
    }    
}
