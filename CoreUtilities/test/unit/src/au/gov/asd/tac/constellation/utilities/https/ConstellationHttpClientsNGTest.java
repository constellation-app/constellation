/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.https;

import au.gov.asd.tac.constellation.security.ConstellationSecurityContext;
import au.gov.asd.tac.constellation.security.ConstellationSecurityManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;
import javax.net.ssl.SSLContext;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.mockito.MockedStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Tests for {@link ConstellationHttpClients}.
 */
public class ConstellationHttpClientsNGTest {

    @Test
    public void testNewBuilderUsesSecurityContext() throws Exception {
        final SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(null, null, null);
        final ConstellationSecurityContext context = mock(ConstellationSecurityContext.class);
        when(context.getSSLContext()).thenReturn(ssl);

        try (final MockedStatic<ConstellationSecurityManager> manager = mockStatic(ConstellationSecurityManager.class)) {
            manager.when(ConstellationSecurityManager::getCurrentSecurityContext).thenReturn(context);
            final HttpClient client = ConstellationHttpClients.newBuilder(true).build();
            assertSame(client.sslContext(), ssl);
            assertTrue(client.proxy().isPresent());
            assertTrue(client.connectTimeout().isPresent());
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testNewBuilderFailsClosedWithoutContext() {
        try (final MockedStatic<ConstellationSecurityManager> manager = mockStatic(ConstellationSecurityManager.class)) {
            manager.when(ConstellationSecurityManager::getCurrentSecurityContext).thenReturn(null);
            ConstellationHttpClients.newBuilder(true);
        }
    }

    @Test
    public void testNewBuilderWithoutContextWhenNotRequired() {
        try (final MockedStatic<ConstellationSecurityManager> manager = mockStatic(ConstellationSecurityManager.class)) {
            manager.when(ConstellationSecurityManager::getCurrentSecurityContext).thenReturn(null);
            final HttpClient client = ConstellationHttpClients.newBuilder(false).build();
            assertTrue(client.proxy().isPresent());
        }
    }

    @Test
    public void testProxySelectorFollowsDefault() {
        final ProxySelector original = ProxySelector.getDefault();
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved("proxy", 8080));
        final ProxySelector replacement = new ProxySelector() {
            @Override
            public List<Proxy> select(final URI uri) {
                return List.of(proxy);
            }

            @Override
            public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {
                // not used
            }
        };
        try {
            final ProxySelector live = new ConstellationHttpClients.LiveDefaultProxySelector();
            ProxySelector.setDefault(replacement);
            assertEquals(live.select(URI.create("https://example.com")), List.of(proxy));
            ProxySelector.setDefault(null);
            assertEquals(live.select(URI.create("https://example.com")), List.of(Proxy.NO_PROXY));
        } finally {
            ProxySelector.setDefault(original);
        }
    }
}
