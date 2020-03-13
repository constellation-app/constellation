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
package au.gov.asd.tac.constellation.webserver.api;

import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameter;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.webserver.ServiceRegistry;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.WebServer.ConstellationHttpServlet;
import au.gov.asd.tac.constellation.webserver.restapi.ServiceUtilities.HttpMethod;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openide.util.lookup.ServiceProvider;

/**
 * A web service that allows a client to call CONSTELLATION REST services.
 *
 * @author algol
 */
@ServiceProvider(service = ConstellationHttpServlet.class)
@WebServlet(
    name = "ServicesAPI",
    description = "REST API for services",
    urlPatterns = {"/v1/service/*"})
public class RestServiceServlet extends ConstellationApiServlet {

    @Override
    protected void get(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        call_service(HttpMethod.GET, request, response);
    }

    @Override
    protected void post(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        call_service(HttpMethod.POST, request, response);
    }

    @Override
    protected void put(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        call_service(HttpMethod.PUT, request, response);
    }

    private void call_service(final HttpMethod httpMethod, final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException{
        // Which service is being called?
        //
        final String serviceName = request.getPathInfo().substring(1);

        // Get an instance of the service (if it exists).
        //
        final RestService rs = ServiceRegistry.get(serviceName, httpMethod);

        // Convert the arguments in the URL of the request to PluginParameters.
        //
        final PluginParameters parameters = rs.createParameters();
        final Map<String, String[]> paramMap = request.getParameterMap();

        paramMap.entrySet().forEach(entry -> {
            final String parameterName = entry.getKey();
            if(parameters.hasParameter(parameterName)) {
                final PluginParameter<?> param = parameters.getParameters().get(parameterName);
                if(entry.getValue().length==1) {
                    param.setStringValue(entry.getValue()[0]);
                } else {
                    throw new EndpointException("Service parameters do not accept multiple values");
                }
            } else {
                throw new EndpointException(String.format("Service %s has no such parameter: %s", serviceName, parameterName));
            }
        });

        try {
            rs.service(parameters, request.getInputStream(), response.getOutputStream());
        } catch(final IOException | RuntimeException ex) {
            throw new ServletException(ex);
        }

        response.setContentType(rs.getMimeType());
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
