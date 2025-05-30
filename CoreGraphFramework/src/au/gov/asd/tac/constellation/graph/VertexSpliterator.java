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
package au.gov.asd.tac.constellation.graph;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 *
 * @author algol
 */
public class VertexSpliterator implements Spliterator.OfInt {

    private final GraphReadMethods rg;
    private int position;
    private int limit;

    public VertexSpliterator(final GraphReadMethods rg) {
        this.rg = rg;
        position = 0;
        limit = rg.getVertexCount();
    }

    private VertexSpliterator(final GraphReadMethods rg, final int position, final int limit) {
        this.rg = rg;
        this.position = position;
        this.limit = limit;
    }

    @Override
    public OfInt trySplit() {
        final int remaining = limit - position;
        final int half = remaining / 2;
        if (half < 2) {
            return null;
        }

        final int oldLimit = limit;
        limit -= half;

        return new VertexSpliterator(rg, limit, oldLimit);
    }

    @Override
    public boolean tryAdvance(final IntConsumer action) {
        if (position < limit) {
            final int vxId = rg.getVertex(position++);
            action.accept(vxId);
            return true;
        }

        return false;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super Integer> action) {
        if (position < limit) {
            final int vxId = rg.getVertex(position++);
            action.accept(vxId);
            return true;
        }

        return false;
    }

    @Override
    public long estimateSize() {
        return (long) limit - position;
    }

    @Override
    public int characteristics() {
        return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.SIZED;
    }
}
