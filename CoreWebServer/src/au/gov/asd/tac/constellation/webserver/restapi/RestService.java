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
 * This also stores status codes.
 * Top section copied from public interface HttpServletResponse.
 * 
 * @author algol
 */
public abstract class RestService {
    public static final int SC_CONTINUE = 100;
    public static final int SC_SWITCHING_PROTOCOLS = 101;
    public static final int SC_OK = 200;
    public static final int SC_CREATED = 201;
    public static final int SC_ACCEPTED = 202;
    public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;
    public static final int SC_NO_CONTENT = 204;
    public static final int SC_RESET_CONTENT = 205;
    public static final int SC_PARTIAL_CONTENT = 206;
    public static final int SC_MULTIPLE_CHOICES = 300;
    public static final int SC_MOVED_PERMANENTLY = 301;
    public static final int SC_MOVED_TEMPORARILY = 302;
    public static final int SC_FOUND = 302;
    public static final int SC_SEE_OTHER = 303;
    public static final int SC_NOT_MODIFIED = 304;
    public static final int SC_USE_PROXY = 305;
    public static final int SC_TEMPORARY_REDIRECT = 307;
    public static final int SC_BAD_REQUEST = 400;
    public static final int SC_UNAUTHORIZED = 401;
    public static final int SC_PAYMENT_REQUIRED = 402;
    public static final int SC_FORBIDDEN = 403;
    public static final int SC_NOT_FOUND = 404;
    public static final int SC_METHOD_NOT_ALLOWED = 405;
    public static final int SC_NOT_ACCEPTABLE = 406;
    public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
    public static final int SC_REQUEST_TIMEOUT = 408;
    public static final int SC_CONFLICT = 409;
    public static final int SC_GONE = 410;
    public static final int SC_LENGTH_REQUIRED = 411;
    public static final int SC_PRECONDITION_FAILED = 412;
    public static final int SC_REQUEST_ENTITY_TOO_LARGE = 413;
    public static final int SC_REQUEST_URI_TOO_LONG = 414;
    public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    public static final int SC_EXPECTATION_FAILED = 417;
    public static final int SC_INTERNAL_SERVER_ERROR = 500;
    public static final int SC_NOT_IMPLEMENTED = 501;
    public static final int SC_BAD_GATEWAY = 502;
    public static final int SC_SERVICE_UNAVAILABLE = 503;
    public static final int SC_GATEWAY_TIMEOUT = 504;
    public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;
    
    // Custom error codes
    public static final int SC_UNPROCESSABLE_ENTITY = 422;

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
     * @return a ServiceResponse with an appropriate custom status code and message
     *
     * @throws java.io.IOException
     */
    public abstract ServiceResponse callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException;

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
