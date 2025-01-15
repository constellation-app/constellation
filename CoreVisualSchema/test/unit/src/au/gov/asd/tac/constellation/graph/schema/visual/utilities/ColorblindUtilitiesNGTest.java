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
package au.gov.asd.tac.constellation.graph.schema.visual.utilities;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mockStatic;
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
public class ColorblindUtilitiesNGTest {

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
     * Test of calculateColorBrightness method, of class ColorblindUtilities.
     * @throws java.util.prefs.BackingStoreException
     */
    @Test
    public void testCalculateColorBrightness() throws BackingStoreException {
        System.out.println("calculateColorBrightness");
        
        final Preferences p = Preferences.userNodeForPackage(ColorblindUtilitiesNGTest.class);
        
        try (final MockedStatic<ColorblindUtilities> colorblindUtilitiesMockedStatic = mockStatic(ColorblindUtilities.class, Mockito.CALLS_REAL_METHODS)) {
            colorblindUtilitiesMockedStatic.when(() -> ColorblindUtilities.getApplicationPreferences()).thenReturn(p);
            
            p.put(ApplicationPreferenceKeys.COLORBLIND_MODE, ApplicationPreferenceKeys.COLORBLIND_MODE_DEFAULT);
            final String colorblindModeBefore = p.get(ApplicationPreferenceKeys.COLORBLIND_MODE, ApplicationPreferenceKeys.COLORBLIND_MODE_DEFAULT);
            assertEquals(colorblindModeBefore, "None");
            
            final ConstellationColor color1 = ConstellationColor.getColorValue(0.6F, 0.6F, 0.6F, 0.6F);
            final ConstellationColor color2 = ConstellationColor.getColorValue(0.3F, 0.2F, 0.6F, 0.6F);
            final ConstellationColor color3 = ConstellationColor.getColorValue(0.6F, 0.3F, 0.2F, 0.6F);
            final ConstellationColor color4 = ConstellationColor.getColorValue(0.2F, 0.6F, 0.3F, 0.6F);
            
            // no change should happen with default None
            assertEquals(ColorblindUtilities.calculateColorBrightness(color1), ConstellationColor.getColorValue(0.6F, 0.6F, 0.6F, 0.6F));
            assertEquals(ColorblindUtilities.calculateColorBrightness(color2), ConstellationColor.getColorValue(0.3F, 0.2F, 0.6F, 0.6F));
            assertEquals(ColorblindUtilities.calculateColorBrightness(color3), ConstellationColor.getColorValue(0.6F, 0.3F, 0.2F, 0.6F));
            assertEquals(ColorblindUtilities.calculateColorBrightness(color4), ConstellationColor.getColorValue(0.2F, 0.6F, 0.3F, 0.6F));
            
            p.put(ApplicationPreferenceKeys.COLORBLIND_MODE, "Deuteranopia");
            // red and blue adjusted, green no change
            assertEquals(ColorblindUtilities.calculateColorBrightness(color1), ConstellationColor.getColorValue(0.36F, 0.6F, 0.5F, 0.6F));
            assertEquals(ColorblindUtilities.calculateColorBrightness(color2), ConstellationColor.getColorValue(0.16F, 0.2F, 0.5F, 0.6F));
            assertEquals(ColorblindUtilities.calculateColorBrightness(color3), ConstellationColor.getColorValue(0.28F, 0.3F, 0.2F/1.2F, 0.6F));
            assertEquals(ColorblindUtilities.calculateColorBrightness(color4), ConstellationColor.getColorValue(0.2F, 0.6F, 0.3F, 0.6F));
            
            p.put(ApplicationPreferenceKeys.COLORBLIND_MODE, "Protanopia");
            // green and red adjusted, blue no change
            assertEquals(ColorblindUtilities.calculateColorBrightness(color1), ConstellationColor.getColorValue(1F/3, 0.36F, 0.6F, 0.6F));
            assertEquals(ColorblindUtilities.calculateColorBrightness(color2), ConstellationColor.getColorValue(0.3F, 0.2F, 0.6F, 0.6F));
            assertEquals(ColorblindUtilities.calculateColorBrightness(color3), ConstellationColor.getColorValue(0.6F, 0.3F, 0.2F, 0.6F));
            assertEquals(ColorblindUtilities.calculateColorBrightness(color4), ConstellationColor.getColorValue(0.2F/1.8F, 0.22F, 0.3F, 0.6F));
            
            p.put(ApplicationPreferenceKeys.COLORBLIND_MODE, "Tritanopia");
            // blue and green adjusted, red no change
            assertEquals(ColorblindUtilities.calculateColorBrightness(color1), ConstellationColor.getColorValue(0.6F, 4F/7, 0.36F, 0.6F));
            assertEquals(ColorblindUtilities.calculateColorBrightness(color2), ConstellationColor.getColorValue(0.3F, 0.2F/1.05F, 0.28F, 0.6F));
            assertEquals(ColorblindUtilities.calculateColorBrightness(color3), ConstellationColor.getColorValue(0.6F, 0.3F, 0.2F, 0.6F));
            assertEquals(ColorblindUtilities.calculateColorBrightness(color4), ConstellationColor.getColorValue(0.2F, 0.6F, 0.3F, 0.6F));
        } finally {
            // clean up, first remove Preferences nodes this test plays with
            p.removeNode();
            // Assert that default mode has not changed
            assertEquals(ApplicationPreferenceKeys.COLORBLIND_MODE_DEFAULT, "None");
        }
    }   
}
