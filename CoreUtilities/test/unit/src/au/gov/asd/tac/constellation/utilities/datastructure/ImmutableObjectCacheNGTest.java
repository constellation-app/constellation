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
package au.gov.asd.tac.constellation.utilities.datastructure;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class ImmutableObjectCacheNGTest {
    
    private static final String TO_STRING_PREFIX = "ImmutableObjectCache[entries = ";
    
    /**
     * Cannot add null to the cache.
     */
    @Test
    public void testNull() {
        final ImmutableObjectCache c = new ImmutableObjectCache();
        assertNull(c.deduplicate(null));
        assertEquals(c.toString(), TO_STRING_PREFIX + "0]");
    }
    
    /**
     * Can switch between verbose and not verbose modes.
     */
    @Test
    public void testVerboseSwitch() {
        final ImmutableObjectCache c = new ImmutableObjectCache();
        c.deduplicate("dummy");
        c.deduplicate("another dummy");
        assertEquals(c.toString(), TO_STRING_PREFIX + "2]");
        c.setVerbose(true);
        c.deduplicate("dummy");
        c.deduplicate("another dummy");
        assertEquals(c.toString(), 
                TO_STRING_PREFIX + "2]\n"
                        + "    java.lang.String: new = 0, old = 2, dedupe = 0\n"
                        + "    saved String bytes = 0\n");
        c.setVerbose(false);
        c.deduplicate("dummy");
        c.deduplicate("another dummy");
        assertEquals(c.toString(), TO_STRING_PREFIX + "2]");
    }
    
    /**
     * Can add new objects to the cache and retrieve previously cached objects
     * when in non-verbose mode.
     */
    @Test
    public void testDeduplicateNotVerbose() {
        final ImmutableObjectCache c = new ImmutableObjectCache();
        
        // add an object that isn't in the cache
        final ObjectA a1 = new ObjectA(1);
        assertSame(c.deduplicate(a1), a1);
        
        // attempting to add an equivalent object returns the first object
        final ObjectA a2 = new ObjectA(1);
        assertSame(c.deduplicate(a2), a1);
        
        // add a different object that isn't in the cache
        final ObjectB b = new ObjectB(1);
        assertSame(c.deduplicate(b), b);
    }
    
    /**
     * Can call toString to output the size of the cache when in non-verbose mode.
     */
    @Test
    public void testToStringNotVerbose() {
        final ImmutableObjectCache c = new ImmutableObjectCache();
        final ObjectA a1 = new ObjectA(1);
        final ObjectA a2 = new ObjectA(1);
        final ObjectB b = new ObjectB(1);
        c.deduplicate(a1);
        c.deduplicate(a2);
        c.deduplicate(b);
        assertEquals(c.toString(), TO_STRING_PREFIX + "2]");
    }
    
    /**
     * Can add new objects to the cache and retrieve previously cached objects
     * when in verbose mode, and can call toString() to retrieve diagnostic
     * information about the cache. Testing toString() here as well because it's
     * convenient to do so.
     */
    @Test
    public void testDeduplicateVerboseAndToString() {
        final ImmutableObjectCache c = new ImmutableObjectCache();
        c.setVerbose(true);
        
        // add an object that isn't in the cache
        final ObjectA a1 = new ObjectA(1);
        assertSame(c.deduplicate(a1), a1);
        
        // attempting to add exactly the same object returns the object
        assertSame(c.deduplicate(a1), a1);
        
        // attempting to add an equivalent object returns the first object
        final ObjectA a2 = new ObjectA(1);
        assertSame(c.deduplicate(a2), a1);
        
        // add a different object that isn't in the cache
        final ObjectB b = new ObjectB(1);
        assertSame(c.deduplicate(b), b);
        
        // adding an object of a different class but equivalent to a previous
        // object returns the previous object
        final ObjectBB bb = new ObjectBB(1);
        assertSame(c.deduplicate(bb), b);
        
        // add a String to the cache
        final String dummy1 = "dummy1";
        assertSame(c.deduplicate(dummy1), dummy1);
        
        // add several different Strings to the cache, including Strings which 
        // are equivalent but not interned by the JVM.
        final String dummy2 = "dummy2";
        final String dummy3 = "dummy3";
        final String dummy4 = "dummy4";
        final String dummy4_equiv = new String(dummy4);
        final String dummy5 = "dummy5";
        final String dummy5_equiv = new String(dummy5);
        assertSame(c.deduplicate(dummy2), dummy2);
        assertSame(c.deduplicate(dummy3), dummy3);
        assertSame(c.deduplicate(dummy4), dummy4);
        assertSame(c.deduplicate(dummy5), dummy5);
        assertSame(c.deduplicate(dummy4_equiv), dummy4);
        assertSame(c.deduplicate(dummy5_equiv), dummy5);
        
        // add a bunch more objects to the cache then verify toString() outputs 
        // the correct diagnostic information
        c.deduplicate(new ObjectA(2));
        c.deduplicate(new ObjectA(3));
        c.deduplicate(new ObjectB(11));
        c.deduplicate(new ObjectB(13));
        c.deduplicate(new ObjectB(15));
        c.deduplicate(new ObjectBB(99));
        final String toString = c.toString();
        assertTrue(toString.startsWith(TO_STRING_PREFIX + "13]\n"));
        assertTrue(toString.contains("    au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCacheNGTest.ObjectB: new = 4, old = 0, dedupe = 0\n"));
        assertTrue(toString.contains("    java.lang.String: new = 5, old = 0, dedupe = 2\n"));
        assertTrue(toString.contains("    au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCacheNGTest.ObjectA: new = 3, old = 1, dedupe = 1\n"));
        assertTrue(toString.contains("    au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCacheNGTest.ObjectBB: new = 1, old = 0, dedupe = 1\n"));
        assertTrue(toString.endsWith("    saved String bytes = 44\n"));
    }
    
    /**
     * Can call toString to output information about an empty cache when in 
     * verbose mode.
     */
    @Test
    public void testToStringVerboseCacheEmpty() {
        final ImmutableObjectCache c = new ImmutableObjectCache();
        c.setVerbose(true);
        assertEquals(c.toString(), 
                TO_STRING_PREFIX + "0]\n"
                + "    saved String bytes = 0\n");
    }
    
    // dummy class for testing
    private class ObjectA {
        public ObjectA(int i) { this.i = i; }
        private final int i;
        public int getI() { return i; }
        @Override
        public int hashCode() { return 97 * 7 + this.i; }
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ObjectA other = (ObjectA) obj;
            return this.getI() == other.getI();
        }  
    }
    
    // dummy class for testing
    protected class ObjectB {
        public ObjectB(int i) { this.i = i; }
        protected final int i;
        public int getI() { return i; }
        @Override
        public int hashCode() { return 97 * 13 + this.i; }
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            // ensures subclasses will be treated as equal to superclasses
            if (!getClass().isAssignableFrom(obj.getClass()) && 
                    !obj.getClass().isAssignableFrom(getClass())) {
                return false;
            }
            final ObjectB other = (ObjectB) obj;
            return this.getI() == other.getI();
        }  
    }
    
    // dummy class for testing
    // extends another class but is equivalent in every way
    private class ObjectBB extends ObjectB {
        public ObjectBB(int i) { super(i); }
    }
}
