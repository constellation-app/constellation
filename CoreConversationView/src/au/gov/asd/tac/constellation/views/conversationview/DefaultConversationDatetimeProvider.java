/*
 * Copyright 2010-2021 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.layout.Region;
import javax.swing.SwingUtilities;

/**
 * The DefaultConversationDatetimeProvider simply creates a datetime for a
 * bubble based on the datetime attribute of a transaction.
 *
 * @author sirius
 */
public class DefaultConversationDatetimeProvider implements ConversationDatetimeProvider {

    @Override
    public void updateDatetimes(GraphReadMethods graph, List<ConversationMessage> messages) {
        assert !SwingUtilities.isEventDispatchThread();
        if (messages.isEmpty()) {
            return; // No messages means nothing to do.
        }
        final int datetimeAttribute = TemporalConcept.TransactionAttribute.DATETIME.get(graph);
        if (datetimeAttribute == Graph.NOT_FOUND) {
            for (ConversationMessage message : messages) {
                message.setDatetime(null);
            }
        } else {
            for (ConversationMessage message : messages) {
                final ZonedDateTime dateTime = (ZonedDateTime) graph.getObjectValue(datetimeAttribute, message.getTransaction());
                if (dateTime == null) {
                    message.setDatetime(null);
                } else {
                    message.setDatetime(new DefaultConversationDatetime(dateTime, TemporalFormatting.ZONED_DATE_TIME_FORMATTER));
                }
            }
        }

    }

    private static class DefaultConversationDatetime implements ConversationDatetime {

        private final ZonedDateTime date;
        private final DateTimeFormatter formatter;

        public DefaultConversationDatetime(ZonedDateTime date, DateTimeFormatter dateFormat) {
            this.date = date;
            this.formatter = dateFormat;
        }

        @Override
        public ZonedDateTime getDate() {
            return date;
        }

        @Override
        public Region createContent() {
            final String timestampString = date != null ? date.format(formatter) : "<unknown>";
            return new SelectableLabel(timestampString, false, "-fx-text-fill: #ffffff;", null, null);
        }

        @Override
        public int compareTo(ConversationDatetime o) {
            return date.compareTo(((DefaultConversationDatetime) o).date);
        }
    }
}
