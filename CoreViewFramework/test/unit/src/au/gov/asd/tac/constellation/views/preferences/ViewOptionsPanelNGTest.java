/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.preferences;

import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.mockito.MockedConstruction;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Test of class ViewOptionsPanel.
 *
 * @author sol695510
 */
public class ViewOptionsPanelNGTest {

    /**
     * Test of createTableModel method, of class ViewOptionsPanel.
     */
    @Test
    public void testCreateTableModel() {
        System.out.println("createTableModel");

        try (MockedConstruction<DefaultTableModel> mockDTM = mockConstruction(DefaultTableModel.class)) {

            final ViewOptionsPanel instance = new ViewOptionsPanel();
            instance.createTableModel();

            // Assert that a mock of the DefaultTableModel was constructed.
            final List<DefaultTableModel> constructedDTM = mockDTM.constructed();
            assertEquals(constructedDTM.size(), 3); // Why 3 and not 1?

            // Verify that the DefaultTableModel was correctly constructed.
            assertEquals(constructedDTM.get(2).getColumnClass(0), String.class);
            assertEquals(constructedDTM.get(2).getColumnClass(1), Boolean.class);

            for (int i = 1; i < 28; i++) {
                assertFalse(constructedDTM.get(2).isCellEditable(i, 0));
                assertTrue(constructedDTM.get(2).isCellEditable(i, 1));
            }
        }
    }

    /**
     * Test of fireTableDataChanged method, of class ViewOptionsPanel.
     */
    @Test
    public void testFireTableDataChanged() {
        System.out.println("fireTableDataChanged");

        try (MockedConstruction<DefaultTableModel> mockDTM = mockConstruction(DefaultTableModel.class)) {

            final ViewOptionsPanel instance = new ViewOptionsPanel();
            instance.fireTableDataChanged();

            // Assert that a mock of the DefaultTableModel was constructed.
            final List<DefaultTableModel> constructedDTM = mockDTM.constructed();
            assertEquals(constructedDTM.size(), 2); // Why 2 and not 1?

            // Verify that this method was run on the constructed mock.
            verify(constructedDTM.get(1), times(1)).fireTableDataChanged();
        }
    }
}
