/*
 * Copyright 2010-2019 Australian Signals Directorate
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
 /*
 * This package contains the implementations of the REST endpoints.
 *
 * The transport APIs accept data from a client (via HTTP or file), parse it for the required parameters and input,
 * and call these methods, which return data to the transport APIs to create a response.
 *
 * The implementation methods should have no knowledge of the transport that was used.
 *
 * The implementation methods should be named "verb_endpoint" to match the HTTP endpoint.
 */
package au.gov.asd.tac.constellation.webserver.impl;
