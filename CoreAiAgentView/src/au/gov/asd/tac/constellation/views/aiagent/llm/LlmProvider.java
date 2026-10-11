/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.aiagent.llm;

import java.io.IOException;
import java.util.List;

/**
 * A source of model responses. All LLM client library types stay behind this
 * interface so the agent loop and UI don't depend on them.
 *
 * @author doubtsec
 */
public interface LlmProvider {

    /**
     * @return an identifier for the provider, e.g. "openai" or
     * "compatible:my-endpoint".
     */
    String id();

    /**
     * List the models the endpoint offers.
     *
     * @return the models.
     * @throws IOException if the endpoint can't be reached.
     */
    List<ModelInfo> listModels() throws IOException;

    /**
     * Start streaming a response.
     *
     * @param request the request.
     * @param listener receives the streamed output.
     * @return a handle that can cancel the stream.
     */
    LlmStream stream(LlmRequest request, LlmStreamListener listener);
}
