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
import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class PasswordObfuscatorNGTest {
    
    private static final String TEST_STRING = "This is a password 1234!@#$";

    /**
     * Test of obfuscate method, of class PasswordObfuscator.
     */
    @Test
    public void testObfuscate() {
        System.out.println("obfuscate");
        
        // Run obfuscate
        final ObfuscatedPassword op = PasswordObfuscator.obfuscate(TEST_STRING);
        // Assert obfuscation exists, and isn't the same as password
        assertNotNull(op);
        assertFalse(TEST_STRING.equals(op.toString()));
    }
    
    /**
     * Test of main method, of class PasswordObfuscator.
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
        
        // this enables us to control where the input of the program comes from for testing purposes
        try (final ByteArrayInputStream input = new ByteArrayInputStream(TEST_STRING.getBytes())) {
            System.setIn(input);
            
            // this enables us to control where the output of the program goes to for testing purposes
            final TestHandler handler = new TestHandler();
            final Logger logger = Logger.getLogger(PasswordObfuscator.class.getName());
            final boolean useParentHandlers = logger.getUseParentHandlers();
                       
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);
            
            PasswordObfuscator.main(new String[0]);
            final LogRecord logRecord = handler.getLastLog();
            
            final ObfuscatedPassword op = PasswordObfuscator.obfuscate(TEST_STRING);
            
            // the result of providing input into the program should match what you get passing that same input directly into the function
            assertEquals(String.valueOf(logRecord.getParameters()[0]), op.toString());
            
            // restore logger
            logger.removeHandler(handler);
            handler.close();
            logger.setUseParentHandlers(useParentHandlers);
        }
        // restore the I/O default
        System.setIn(System.in);
    }
    
        /**
     * Test of main method, of class PasswordObfuscator. Key Gen Mode
     * @throws java.security.NoSuchAlgorithmException
     * @throws javax.crypto.NoSuchPaddingException
     * @throws java.security.InvalidKeyException
     * @throws javax.crypto.BadPaddingException
     * @throws java.security.spec.InvalidKeySpecException
     * @throws java.io.IOException
     * @throws javax.crypto.IllegalBlockSizeException
     */
    @Test
    public void testMainKeyGen() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, InvalidKeySpecException, IOException, IllegalBlockSizeException {
        System.out.println("mainKeyGen");
        
        // this enables us to control where the input of the program comes from for testing purposes
        try (final ByteArrayInputStream input = new ByteArrayInputStream("-".getBytes())) {
            System.setIn(input);
            
            // this enables us to control where the output of the program goes to for testing purposes
            final TestHandler handler = new TestHandler();
            final Logger logger = Logger.getLogger(PasswordObfuscator.class.getName());
            final boolean useParentHandlers = logger.getUseParentHandlers();
                       
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);
            
            PasswordObfuscator.main(new String[0]);
            final List<LogRecord> logRecords = handler.getLogs();
            
            // now we go through the logs and assert that it logs the way we expect it to
            // the actual key it generates will be different each time 
            assertEquals(logRecords.get(1).getMessage(), "new byte[] {");
            for (int i = 2; i < logRecords.size(); i += 17) {
                for (int j = 0; j < 17; j++) {
                    final String logMessage = logRecords.get(i + j).getMessage();
                    if (i + j == logRecords.size() - 1) {
                        // the last byte is unique in that it prints out one less comma and so we need to break out of the loop before we hit an array out of bounds
                        assertEquals(logMessage, "next byte");
                        break;
                    } else if (j == 16) {
                        assertEquals(logMessage, "next byte");
                    } else if (j % 2 == 0) {
                        assertEquals(logMessage, "(byte) 0x{0}x");
                    } else {
                        assertEquals(logMessage, ", ");                       
                    }
                }
            }
            
            // restore logger
            logger.removeHandler(handler);
            handler.close();
            logger.setUseParentHandlers(useParentHandlers);
        }
        // restore the I/O default
        System.setIn(System.in);
    }
}
