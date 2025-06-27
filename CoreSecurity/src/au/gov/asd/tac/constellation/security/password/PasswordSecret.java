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

import org.openide.util.lookup.ServiceProvider;

/**
 * A {@link ServiceProvider} to define the key used to obfuscate/de-obfuscate
 * passwords. Note that the key should be used for obfuscation and not
 * encryption.
 * <p>
 * Note that storing obfuscated passwords in source code or configuration files
 * is strongly discouraged. This is NOT a good security practice and might make
 * sense if working locally and as a temporary measure. Once again, strongly
 * discourage using password obfuscation in a production environment. USE AT
 * YOUR OWN RISK!
 *
 * @author arcturus
 */
public interface PasswordSecret {

    /**
     * An initialisation vector to obfuscate/de-obfuscate passwords. This should
     * return a random 16 bit initialisation vector.
     * <p>
     * WARNING: Do not attempt to use this for encryption.
     *
     * @return An initialisation vector which is a 16 bit byte array.
     */
    public byte[] getIV();

    /**
     * A key to obfuscate/de-obfuscate passwords. This should return a random 16
     * bit key.
     * <p>
     * WARNING: Do not attempt to use this for encryption.
     *
     * @return A key which is a 16 bit byte array.
     */
    public byte[] getKey();
}
