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
package au.gov.asd.tac.constellation.views.conversationview;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.utilities.temporal.TimeZoneUtilities;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.List;

/**
 * The DefaultConversationBackgroundProvider updates the background color behind
 * the bubbles to alternating colors so that bubbles that occur on the same day
 * are the same color.
 *
 * @author sirius
 */
public class DefaultConversationBackgroundProvider implements ConversationBackgroundProvider {

    @Override
    public void updateMessageBackgrounds(GraphReadMethods graph, List<ConversationMessage> messages) {

        //Background bg = new Background(new BackgroundFill(Color.RED, null, null));
        String[] colors = new String[]{"transparent", "rgb(60, 60, 60)"};
        int currentColor = 1;
        int currentDay = -1;

        for (ConversationMessage message : messages) {
            // Get the unique day since the epcoh of the date-time in UTC.
            final int day = (int) ZonedDateTime.of(message.getDatetime().getDate().toLocalDateTime(), TimeZoneUtilities.UTC).getLong(ChronoField.EPOCH_DAY);

            if (day != currentDay) {
                currentColor ^= 1;
                currentDay = day;
            }

            message.setBackgroundColor(colors[currentColor]);
        }
    }
}
