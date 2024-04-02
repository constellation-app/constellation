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
package au.gov.asd.tac.constellation.views.conversationview;

/**
 *
 * @author Delphinus8821
 */
public class ConversationController {
    
    // Conversation view controller instance
    private static ConversationController instance = null;
    
    private final Conversation conversation = new Conversation();
    private final ConversationBox conversationBox = new ConversationBox(conversation);
    
    /**
     * Singleton instance retrieval
     *
     * @return the instance, if one is not made, it will make one.
     */
    public static synchronized ConversationController getDefault() {
        if (instance == null) {
            instance = new ConversationController();
        }
        return instance;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public ConversationBox getConversationBox() {
        return conversationBox;
    }
    
    public void updateComponent() {
        getConversation().getGraphUpdateManager().setManaged(true);
    }
    
}
