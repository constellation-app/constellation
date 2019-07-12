/*
 * Copyright 2010-2019 Australian Signals Directorate
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
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

    protected static final String ALG = "AES";
    protected static final String ALG_SPEC = ALG; // + "/CBC/PKCS5Padding"; // Specifying the mode and padding causes a parameter error

    /**
     * Either obfuscate a password or (if "-" is entered for the password),
     * create a new key.
     * <p>
     * To run the main method navigate to constellation\Security\build\classes
     * on the command prompt and run the following command:
     * <pre>
     * java -cp . au.gov.asd.tac.constellation.security.password.PasswordObfuscator
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
        System.out.print("Enter the string to encrypt (enter \"-\" to genernate a key): ");
        System.out.flush();
        final String pwd = input.readLine();
        if (pwd != null) {
            if (pwd.equals("-")) { // Key gen mode
                final KeyGenerator keygen = KeyGenerator.getInstance(ALG);
                final SecretKey cv = keygen.generateKey();
                byte[] raw = cv.getEncoded();
                System.out.print("new byte[] {");
                int i = 0;
                for (byte b : raw) {
                    System.out.printf("(byte) 0x%02x", b);
                    if (i < raw.length - 1) {
                        System.out.print(", ");
                    }
                    i = i + 1;
                    if (i % 8 == 0) {
                        System.out.print("\n\t");
                    }
                }
            } else { // Encrypt a password
                final byte[] cleartext = pwd.getBytes(StandardCharsets.UTF_8.name());
                final SecretKey cv = new SecretKeySpec(PasswordUtilities.getKey(), ALG);
                final Cipher cipher = Cipher.getInstance(ALG_SPEC);
                cipher.init(Cipher.ENCRYPT_MODE, cv);
                final byte[] ciphertext = cipher.doFinal(cleartext);
                System.out.print("The obfuscated password is: ");
                for (final byte b : ciphertext) {
                    System.out.printf("%02x", b);
                }
                System.out.flush();
            }
        }
    }
}
