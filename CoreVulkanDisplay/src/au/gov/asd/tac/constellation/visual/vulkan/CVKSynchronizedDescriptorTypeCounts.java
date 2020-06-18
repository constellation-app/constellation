/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.visual.vulkan;

public class CVKSynchronizedDescriptorTypeCounts {
    protected int poolDescriptorTypeCounts[] = new int[11];
    protected boolean isDirty = false;

    public synchronized void Set(int newCounts[]) {
        assert(newCounts.length == 11);
        for (int i = 0; i < 11; ++i) {
            poolDescriptorTypeCounts[i] = newCounts[i];
        }
        isDirty = true;
    }
    public synchronized void Set(int type, int value) {
        assert(type >= 0 && type < 11);
        poolDescriptorTypeCounts[type] = value;
        isDirty = true;
    }
    public synchronized int Get(int type) {
        assert(type >= 0 && type < 11);
        return poolDescriptorTypeCounts[type];
    }
    public synchronized void Increment(int type) {
        assert(type >= 0 && type < 11);
        poolDescriptorTypeCounts[type] += 1;
        isDirty = true;
    }
    public synchronized boolean IsDirty() {
        return isDirty;
    }
    public synchronized void ResetDirty() {
//        for (int i = 0; i < 11; ++i) {
//            poolDescriptorTypeCounts[i] = 0;
//        }            
        isDirty = false;
    }

    protected synchronized int NumberOfDescriptorTypes() {
        int allTypesCount = 0;
        for (int i = 0; i < 11; ++i) {
            if (poolDescriptorTypeCounts[i] > 0) {
                ++allTypesCount;
            }
        }
        return allTypesCount;
    }   
}
