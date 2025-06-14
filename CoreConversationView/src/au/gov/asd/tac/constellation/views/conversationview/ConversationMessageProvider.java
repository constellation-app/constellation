/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.conversationview;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.util.List;

/**
 * Implements an algorithm that populates a list of messages based on the state
 * of a provided graph.
 *
 * @author sirius
 */
public interface ConversationMessageProvider {

    /**
     * Populates the messages list based on the provided graph.
     *
     * @param graph the provided graph.
     * @param messages the list of messages to populate.
     */
    public void getMessages(final GraphReadMethods graph, final List<ConversationMessage> messages);
    
    public int getTotalMessageCount();
    
}
