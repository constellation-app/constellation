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
package au.gov.asd.tac.constellation.graph.utilities.wrapper;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Graph Wrapper Helper
 *
 * @author capella
 */
public class GraphWrapperHelper {

    public static interface NextMethod<T> {

        public T getAtPosition(int position);
    }

    public static interface CountMethod {

        public int getCount();
    }

    public static <T> Iterable<T> createIterator(NextMethod<T> nextMethod, CountMethod countMethod) {
        return () -> new Iterator<T>() {
            private final int count = countMethod.getCount();
            private int position = 0;

            @Override
            public boolean hasNext() {
                return position < count;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                return nextMethod.getAtPosition(position++);
            }
        };
    }
}
