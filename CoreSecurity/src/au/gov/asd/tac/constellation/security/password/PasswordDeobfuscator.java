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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class allows de-obfuscation of obfuscated passwords.
 * <p>
 * To get the password from an obfuscated password, run this class as an application and enter the obfuscated password
 * at the prompt. The password will be echoed to the screen.
 * <p>
 * Note that storing obfuscated passwords in source code or configuration files is strongly discouraged. This is NOT a
 * good security practice and might make sense if working locally and as a temporary measure. Once again, strongly
 * discourage using password obfuscation in a production environment. USE AT YOUR OWN RISK!
 *
 * @author ruby_crucis
 * @author arcturus
 */
public class PasswordDeobfuscator {

    private static final Logger LOGGER = Logger.getLogger(PasswordDeobfuscator.class.getName());

    /**
     * Return the de-obfuscated password.
     *
     * This is returned as a String which is not ideal. In future this should be changed to a mutable character type.
     *
     * @param password The obfuscated password
     *
     * @return The de-obfuscated password.
     */
    public static String deobfuscate(final ObfuscatedPassword password) {
        final GCMParameterSpec iv = new GCMParameterSpec(PasswordUtilities.T_LEN, PasswordUtilities.getIV());
        final SecretKeySpec key = new SecretKeySpec(PasswordUtilities.getKey(), PasswordUtilities.ALG);
        try {
            final Cipher cipher = Cipher.getInstance(PasswordUtilities.ALG_SPEC);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            final byte[] unencrypted = cipher.doFinal(password.getBytes());
            return new String(unencrypted, 0, unencrypted.length, StandardCharsets.UTF_8);
        } catch (final InvalidKeyException | IllegalBlockSizeException | BadPaddingException
                | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * De-obfuscate a password.
     * <p>
     * To run the main method navigate to constellation\CoreSecurity\build\classes on the command prompt and run the
     * following command:
     * <pre>
     * java -cp {path/to/org-openide-util-lookup.jar};. au.gov.asd.tac.constellation.security.password.PasswordDeobfuscator
     * </pre>
     *
     * @param args
     * @throws java.io.UnsupportedEncodingException
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public static void main(final String[] args) throws IOException {
        final BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8.name()));
        LOGGER.log(Level.INFO, "Enter the obfuscated password to decrypt: ");
        final String password = input.readLine();
        if (password != null) {
            final String deobfuscatedPassword = deobfuscate(new ObfuscatedPassword(password));
            LOGGER.log(Level.INFO, "The password is: {0}", deobfuscatedPassword);
        }
    }
}
