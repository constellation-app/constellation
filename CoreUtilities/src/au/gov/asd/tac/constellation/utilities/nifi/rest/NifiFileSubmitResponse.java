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
package au.gov.asd.tac.constellation.utilities.nifi.rest;

import au.gov.asd.tac.constellation.utilities.rest.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A response class used when submitting files to Niagara Files.
 *
 * @author cygnus_x-1
 */
public class NifiFileSubmitResponse extends Response {

    public NifiFileSubmitResponse(final int code, final String message, final Map<String, List<String>> headers, final byte[] bytes) throws IOException {
        super(code, message, headers, bytes, false);
    }
}
