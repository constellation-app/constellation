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
package au.gov.asd.tac.constellation.utilities.ip;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * IP Address Utilities Test
 *
 * @author arcturus
 */
public class IpAddressUtilitiesNGTest {

    public IpAddressUtilitiesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of withPadding method, of class IpAddressUtilities.
     */
    @Test
    public void testToPaddedStringWithNormalisedIpv6() {
        final InetAddress ipv6;
        try {
            ipv6 = InetAddress.getByName("2400:cb00:2048:1::681c:5f2");
            final String expResult = "2400:cb00:2048:0001:0000:0000:681c:05f2";
            final String result = IpAddressUtilities.withPadding(ipv6);
            assertEquals(result, expResult);
        } catch (UnknownHostException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
    }

    @Test
    public void testToPaddedStringWithLocalhost() {
        final InetAddress ipv6;
        try {
            ipv6 = InetAddress.getByName("::1");
            final String expResult = "0000:0000:0000:0000:0000:0000:0000:0001";
            final String result = IpAddressUtilities.withPadding(ipv6);
            assertEquals(result, expResult);
        } catch (UnknownHostException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
    }

}
