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
package au.gov.asd.tac.constellation.webserver.services;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openide.util.lookup.ServiceProvider;

/**
 * Reflect the JSON in the request body as output to the response body.
 * <p>
 * This is an example to ensure JSON input and output works.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class ReflectJSON extends RestService {

    private static final String NAME = "reflect_json";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Reflect the JSON in the request body as output to the response body. This is an example to ensure JSON input and output works.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"example"};
    }

    @Override
    public RestServiceUtilities.HttpMethod getHttpMethod() {
        return RestServiceUtilities.HttpMethod.POST;
    }

    @Override
    public void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode json = mapper.readTree(in);
        mapper.writeValue(out, json);
    }
}
