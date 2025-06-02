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

import java.util.List;

/**
 * A listener that hears when the user changes the attributes that are displayed
 * to describe the sender of a message.
 *
 * @author sirius
 */
@FunctionalInterface
public interface ConversationSenderAttributeListener {

    public void senderAttributesChanged(final List<String> possibleSenderAttributes, final List<String> senderAttributes);
}
