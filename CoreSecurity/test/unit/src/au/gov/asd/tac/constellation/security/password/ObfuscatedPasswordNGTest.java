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

import java.security.InvalidParameterException;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class ObfuscatedPasswordNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of constructor, of class ObfuscatedPassword. Password is of odd length which is a bad password
     */
    @Test(expectedExceptions = InvalidParameterException.class, expectedExceptionsMessageRegExp = "Obfuscated password is the wrong length")
    public void testConstructorBadPassword() {
        System.out.println("constructorBadPassword");
        
        new ObfuscatedPassword("123ab");
    }
    
    /**
     * Test of getBytes method, of class ObfuscatedPassword.
     */
    @Test
    public void testGetBytes() {
        System.out.println("getBytes");
        
        final ObfuscatedPassword instance = new ObfuscatedPassword("123abc");
        byte[] result = instance.getBytes();
        
        assertEquals(result.length, 3);
        assertEquals(result[0], (byte) 0x12);
        assertEquals(result[1], (byte) 0x3a);
        assertEquals(result[2], (byte) 0xbc);
    }
}
