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
package au.gov.asd.tac.constellation.views.tableview.state;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.ArrayList;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class TableViewStateNGTest {

    @Test
    public void comlumnAttributesTransactionElements() {
        final TableViewState state = new TableViewState();
        state.setElementType(GraphElementType.TRANSACTION);

        state.setColumnAttributes(new ArrayList<>());

        assertEquals(state.getTransactionColumnAttributes(), new ArrayList<>());
        assertEquals(state.getVertexColumnAttributes(), null);
    }

    @Test
    public void columnAttributesVertexElements() {
        final TableViewState state = new TableViewState();
        state.setElementType(GraphElementType.VERTEX);

        state.setColumnAttributes(new ArrayList<>());

        assertEquals(state.getTransactionColumnAttributes(), null);
        assertEquals(state.getVertexColumnAttributes(), new ArrayList<>());
    }

    @Test
    public void constructorNullShallowCopy() {
        final TableViewState state = new TableViewState(null);

        assertEquals(state.isSelectedOnly(), false);
        assertEquals(state.getElementType(), GraphElementType.TRANSACTION);
        assertEquals(state.getTransactionColumnAttributes(), null);
        assertEquals(state.getVertexColumnAttributes(), null);
    }

    @Test
    public void constructorNonNullShallowCopy() {
        final TableViewState state = new TableViewState();
        state.setSelectedOnly(true);
        state.setElementType(GraphElementType.VERTEX);
        state.setTransactionColumnAttributes(new ArrayList<>());
        state.setVertexColumnAttributes(new ArrayList<>());

        final TableViewState copy = new TableViewState(state);

        assertEquals(copy.isSelectedOnly(), true);
        assertEquals(copy.getElementType(), GraphElementType.VERTEX);
        assertEquals(copy.getTransactionColumnAttributes(), new ArrayList<>());
        assertEquals(copy.getVertexColumnAttributes(), new ArrayList<>());
    }

    @Test
    public void equality() {
        EqualsVerifier
                .forClass(TableViewState.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
}
