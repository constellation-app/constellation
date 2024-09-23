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
package au.gov.asd.tac.constellation.security.password;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class PasswordDeobfuscatorNGTest {

    /**
     * Test of deobfuscate method, of class PasswordDeobfuscator.
     */
    @Test
    public void testDeobfuscate() {
        System.out.println("deobfuscate");

        final String password = "This is a password 1234!@#$";
        // Run obfuscate
        final ObfuscatedPassword op = PasswordObfuscator.obfuscate(password);
        // Assert obfuscation exists, and isn't the same as password
        assertNotNull(op);
        assertFalse(password.equals(op.toString()));

        // Run deobfuscate
        final String result = PasswordDeobfuscator.deobfuscate(op);
        // Assert original password and deobfuscated match
        assertEquals(result, password);
    }

}
