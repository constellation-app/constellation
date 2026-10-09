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

/**
 * Receives streamed output from a model. Methods are called on a provider
 * thread.
 *
 * @author constellation
 */
public interface LlmStreamListener {

    /**
     * @param delta more response text.
     */
    void onTextDelta(String delta);

    /**
     * @param delta more reasoning text, where the provider exposes it.
     */
    void onReasoningDelta(String delta);

    /**
     * Part of a tool call has arrived.
     *
     * @param index the index of the tool call in the response.
     * @param id the call id, may be null until known.
     * @param name the tool name, may be null until known.
     * @param argumentsDelta more of the argument JSON.
     */
    void onToolCallDelta(int index, String id, String name, String argumentsDelta);

    /**
     * The response is complete.
     *
     * @param response the full response.
     */
    void onComplete(LlmResponse response);

    /**
     * The request failed.
     *
     * @param error what went wrong.
     */
    void onError(Throwable error);
}
