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
package au.gov.asd.tac.constellation.webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * The definition of a REST service.
 * <p>
 * REST services are accessed via the internal web server by REST clients.
 * They are not otherwise available from the CONSTELLATION user interface.
 *
 * @author algol
 */
public interface RestService {

    /**
     * The name of the service as it appears in the URL to be called.
     *
     * @return The service name.
     */
    String getName();

    /**
     * A generic REST service.
     * <p>
     * REST services accept arbitrary JSON as input, and return arbitrary JSON
     * as output.
     *
     * @param args URL arguments as returned by request.getParameterMap().
     * @param in The body of the HTTP request.
     * @param out The body of the response.
     */
    void service(Map<String, String[]> args, InputStream in, OutputStream out) throws IOException;
}
