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
package au.gov.asd.tac.constellation.views.scripting.graph.iterators;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.views.scripting.graph.STransaction;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator for accessing transactions from the context of links via
 * scripting.
 *
 * @author cygnus_x-1
 */
public class SLinkTransactionIterator implements Iterator<STransaction> {

    private final GraphReadMethods readableGraph;
    private final int linkId;
    private final int transactionCount;
    private int currentPosition;

    public SLinkTransactionIterator(final GraphReadMethods readableGraph, final int linkId) {
        this.readableGraph = readableGraph;
        this.linkId = linkId;
        this.transactionCount = readableGraph.getLinkTransactionCount(linkId);
        this.currentPosition = 0;
    }

    @Override
    public boolean hasNext() {
        return currentPosition < transactionCount;
    }

    @Override
    public STransaction next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        final int transactionId = readableGraph.getLinkTransaction(linkId, currentPosition++);
        return new STransaction(readableGraph, transactionId);
    }
}
