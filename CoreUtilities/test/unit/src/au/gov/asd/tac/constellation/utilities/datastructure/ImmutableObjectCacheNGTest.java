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
package au.gov.asd.tac.constellation.utilities.datastructure;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

/**
 * @author groombridge34a
 */
public class ImmutableObjectCacheNGTest {
    
    private final class ClassX {
        private int i = 0;
        public ClassX(int i) {
            this.i = i;
        }
        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof ClassX))
                return false;
            ClassX other = (ClassX) o;
            return this.i == other.i;
        }
        @Override
        public int hashCode() {
            return (int) (i ^ (i >>> 32));
        }
    };
    
    private final class ClassY {};
    
    private static final String TO_STRING = "ImmutableObjectCache[entries = %d]";
    
    /**
     * toString() and deduplicating null on an empty cache.
     */
    @Test
    public void testEmptyAndNull() {
        ImmutableObjectCache cache = new ImmutableObjectCache();
        
        // empty cache
        assertEquals(cache.toString(), String.format(TO_STRING, 0));
        
        // adding null simply returns null, no other actions are performed
        assertNull(cache.deduplicate(null));
        assertEquals(cache.toString(), String.format(TO_STRING, 0));
    }
    
    /**
     * Adding the same, equivalent and different objects to the cache.
     */
    @Test
    public void testDeduplicateAndToString() {
        ImmutableObjectCache cache = new ImmutableObjectCache();
        
        // add the same object twice
        ClassX cx1 = new ClassX(0);
        assertSame(cache.deduplicate(cx1), cx1);
        assertSame(cache.deduplicate(cx1), cx1);
        assertEquals(cache.toString(), String.format(TO_STRING, 1));

        // add an equivalent object to an object already present
        ClassX cx2 = new ClassX(0);
        ClassX cx2ret = cache.deduplicate(cx2);
        assertSame(cx2ret, cx1);
        assertEquals(cx2ret, cx2);
        assertEquals(cache.toString(), String.format(TO_STRING, 1));
        
        // add an object of a different class
        ClassY cy = new ClassY();
        assertSame(cache.deduplicate(cy), cy);
        assertEquals(cache.toString(), String.format(TO_STRING, 2));
    }

}
