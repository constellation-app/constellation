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

import javax.servlet.http.HttpServletResponse;

/**
 * A generic exception for problems in REST services.
 * <p>
 * This provides a transport-independent exception.
 *
 * @author algol
 */
public class RestServiceException extends RuntimeException {

    private final int code;

    public RestServiceException(final int code, final String message) {
        super(message);
        this.code = code;
    }

    public RestServiceException(final String message) {
        super(message);
        this.code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    public RestServiceException(final Exception ex) {
        super(ex);
        this.code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    public int getHttpCode() {
        return this.code;
    }
}
