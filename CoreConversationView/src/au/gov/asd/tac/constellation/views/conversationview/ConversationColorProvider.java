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
 * The ConversationColorProvider updates the background colors of bubbles
 * depending on their content and position within the conversation. The default
 * provider colors all bubbles on the left hand side the same while coloring
 * right hand side bubbles by their sender.
 *
 * @author sirius
 */
public interface ConversationColorProvider {

    public void updateMessageColors(final GraphReadMethods graph, final List<ConversationMessage> messages);
}
