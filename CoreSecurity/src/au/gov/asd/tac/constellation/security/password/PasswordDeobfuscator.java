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
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class allows de-obfuscation of obfuscated passwords.
 * <p>
 * To get the password from an obfuscated password, run this class as an
 * application and enter the obfuscated password at the prompt. The password
 * will be echoed to the screen.
 * <p>
 * Note that storing obfuscated passwords in source code or configuration files
 * is strongly discouraged. This is NOT a good security practice and might make
 * sense if working locally and as a temporary measure. Once again, strongly
 * discourage using password obfuscation in a production environment. USE AT
 * YOUR OWN RISK!
 *
 * @author ruby_crucis
 * @author arcturus
 */
public class PasswordDeobfuscator {

    protected static final String ALG = "AES";
    protected static final String ALG_SPEC = ALG; // + "/CBC/PKCS5Padding"; // Specifying the mode and padding causes a parameter error

    /**
     * Return the de-obfuscated password.
     *
     * This is returned as a String which is not ideal. In future this should be
     * changed to a mutable character type.
     *
     * @param pwd The obfuscated password
     *
     * @return The de-obfuscated password.
     */
    public static CharSequence deobfuscate(final ObfuscatedPassword pwd) {
        final byte[] encPassword = pwd.getBytes();
        final SecretKeySpec cv = new SecretKeySpec(PasswordUtilities.getKey(), ALG);
        try {
            final Cipher cipher = Cipher.getInstance(ALG_SPEC);
            cipher.init(Cipher.DECRYPT_MODE, cv);
            final byte[] plainText = cipher.doFinal(encPassword);
            return new String(plainText, 0, plainText.length, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * De-obfuscate a password.
     * <p>
     * To run the main method navigate to
     * constellation\CoreSecurity\build\classes on the command prompt and run
     * the following command:
     * <pre>
     * java -cp . au.gov.asd.tac.constellation.security.password.PasswordDeobfuscator
     * </pre>
     *
     * @param args
     * @throws java.io.UnsupportedEncodingException
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public static void main(final String[] args) throws UnsupportedEncodingException, IOException {
        final BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8.name()));
        System.out.print("Enter the obfuscated password to decrypt: ");
        System.out.flush();
        final String pwd = input.readLine();
        if (pwd != null) {
            System.out.print("The password is: ");
            System.out.println(deobfuscate(new ObfuscatedPassword(pwd)).toString());
            System.out.flush();
        }

    }
}
