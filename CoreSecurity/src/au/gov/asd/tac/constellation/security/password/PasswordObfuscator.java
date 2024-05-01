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
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class allows creation of obfuscated passwords and keys.
 * <p>
 * To obfuscate a password, run this class as an application and enter the
 * password at the prompt (it will be echoed to the screen). You can now pass in
 * your obfuscated password as a string enclosed inside
 * <code>new ObfuscatedPassword(...)</code>
 * <p>
 * To generate a new key, run this class as an application and enter "-" at the
 * prompt. A key will be generated. Copy the Java array which is printed out
 * into the definition of <code>raw</code>.
 * <p>
 * This class cannot be combined with <code>ObfuscatedPassword</code>.
 * <p>
 * Note that storing obfuscated passwords in source code or configuration files
 * is strongly discouraged. This is NOT a good security practice and might make
 * sense if working locally and as a temporary measure. Once again, strongly
 * discourage using password obfuscation in a production environment. USE AT
 * YOUR OWN RISK!
 *
 * @author ruby_crucis
 * @author arcturus
 *
 */
public class PasswordObfuscator {

    private static final Logger LOGGER = Logger.getLogger(PasswordObfuscator.class.getName());

    public static final String KEYGEN_SYMBOL = "-";

    /**
     * Obfuscate a password.
     *
     * @param password The password as a String.
     *
     * @return The obfuscated password.
     */
    public static ObfuscatedPassword obfuscate(final String password) {
        final IvParameterSpec iv = new IvParameterSpec(PasswordUtilities.getIV());
        final SecretKey key = new SecretKeySpec(PasswordUtilities.getKey(), PasswordUtilities.ALG);
        try {
            final Cipher cipher = Cipher.getInstance(PasswordUtilities.ALG_SPEC);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            final byte[] encrypted = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
            final StringBuilder encryptedHex = new StringBuilder();
            for (final byte b : encrypted) {
                encryptedHex.append(String.format("%02x", b));
            }
            return new ObfuscatedPassword(encryptedHex.toString());
        } catch (final InvalidKeyException | IllegalBlockSizeException | BadPaddingException
                | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Either obfuscate a password or (if "-" is entered for the password),
     * create a new key.
     * <p>
     * To run the main method navigate to constellation\Security\build\classes
     * on the command prompt and run the following command:
     * <pre>
     * java -cp {path/to/org-openide-util-lookup.jar};. au.gov.asd.tac.constellation.security.password.PasswordObfuscator
     * </pre>
     * <p>
     * Should you decide to create a new key, note that all existing obfuscated
     * passwords will need to be re-obfuscated using the new key. You need to
     * de-obfuscate the passwords using the old key first (via
     * <code>PasswordDeobfuscator</code>) and re-obfuscate the password with the
     * new key.
     *
     * @param args
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException
     * @throws InvalidKeySpecException
     */
    @SuppressWarnings("unused")
    public static void main(final String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, InvalidKeySpecException, IOException, IllegalBlockSizeException {
        final BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8.name()));
        LOGGER.log(Level.INFO, "Enter the string to encrypt (enter \"-\" to genernate a key): ");
        final String password = input.readLine();
        if (password != null) {
            if (password.equals(KEYGEN_SYMBOL)) { // Key gen mode
                final byte[] encodedKey = PasswordUtilities.generateKey();
                LOGGER.log(Level.INFO, "new byte[] {");
                int i = 0;
                for (final byte b : encodedKey) {
                    LOGGER.log(Level.INFO, "(byte) 0x{0}x", b);
                    if (i < encodedKey.length - 1) {
                        LOGGER.log(Level.INFO, ", ");
                    }
                    i = i + 1;
                    if (i % 8 == 0) {
                        LOGGER.log(Level.INFO, "next byte");
                    }
                }
            } else { // Encrypt a password
                final ObfuscatedPassword obfuscatedPassword = obfuscate(password);
                LOGGER.log(Level.INFO, "The obfuscated password is: {0}", obfuscatedPassword.toString());
            }
        }
    }
}
