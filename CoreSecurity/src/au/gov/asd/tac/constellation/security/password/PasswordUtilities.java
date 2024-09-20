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

import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.openide.util.Lookup;

/**
 * Password Utilities
 * <p>
 * Note that storing obfuscated passwords in source code or configuration files
 * is strongly discouraged. This is NOT a good security practice and might make
 * sense if working locally and as a temporary measure. Once again, strongly
 * discourage using password obfuscation in a production environment. USE AT
 * YOUR OWN RISK!
 *
 * @author arcturus
 */
public class PasswordUtilities {

    public static final String ALG = "AES";
    public static final String ALG_SPEC = "AES/GCM/NoPadding";

    private static byte[] iv = null;
    private static byte[] key = null;
    
    public static final int T_LEN = 128;
    
    private PasswordUtilities() {
        throw new IllegalStateException("Utility class");
    }

    public static byte[] getIV() {
        if (iv == null) {
            final PasswordSecret secret = Lookup.getDefault().lookup(PasswordSecret.class);
            if (secret == null) {
                throw new RuntimeException("Could not find initialisation vector to use.");
            }
            iv = secret.getIV();
        }

        return iv.clone();
    }

    public static byte[] getKey() {
        if (key == null) {
            final PasswordSecret secret = Lookup.getDefault().lookup(PasswordSecret.class);
            if (secret == null) {
                throw new RuntimeException("Could not find password key to use.");
            }
            key = secret.getKey();
        }

        return key.clone();
    }

    public static byte[] generateKey() {
        try {
            final KeyGenerator keyGenerator = KeyGenerator.getInstance(PasswordUtilities.ALG);
            final SecretKey secretKey = keyGenerator.generateKey();
            return secretKey.getEncoded();
        } catch (final NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
}
