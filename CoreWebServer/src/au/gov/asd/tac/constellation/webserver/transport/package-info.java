/*
 * Copyright 2010-2024 Australian Signals Directorate
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
 * This package implements a file-based transport for the REST API.
 *
 * The HTTP transport works nicely when CONSTELLATION and the client (typically
 * a Jupyter notebook) are running on the same workstation. However, if the
 * client is running elsewhere (for example, an Apache Zeppelin notebook server
 * running on a dedicated system), using HTTP is problematic. Additional security
 * (eg SSL) is required, and the user has to do something to connect the notebook
 * with CONSTELLATION (eg provide IP addresses). This assumes that firewalls etc
 * allow such connections.
 *
 * A file-based transport assumes that a user's notebook and CONSTELLATION share
 * a mutually accessible filesystem.
 *
 * CONSTELLATION monitors a directory (by default, $HOME/.CONSTELLATION/REST)
 * looking for one of the filenames get.json, post.json, put.json. Each of these
 * corresponds to the equivalent HTTP verb, and contains a JSON document which
 * mirrors the HTTP request parameters. Individual endpoints may also require a
 * data.json file containing what would be the data stream passed over HTTP.
 * (Clients should write the data file first, then the verb file using a temporary
 * filename, then rename the verb file, so the file monitor sees the new files
 * only after they have been written.)
 *
 * CONSTELLATION responds by deleting the input files and creating a response.json
 * file (again, using a temporary filename first). The response file contains status
 * data (successful, unsuccessful, etc). There may also be a command-specific file.
 * For example, the screenshot request may additionally write a response.png file.
 *
 * After writing the requests files, the client then monitors the directory for the
 * response.json file and reads it (and any other command-specific files).
 * The client can then optionally remove the response files. (If not removed, they
 * will be overwritten by CONSTELLATION on the next request.)
 */
package au.gov.asd.tac.constellation.webserver.transport;
