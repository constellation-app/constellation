/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import java.util.Locale;

/**
 *
 * @author algol
 */
public class ServiceUtilities {
    private ServiceUtilities() {
    }
    
    /**
     * Constants for the HTTP method used by a service.
     */
    public static enum HttpMethod {
        GET,
        POST,
        PUT;

        /**
         * A helper to convert a string to an HttpMethod without throwing an exception.
         *
         * @param s The String to be converted.
         *
         * @return An enum member, or null if the String was invalid.
         */
        public static HttpMethod getValue(final String s) {
            try {
                return HttpMethod.valueOf(s.toUpperCase(Locale.US));
            }
            catch(final IllegalArgumentException ex) {
                return null;
        }
}
    }

    /**
     * Constants for common MIME types.
     */

    public static final String APPLICATION_JSON = "application/json";
    public static final String IMAGE_PNG = "image/png";

    /**
     * Build a parameter id for a service parameter in a consistent way.
     *
     * @param serviceName The service name.
     * @param parameterName The parameter name.
     *
     * @return A parameter id that is unique to the service.
     */
    public static String buildId(final String serviceName, final String parameterName) {
        return String.format("%s.%s", serviceName, parameterName);
    }
}
