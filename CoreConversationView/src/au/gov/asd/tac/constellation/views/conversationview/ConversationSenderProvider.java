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
 * A ConversationSenderProvider provides sender information for a bubble based
 * on the properties of a message.
 *
 * @author sirius
 */
public interface ConversationSenderProvider {

    public void updateMessageSenders(final GraphReadMethods graph, final List<ConversationMessage> messages, final List<String> senderAttributes);
}
