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
package au.gov.asd.tac.constellation.webserver.api;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.webserver.WebServer.ConstellationHttpServlet;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceRegistry;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities.HttpMethod;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 * A web service that allows a client to call CONSTELLATION REST services.
 * <p>
 * All services are accessed in the same way, regardless of the HTTP verb used.
 * The plugin name is in the last part of the URL (request.getPathInfo()). This
 * is used to look up the service in the ServiceRegistry. The service's
 * createParameters() method is called to get a PluginParameters instance, which
 * is then used to parse parameters from the query section of the URL (if any).
 * <p>
 * The service is then called, passing the populated PluginParameters instance,
 * and the HTTP request's input and output streams.
 *
 * The URL pattern *must* match the FileListener and Swagger/OpenAPI patterns.
 *
 * @author algol
 */
@ServiceProvider(service = ConstellationHttpServlet.class)
@WebServlet(
        name = "ServicesAPI",
        description = "REST API for services",
        urlPatterns = {"/v2/service/*"})
public class RestServiceServlet extends ConstellationApiServlet {

    @Override
    protected void get(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        callService(HttpMethod.GET, request, response);
    }

    @Override
    protected void post(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        callService(HttpMethod.POST, request, response);
    }

    @Override
    protected void put(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        callService(HttpMethod.PUT, request, response);
    }

    private void callService(final HttpMethod httpMethod, final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // Which service is being called?
        //
        final String serviceName = request.getPathInfo().substring(1);

        // Get an instance of the service (if it exists).
        //
        final RestService rs = RestServiceRegistry.get(serviceName, httpMethod);

        // Convert the arguments in the URL of the request to PluginParameters.
        //
        final PluginParameters parameters = rs.createParameters();
        final Map<String, String[]> paramMap = request.getParameterMap();

        paramMap.entrySet().forEach(entry -> {
            final String parameterName = entry.getKey();
            if (parameters.hasParameter(parameterName)) {
                final PluginParameter<?> param = parameters.getParameters().get(parameterName);
                if (entry.getValue().length == 1) {
                    param.setStringValue(entry.getValue()[0]);
                } else {
                    throw new RestServiceException("Service parameters do not accept multiple values");
                }
            } else {
                throw new RestServiceException(String.format("Service '%s' has no parameter '%s'", serviceName, parameterName));
            }
        });

        // Call the service.
        //
        try {
            response.setContentType(rs.getMimeType());
            response.setStatus(HttpServletResponse.SC_OK);
            rs.callService(parameters, request.getInputStream(), response.getOutputStream());
        } catch (final RestServiceException ex) {
            throw ex;
        } catch (final IOException | RuntimeException ex) {
            throw new ServletException(ex);
        }
    }
}
