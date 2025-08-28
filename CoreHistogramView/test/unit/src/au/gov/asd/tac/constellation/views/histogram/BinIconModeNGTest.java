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
package au.gov.asd.tac.constellation.views.histogram;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.javafx.JavaFxUtilities;
import au.gov.asd.tac.constellation.views.histogram.bins.ObjectBin;
import java.awt.Color;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class BinIconModeNGTest {

    /**
     * Test of getWidth method, of class BinIconMode.
     */
    @Test
    public void testGetWidthNone() {
        System.out.println("getWidthColorNone");
        final BinIconMode instance = BinIconMode.NONE;
        final float expResult = 0.0F;
        final float result = instance.getWidth();
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of getWidth method, of class BinIconMode.
     */
    @Test
    public void testGetWidthIcon() {
        System.out.println("getWidthIcon");
        final BinIconMode instance = BinIconMode.ICON;
        final float expResult = 1.5F;
        final float result = instance.getWidth();
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of getWidth method, of class BinIconMode.
     */
    @Test
    public void testGetWidthColor() {
        System.out.println("getWidthColor");
        final BinIconMode instance = BinIconMode.COLOR;
        final float expResult = 1.5F;
        final float result = instance.getWidth();
        assertEquals(result, expResult, 0.0);
    }

    /**
     * Test of createFXIcon method, of class BinIconMode.
     */
    @Test
    public void testCreateFXIconNone() {
        System.out.println("createFXIconNone");

        final int height = 0;
        final Node expResult = null;

        final BinIconMode instance = BinIconMode.NONE;
        final Node result = instance.createFXIcon(null, height);

        assertEquals(result, expResult);
    }

    /**
     * Test of createFXIcon method, of class BinIconMode.
     */
    @Test
    public void testCreateFXIconIcon() throws Exception {
        System.out.println("createFXIconIcon");

        // Set up mocks
        final ObjectBin mockBin = mock(ObjectBin.class);
        final ConstellationIcon mockKey = mock(ConstellationIcon.class);
        final String keyName = "name";
        when(mockBin.getKeyAsObject()).thenReturn(mockKey);
        when(mockKey.getName()).thenReturn(keyName);

        final int height = 0;

        final BinIconMode instance = BinIconMode.ICON;
        final ImageView result = (ImageView) instance.createFXIcon(mockBin, height);

        assertNotNull(result);
    }

    /**
     * Test of createFXIcon method, of class BinIconMode.
     */
    @Test
    public void testCreateFXIconColor() {
        System.out.println("createFXIconColor");

        // Set up mocks
        final ObjectBin mockBin = mock(ObjectBin.class);
        final ConstellationColor mockKey = mock(ConstellationColor.class);
        when(mockBin.getKeyAsObject()).thenReturn(mockKey);
        when(mockKey.getJavaColor()).thenReturn(Color.RED);

        final int height = 0;
        final int arc = height / 3;
        final Rectangle expResult = new Rectangle(Double.valueOf(height), Double.valueOf(height), JavaFxUtilities.awtColorToFXColor(mockKey.getJavaColor()));
        expResult.setArcHeight(arc);
        expResult.setArcWidth(arc);

        final BinIconMode instance = BinIconMode.COLOR;
        final Rectangle result = (Rectangle) instance.createFXIcon(mockBin, height);

        assertEquals(result.getArcHeight(), expResult.getArcHeight());
        assertEquals(result.getArcWidth(), expResult.getArcWidth());

        assertEquals(result.getHeight(), expResult.getHeight());
        assertEquals(result.getWidth(), expResult.getWidth());
        // Check color matches
        assertEquals(result.getFill().toString(), expResult.getFill().toString());
    }
}
