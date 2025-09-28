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
package au.gov.asd.tac.constellation.webserver.restapi;

import au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities.HttpMethod;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 * This class holds all REST services that have been registered using the
 * ServiceProvider annotation. It is primarily used to get new instances of
 * registered services from their class names.
 *
 * The preferred pattern for getting a registered service involves storing its
 * name and HTTP method in the registry and then calling
 * {@link #get RestServiceRegistry.get(serviceName, httpMethod)}
 *
 * @author algol
 */
public class RestServiceRegistry {

    private static final Logger LOGGER = Logger.getLogger(RestServiceRegistry.class.getName());

    public static class ServiceKey {

        public final String name;
        public final HttpMethod httpMethod;

        private ServiceKey(final String name, final HttpMethod httpMethod) {
            this.name = name;
            this.httpMethod = httpMethod;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + Objects.hashCode(this.name);
            hash = 29 * hash + Objects.hashCode(this.httpMethod);
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ServiceKey other = (ServiceKey) obj;
            return httpMethod == other.httpMethod && name.equals(other.name);
        }

        @Override
        public String toString() {
            return String.format("%s (%s)", name, httpMethod);
        }
    }

    private static HashMap<ServiceKey, Class<? extends RestService>> servicesMap = null;

    private static synchronized void init() {
        if (servicesMap != null) {
            return;
        }

        // Find all the REST services.
        // We have to instantiate them to get the name and httpMethod; oh well.
        //
        servicesMap = new HashMap<>();
        Lookup.getDefault().lookupAll(RestService.class)
                .stream()
                .forEach(rs -> {
                    servicesMap.put(new ServiceKey(rs.getName(), rs.getHttpMethod()), rs.getClass());
                    final String msg = String.format("Discovered REST service %s (%s): %s", rs.getName(), rs.getHttpMethod(), rs.getDescription());
                    LOGGER.info(msg);
                });
    }

    /**
     * Get an instance of a registered service by key.
     *
     * @param serviceKey The service key.
     *
     * @return A new instance of the named service.
     * @throws IllegalArgumentException If the supplied key did not correspond
     * to a registered service.
     */
    public static RestService get(final ServiceKey serviceKey) {
        return get(serviceKey.name, serviceKey.httpMethod);
    }

    /**
     * Get an instance of a registered service by name.
     *
     * @param name The name of the service.
     * @param httpMethod The HttpMethod of the service.
     *
     * @return A new instance of the named service.
     * @throws IllegalArgumentException If the supplied name did not correspond
     * to a registered service.
     */
    public static RestService get(final String name, final HttpMethod httpMethod) {
        init();

        final ServiceKey key = new ServiceKey(name, httpMethod);
        if (servicesMap.containsKey(key)) {
            final Class<? extends RestService> c = servicesMap.get(key);
            try {
                return c.getDeclaredConstructor().newInstance();
            } catch (final NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException | InstantiationException | IllegalAccessException ex) {
                LOGGER.log(Level.SEVERE, String.format("Error occurred when creating REST service %s", key), ex);
            }
        }

        // Throw a RunTimeException if an invalid name was passed.
        //
        throw new IllegalArgumentException(String.format("No such service as %s (%s)!", name, httpMethod));
    }

    /**
     * Return a set containing all the names and HttpMethods of the available
     * services.
     *
     * @return A set containing all the names and HttpMethods of the available
     * services.
     */
    public static Set<ServiceKey> getServices() {
        init();
        return Collections.unmodifiableSet(servicesMap.keySet());
    }
}
