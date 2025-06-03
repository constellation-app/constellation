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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mockStatic;
import org.openide.util.NbPreferences;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class ConstellationHttpProxySelectorNGTest {
    
    private Preferences p;
    
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
        // set up by ensuring Preferences for this test do not exist
        p = Preferences.userNodeForPackage(ConstellationHttpProxySelectorNGTest.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // clean up, remove Preferences nodes these test plays with
        p.removeNode();
    }

    /**
     * Test of select method, of class ConstellationHttpProxySelector. Parsing empty default proxy values
     * @throws java.util.prefs.BackingStoreException
     * @throws java.net.URISyntaxException
     */
    @Test
    public void testSelectNoProxy() throws BackingStoreException, URISyntaxException {
        System.out.println("selectNoProxy");
        
        try (final MockedStatic<NbPreferences> nbPreferencesMockedStatic = mockStatic(NbPreferences.class, Mockito.CALLS_REAL_METHODS);
                final MockedStatic<ConstellationHttpProxy> constellationHttpProxyMockedStatic = mockStatic(ConstellationHttpProxy.class, Mockito.CALLS_REAL_METHODS)) {
            nbPreferencesMockedStatic.when(() -> NbPreferences.forModule(ProxyPreferenceKeys.class)).thenReturn(p);
            constellationHttpProxyMockedStatic.when(ConstellationHttpProxy::getDefault).thenReturn(null);
            
            p.putBoolean(ProxyPreferenceKeys.USE_DEFAULTS, true);
            
            final URI uri = new URI("http://my.madeup.site");
            final ConstellationHttpProxySelector instance = new ConstellationHttpProxySelector();
            final List<Proxy> result = instance.select(uri);
            // checks should all fail since there are no proxies specified
            assertEquals(result, Collections.singletonList(Proxy.NO_PROXY));           
        }
    }
    
    /**
     * Test of select method, of class ConstellationHttpProxySelector. Matches additional proxy
     * @throws java.util.prefs.BackingStoreException
     * @throws java.net.URISyntaxException
     */
    @Test
    public void testSelectProxyAdditionalProxy() throws BackingStoreException, URISyntaxException {
        System.out.println("selectProxyAdditionalProxy");
        
        try (final MockedStatic<NbPreferences> nbPreferencesMockedStatic = mockStatic(NbPreferences.class, Mockito.CALLS_REAL_METHODS);
                final MockedStatic<ConstellationHttpProxy> constellationHttpProxyMockedStatic = mockStatic(ConstellationHttpProxy.class, Mockito.CALLS_REAL_METHODS)) {
            nbPreferencesMockedStatic.when(() -> NbPreferences.forModule(ProxyPreferenceKeys.class)).thenReturn(p);
            constellationHttpProxyMockedStatic.when(ConstellationHttpProxy::getDefault).thenReturn(new TestHttpProxy());
            
            p.putBoolean(ProxyPreferenceKeys.USE_DEFAULTS, true);
            
            final URI uri = new URI("http://my.madeup.site");
            final ConstellationHttpProxySelector instance = new ConstellationHttpProxySelector();
            final Proxy expProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("my.proxy.additional", 8080));
            final List<Proxy> result = instance.select(uri);
            assertEquals(result, Collections.singletonList(expProxy));           
        }
    }
    
    /**
     * Test of select method, of class ConstellationHttpProxySelector. Default proxy used
     * @throws java.util.prefs.BackingStoreException
     * @throws java.net.URISyntaxException
     */
    @Test
    public void testSelectProxyDefaultProxy() throws BackingStoreException, URISyntaxException {
        System.out.println("selectProxyDefaultProxy");
        
        try (final MockedStatic<NbPreferences> nbPreferencesMockedStatic = mockStatic(NbPreferences.class, Mockito.CALLS_REAL_METHODS);
                final MockedStatic<ConstellationHttpProxy> constellationHttpProxyMockedStatic = mockStatic(ConstellationHttpProxy.class, Mockito.CALLS_REAL_METHODS)) {
            nbPreferencesMockedStatic.when(() -> NbPreferences.forModule(ProxyPreferenceKeys.class)).thenReturn(p);
            constellationHttpProxyMockedStatic.when(ConstellationHttpProxy::getDefault).thenReturn(new TestHttpProxy());
            
            p.putBoolean(ProxyPreferenceKeys.USE_DEFAULTS, true);
            
            final URI uri = new URI("http://my.madeup.other.site");
            final ConstellationHttpProxySelector instance = new ConstellationHttpProxySelector();
            final Proxy expProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("my-proxy.default", 8080));
            final List<Proxy> result = instance.select(uri);
            assertEquals(result, Collections.singletonList(expProxy));           
        }
    }
    
    /**
     * Test of select method, of class ConstellationHttpProxySelector. Bypass proxy
     * @throws java.util.prefs.BackingStoreException
     * @throws java.net.URISyntaxException
     */
    @Test
    public void testSelectProxyBypassProxy() throws BackingStoreException, URISyntaxException {
        System.out.println("selectProxyBypassProxy");
        
        try (final MockedStatic<NbPreferences> nbPreferencesMockedStatic = mockStatic(NbPreferences.class, Mockito.CALLS_REAL_METHODS);
                final MockedStatic<ConstellationHttpProxy> constellationHttpProxyMockedStatic = mockStatic(ConstellationHttpProxy.class, Mockito.CALLS_REAL_METHODS)) {
            nbPreferencesMockedStatic.when(() -> NbPreferences.forModule(ProxyPreferenceKeys.class)).thenReturn(p);
            constellationHttpProxyMockedStatic.when(ConstellationHttpProxy::getDefault).thenReturn(new TestHttpProxy());
            
            p.putBoolean(ProxyPreferenceKeys.USE_DEFAULTS, true);
            
            final URI uri = new URI("https://my.test");
            final ConstellationHttpProxySelector instance = new ConstellationHttpProxySelector();
            final List<Proxy> result = instance.select(uri);
            // matches bypass and therefore no proxy is needed
            assertEquals(result, Collections.singletonList(Proxy.NO_PROXY));           
        }
    }

    /**
     * Test of toString method, of class ConstellationHttpProxySelector.
     * @throws java.util.prefs.BackingStoreException
     */
    @Test
    public void testToString() throws BackingStoreException {
        System.out.println("toString");
        
        try (final MockedStatic<NbPreferences> nbPreferencesMockedStatic = mockStatic(NbPreferences.class, Mockito.CALLS_REAL_METHODS)) {
            nbPreferencesMockedStatic.when(() -> NbPreferences.forModule(ProxyPreferenceKeys.class)).thenReturn(p);
            
            p.putBoolean(ProxyPreferenceKeys.USE_DEFAULTS, false);
            p.put(ProxyPreferenceKeys.DEFAULT, "my-proxy.default:8080");
            
            final String additionalProxies = """
                                             .madeup.site=my.proxy.additional:8080
                                             .another.madeup.site=my.other.proxy:8888""";
            p.put(ProxyPreferenceKeys.ADDITIONAL, additionalProxies);
            
            final String bypassProxies = """
                                         .test1
                                         .test2""";
            p.put(ProxyPreferenceKeys.BYPASS, bypassProxies);
            
            final ConstellationHttpProxySelector instance = new ConstellationHttpProxySelector();
            final String expResult = "[.test1, .test2];[.madeup.site=my.proxy.additional=8080, .another.madeup.site=my.other.proxy=8888];my-proxy.default=8080";
            assertEquals(instance.toString(), expResult);
        }
    }
}
