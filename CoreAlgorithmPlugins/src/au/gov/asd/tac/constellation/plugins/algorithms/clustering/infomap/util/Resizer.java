/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util;

import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.NodeFactoryBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.traits.FlowBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author algol
 */
public class Resizer {

    public static <T> void resize(final List<T> al, final int size, final T value) {
        if (al instanceof ArrayList) {
            while (al.size() > size) {
                al.remove(size);
            }

            ((ArrayList)al).ensureCapacity(size);
            while (al.size() < size) {
                al.add(value);
            }
        }
    }

    public static FlowBase[] resizeFlowBase(final FlowBase[] a, final int size, final NodeFactoryBase factory) {
        if (size == a.length) {
            return a;
        }

        final FlowBase[] a2 = Arrays.copyOf(a, size);

        for (int i = a.length; i < size; i++) {
            a2[i] = factory.createFlow();
        }

        return a2;
    }
}
