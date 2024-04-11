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
package au.gov.asd.tac.constellation.views.tableview.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import javafx.scene.control.TableColumn;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * De-serializer for the {@link Pair} object that represent the column sort
 * order. If the sort direction is null or an empty string then ascending is
 * chosen.
 * <p/>
 * This de-serializer is being used primarily to handle the setting of the
 * default sort order when the value in the JSON is an empty string. This only
 * happens in preferences saved prior to version 2.5.
 *
 * @author formalhaunt
 */
public class ColumnSortOrderDeserializer extends JsonDeserializer<Pair<String, TableColumn.SortType>> {

    /**
     * De-serializes JSON into a column sort order pair. If the sort direction
     * is null or an empty string then ascending is chosen.
     *
     * @param jp the JSON parser processing the JSON
     * @param dc the current de-serialization context
     * @return the generated column sort order pair
     * @throws IOException if there is an issue de-serializing the JSON
     * @throws JsonProcessingException if there is an issue de-serializing the
     * JSON
     */
    @Override
    public Pair<String, TableColumn.SortType> deserialize(final JsonParser jp,
            final DeserializationContext dc) throws IOException, JsonProcessingException {
        final JsonNode node = jp.getCodec().readTree(jp);

        final String left = node.fieldNames().next();

        final TableColumn.SortType right = StringUtils.isBlank(node.get(left).asText())
                ? TableColumn.SortType.ASCENDING : TableColumn.SortType.valueOf(node.get(left).asText());

        return ImmutablePair.of(left, right);
    }

}
