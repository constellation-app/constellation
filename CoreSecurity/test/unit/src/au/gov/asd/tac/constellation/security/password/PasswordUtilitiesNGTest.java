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
package au.gov.asd.tac.constellation.security.password;

import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.openide.util.Lookup;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class PasswordUtilitiesNGTest {

    /**
     * Test of getIV method, of class PasswordUtilities.
     */
    @Test
    public void testGetIV() {
        System.out.println("getIV");
        PasswordUtilities.reset();

        final byte[] firstResult = PasswordUtilities.getIV();
        assertNotNull(firstResult);
        final byte[] secondResult = PasswordUtilities.getIV();
        assertEquals(firstResult, secondResult);
    }

    /**
     * Test of getIV method, of class PasswordUtilities.
     */
    @Test(expectedExceptions = NoSuchElementException.class)
    public void testGetIVException() {
        System.out.println("getIV Exception");
        PasswordUtilities.reset();

        // Exception can only be tested first
        final Lookup mockLookup = mock(Lookup.class);
        when(mockLookup.lookup(PasswordSecret.class)).thenReturn(null);
        try (final MockedStatic<Lookup> mockLookupStatic = Mockito.mockStatic(Lookup.class)) {
            // Setup static mock
            mockLookupStatic.when(Lookup::getDefault).thenReturn(mockLookup);

            // Expect exception to be thrown
            PasswordUtilities.getIV();
        }
    }

    /**
     * Test of getKey method, of class PasswordUtilities.
     */
    @Test
    public void testGetKey() {
        System.out.println("getKey");
        PasswordUtilities.reset();

        final byte[] firstResult = PasswordUtilities.getKey();
        assertNotNull(firstResult);
        final byte[] secondResult = PasswordUtilities.getKey();
        assertEquals(firstResult, secondResult);
    }

    /**
     * Test of getKey method, of class PasswordUtilities.
     */
    @Test(expectedExceptions = NoSuchElementException.class)
    public void testGetKeyException() {
        System.out.println("getKey Exception");
        PasswordUtilities.reset();

        // Exception can only be tested first
        final Lookup mockLookup = mock(Lookup.class);
        when(mockLookup.lookup(PasswordSecret.class)).thenReturn(null);
        try (final MockedStatic<Lookup> mockLookupStatic = Mockito.mockStatic(Lookup.class)) {
            // Setup static mock
            mockLookupStatic.when(Lookup::getDefault).thenReturn(mockLookup);

            // Expect exception to be thrown
            PasswordUtilities.getKey();
        }
    }

    /**
     * Test of generateKey method, of class PasswordUtilities.
     * @throws java.security.NoSuchAlgorithmException
     */
    @Test
    public void testGenerateKey() throws NoSuchAlgorithmException {
        System.out.println("generateKey");

        final byte[] result = PasswordUtilities.generateKey();
        assertNotNull(result);

    }
}
