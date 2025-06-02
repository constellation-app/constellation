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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities.HttpMethod;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The definition of a REST service.
 * <p>
 * REST services are accessed via the internal web server by REST clients. They
 * are not otherwise available from the CONSTELLATION user interface.
 * <p>
 * Services use createParameters() just like plugins. A PluginParameters
 * instance is populated by reading from the URL, no matter whether GET, POST,
 * or whatever is used. Service parameters are expected to be fairly simple, so
 * this shouldn't be a problem. Anything complex probably belongs in a plugin,
 * which you then use the run_plugin service to run.
 * <p>
 * The descriptive get() methods in RestService and PluginParameter are used to
 * dynamically build a Swagger config file: see SwaggerServlet for details.
 * <p>
 * All services are accessed via RestServiceServlet; see that for more details.
 *
 * This also stores status codes that are not available in the HttpURLConnection
 * class.
 *
 * @author algol
 */
public abstract class RestService {

    // Status codes that are not available in the HttpURLConnection class
    public static final int HTTP_UNPROCESSABLE_ENTITY = 422;

    /**
     * The name of the service as it appears in the URL to be called.
     *
     * @return The service name.
     */
    public abstract String getName();

    /**
     * A user-readable description of this service.
     *
     * This is typically used by a user interface to provide more information
     * about what a service does.
     *
     * @return A description of the service.
     */
    public abstract String getDescription();

    /**
     * Return a list of tags for this service.
     *
     * Tags are used through out Constellation to categorise and filter
     * services.
     *
     * @return a list of tags for this service.
     */
    public abstract String[] getTags();

    /**
     * The HTTP method used to call this service.
     *
     * Two services may have the same name if they have different HTTP methods.
     *
     * Get is a typical HTTP request, so this is the default.
     *
     * @return One of "GET", "POST", "PUT" (case-sensitive).
     */
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }

    /**
     * Creates the parameters for this service.
     *
     * The default is provided for services that take no parameters.
     *
     * @return
     */
    public PluginParameters createParameters() {
        return new PluginParameters();
    }

    /**
     * A generic REST service.
     *
     * REST services accept URL parameters and arbitrary data as input, and
     * return arbitrary data as output. Typically, the input and output may very
     * well be JSON.
     *
     * @param parameters The parameters passed from the service request to the
     * service.
     * @param in The body of the HTTP request.
     * @param out The body of the HTTP response.
     *
     * @throws java.io.IOException
     */
    public abstract void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException;

    /**
     * The MIME type of the data returned by the service.
     *
     * JSON is a typical output type, so this is the default.
     *
     * @return A String containing a MIME type.
     */
    public String getMimeType() {
        return RestServiceUtilities.APPLICATION_JSON;
    }
}
