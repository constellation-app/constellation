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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class PasswordDeobfuscatorNGTest {
    
    private static final String TEST_STRING = "This is a password 1234!@#$";

    /**
     * Test of deobfuscate method, of class PasswordDeobfuscator.
     */
    @Test
    public void testDeobfuscate() {
        System.out.println("deobfuscate");
        
        // Run obfuscate
        final ObfuscatedPassword op = PasswordObfuscator.obfuscate(TEST_STRING);
        // Assert obfuscation has occurred
        assertFalse(TEST_STRING.equals(op.toString()));

        // Run deobfuscate
        final String result = PasswordDeobfuscator.deobfuscate(op);
        // Assert original password and deobfuscated match
        assertEquals(result, TEST_STRING);
    }
    
    /**
     * Test of main method, of class PasswordDeobfuscator.
     * @throws java.security.NoSuchAlgorithmException
     * @throws javax.crypto.NoSuchPaddingException
     * @throws java.security.InvalidKeyException
     * @throws javax.crypto.BadPaddingException
     * @throws java.security.spec.InvalidKeySpecException
     * @throws java.io.IOException
     * @throws javax.crypto.IllegalBlockSizeException
     */
    @Test
    public void testMain() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, InvalidKeySpecException, IOException, IllegalBlockSizeException {
        System.out.println("main");
        
        final ObfuscatedPassword op = PasswordObfuscator.obfuscate(TEST_STRING);
        
        // this enables us to control where the input of the program comes from for testing purposes
        try (final ByteArrayInputStream input = new ByteArrayInputStream(op.toString().getBytes())) {
            System.setIn(input);
            
            // this enables us to control where the output of the program goes to for testing purposes
            final TestHandler handler = new TestHandler();
            final Logger logger = Logger.getLogger(PasswordDeobfuscator.class.getName());
            final boolean useParentHandlers = logger.getUseParentHandlers();
                       
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);
            
            PasswordDeobfuscator.main(new String[0]);
            final LogRecord logRecord = handler.getLastLog();
                       
            // the result of providing input into the program should match the original string
            assertEquals(String.valueOf(logRecord.getParameters()[0]), TEST_STRING);
            
            // restore logger
            logger.removeHandler(handler);
            handler.close();
            logger.setUseParentHandlers(useParentHandlers);
        }
        // restore the I/O default
        System.setIn(System.in);
    }
}
