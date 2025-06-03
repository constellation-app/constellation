/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.preferences;

import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class GraphOptionsPanelNGTest {

    private GraphOptionsPanel graphOptionsPanel;
    
    public GraphOptionsPanelNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        graphOptionsPanel = spy(GraphOptionsPanel.class);        
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    @Test
    public void graphOptionsPanel_animationEnabled() {
        graphOptionsPanel.setAnimationsEnabled(true);
        assertTrue(graphOptionsPanel.getAnimationsEnabled());
        graphOptionsPanel.setAnimationsEnabled(false);
        assertFalse(graphOptionsPanel.getAnimationsEnabled());
    }
    
    @Test
    public void graphOptionsPanel_setColors() {
        graphOptionsPanel.setLeftColor("Blue");        
        assertTrue(graphOptionsPanel.getLeftColor().equals("Blue"));
       
        graphOptionsPanel.setRightColor("Magenta");        
        assertTrue(graphOptionsPanel.getRightColor().equals("Magenta"));        
    }
    
        public void graphOptionsPanel_setBlaze() {
        graphOptionsPanel.setBlazeSize(100);        
        assertTrue(graphOptionsPanel.getBlazeSize() == 100);
       
        graphOptionsPanel.setBlazeSize(150);        
        assertTrue(graphOptionsPanel.getBlazeSize() == 150);
        
        graphOptionsPanel.setBlazeOpacity(11);        
        assertTrue(graphOptionsPanel.getBlazeOpacity() == 111);        
    }            
}
