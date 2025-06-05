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
package au.gov.asd.tac.constellation.security.proxy;

import java.util.List;
import javafx.util.Pair;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class ProxyUtilitiesNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of parseProxies method, of class ProxyUtilities.
     */
    @Test
    public void testParseProxies() {
        System.out.println("parseProxies");
        
        final String proxies = """
                               .test=my.fun.proxy:8080
                               .another.site=my.other.proxy:8888""";
        
        final List<Pair<String, Pair<String, Integer>>> result = ProxyUtilities.parseProxies(proxies, false);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0), new Pair<>(".test", new Pair<>("my.fun.proxy", 8080)));
        assertEquals(result.get(1), new Pair<>(".another.site", new Pair<>("my.other.proxy", 8888)));
    }
    
    /**
     * Test of parseProxies method, of class ProxyUtilities. No host for one of the proxies
     */
    @Test
    public void testParseProxiesNoHost() {
        System.out.println("parseProxiesNoHost");
        
        final String proxies = """
                               .test=my.fun.proxy:8080
                               =my.other.proxy:8888""";
        
        final List<Pair<String, Pair<String, Integer>>> result = ProxyUtilities.parseProxies(proxies, false);
        final List<Pair<String, Pair<String, Integer>>> ignoreIssuesResult = ProxyUtilities.parseProxies(proxies, true);
        
        // when issues can be ignored, any properly formatted proxies should still be parsed (with ones that aren't ignored)
        // when issues can't be ignored, any errors in any proxy should result in the whole thing returning null
        assertNull(result);
        assertEquals(ignoreIssuesResult.size(), 1);
        assertEquals(ignoreIssuesResult.get(0), new Pair<>(".test", new Pair<>("my.fun.proxy", 8080)));
    }
    
    /**
     * Test of parseProxies method, of class ProxyUtilities. A bad proxy for one of the proxies
     */
    @Test
    public void testParseProxiesBadProxy() {
        System.out.println("parseProxiesBadProxy");
        
        final String proxies = """
                               .test=my.fun.proxy:8080
                               .another.site=my.other.proxy8888""";
        
        final List<Pair<String, Pair<String, Integer>>> result = ProxyUtilities.parseProxies(proxies, false);
        final List<Pair<String, Pair<String, Integer>>> ignoreIssuesResult = ProxyUtilities.parseProxies(proxies, true);
        
        // when issues can be ignored, any properly formatted proxies should still be parsed (with ones that aren't ignored)
        // when issues can't be ignored, any errors in any proxy should result in the whole thing returning null
        assertNull(result);
        assertEquals(ignoreIssuesResult.size(), 1);
        assertEquals(ignoreIssuesResult.get(0), new Pair<>(".test", new Pair<>("my.fun.proxy", 8080)));
    }
    
    /**
     * Test of parseProxies method, of class ProxyUtilities. Missing separator for one of the proxies
     */
    @Test
    public void testParseProxiesMissingSeparator() {
        System.out.println("parseProxiesMissingSeaprator");
        
        final String proxies = """
                               .test=my.fun.proxy:8080
                               .another.sitemy.other.proxy:8888""";
        
        final List<Pair<String, Pair<String, Integer>>> result = ProxyUtilities.parseProxies(proxies, false);
        final List<Pair<String, Pair<String, Integer>>> ignoreIssuesResult = ProxyUtilities.parseProxies(proxies, true);
        
        // when issues can be ignored, any properly formatted proxies should still be parsed (with ones that aren't ignored)
        // when issues can't be ignored, any errors in any proxy should result in the whole thing returning null
        assertNull(result);
        assertEquals(ignoreIssuesResult.size(), 1);
        assertEquals(ignoreIssuesResult.get(0), new Pair<>(".test", new Pair<>("my.fun.proxy", 8080)));
    }

    /**
     * Test of parseProxy method, of class ProxyUtilities.
     */
    @Test
    public void testParseProxy() {
        System.out.println("parseProxy");
        
        assertEquals(ProxyUtilities.parseProxy("my.cool.proxy:8080", false), new Pair<>("my.cool.proxy", 8080));
    }
    
    /**
     * Test of parseProxy method, of class ProxyUtilities. Pass Empty
     */
    @Test
    public void testParseProxyEmpty() {
        System.out.println("parseProxyEmpty");
        
        final Pair<String, Integer> canBeEmptyResult = ProxyUtilities.parseProxy("", true);
        final Pair<String, Integer> cannotBeEmptyResult = ProxyUtilities.parseProxy("", false);
        
        assertEquals(canBeEmptyResult, new Pair<>("", 0));
        assertNull(cannotBeEmptyResult);
    }
    
    /**
     * Test of parseProxy method, of class ProxyUtilities. No host specified
     */
    @Test
    public void testParseProxyNoHost() {
        System.out.println("parseProxyNoHost");
        
        assertNull(ProxyUtilities.parseProxy(":8080", true));
    }
    
    /**
     * Test of parseProxy method, of class ProxyUtilities. Missing separator between host and port
     */
    @Test
    public void testParseProxyMissingSeparator() {
        System.out.println("parseProxyNoHost");
        
        // lack of ':' means that the 8080 is effectively part of the host, i.e. no port
        assertNull(ProxyUtilities.parseProxy("my.cool.proxy8080", true));
    }
}
