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
package au.gov.asd.tac.constellation.utilities.ip;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import com.google.common.base.Strings;
import java.net.InetAddress;

/**
 * IP Address Utilities
 *
 * @author arcturus
 */
public class IpAddressUtilities {

    private static final int IPV6_ADDRESS_LENGTH = 8 * 4 + 7;

    private IpAddressUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Return an IPv6 address with 0 padding
     *
     * @param ipv6 An IPv6 address
     * @return A 0 padded IPv6 address
     */
    public static String withPadding(final InetAddress ipv6) {
        if (ipv6 == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        for (final String octet : ipv6.getHostAddress().split(SeparatorConstants.COLON)) {
            sb.append(Strings.padStart(octet, 4, '0'));
            sb.append(SeparatorConstants.COLON);
        }
        return sb.toString().substring(0, IPV6_ADDRESS_LENGTH);
    }
}
