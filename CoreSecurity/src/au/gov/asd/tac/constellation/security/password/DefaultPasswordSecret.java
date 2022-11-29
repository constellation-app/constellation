/*
 * Copyright 2010-2021 Australian Signals Directorate
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
 * The default implementation of a {@link PasswordSecret}. It is highly
 * recommended to override this with your own implementation in your module.
 * This only exists as a fall back.
 * <p>
 * Note that storing obfuscated passwords in source code or configuration files
 * is strongly discouraged. This is NOT a good security practice and might make
 * sense if working locally and as a temporary measure. Once again, strongly
 * discourage using password obfuscation in a production environment. USE AT
 * YOUR OWN RISK!
 *
 * @author arcturus
 */
@ServiceProvider(service = PasswordSecret.class)
public class DefaultPasswordSecret implements PasswordSecret {

    @Override
    public byte[] getIV() {
        return new byte[]{
            (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
            (byte) 0x08, (byte) 0x09, (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15
        };
    }

    @Override
    public byte[] getKey() {
        return new byte[]{
            (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
            (byte) 0x08, (byte) 0x09, (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15
        };
    }
}
