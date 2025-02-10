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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.functionality.dialog.ItemsRow;
import java.util.Collections;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class LeadNodeSelectionDialogNGTest {

    @Test
    public void selectedRows() throws InterruptedException {
        final LeadNodeSelectionDialog leadNodeSelectionDialog = mock(LeadNodeSelectionDialog.class);

        when(leadNodeSelectionDialog.getLeadVertexId()).thenCallRealMethod();
        doCallRealMethod().when(leadNodeSelectionDialog).selectRows(any(List.class));

        // To avoid the constructor and all the JavaFX we mock the class and then
        // tell it to use the real methods. This however means that we do not verify
        // that the constructor sets leadVertexId to -1. At this point in the code
        // leadVertexId is actually 0.
        final List<ItemsRow<Integer>> selectedRows = List.of(
                new ItemsRow(5, "label1", "description1"),
                new ItemsRow(15, "label2", "description2")
        );

        leadNodeSelectionDialog.selectRows(selectedRows);

        assertEquals(leadNodeSelectionDialog.getLeadVertexId(), 5);
    }

    @Test
    public void selectedRowsNoRowsSelected() throws InterruptedException {
        final LeadNodeSelectionDialog leadNodeSelectionDialog = mock(LeadNodeSelectionDialog.class);

        when(leadNodeSelectionDialog.getLeadVertexId()).thenCallRealMethod();
        doCallRealMethod().when(leadNodeSelectionDialog).selectRows(any(List.class));

        // To avoid the constructor and all the JavaFX we mock the class and then
        // tell it to use the real methods. This however means that we do not verify
        // that the constructor sets leadVertexId to -1. At this point in the code
        // leadVertexId is actually 0.
        final List<ItemsRow<Integer>> selectedRows = Collections.emptyList();

        leadNodeSelectionDialog.selectRows(selectedRows);

        assertEquals(leadNodeSelectionDialog.getLeadVertexId(), 0);
    }
}
