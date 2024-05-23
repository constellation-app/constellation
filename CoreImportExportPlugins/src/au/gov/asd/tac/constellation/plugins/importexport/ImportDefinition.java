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
package au.gov.asd.tac.constellation.plugins.importexport;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * An ImportDefinition provides all the information necessary to import a
 * delimited file into a graph. This is typically created by the user using the
 * delimited import UI.
 *
 * @author sirius
 */
public class ImportDefinition {
    
    // Allows a name to be assigned to importDefiniton, which is used by ImportDelimitedPlugin summary dialog.
    private final String definitionName;

    private final int firstRow;

    private final RowFilter filter;

    private final Map<AttributeType, List<ImportAttributeDefinition>> definitions = new EnumMap<>(AttributeType.class);

    public ImportDefinition(final String definitionName, final int firstRow, final RowFilter filter) {

        this.definitionName = definitionName;
        this.firstRow = firstRow;
        this.filter = filter;

        definitions.put(AttributeType.SOURCE_VERTEX, new ArrayList<>());
        definitions.put(AttributeType.DESTINATION_VERTEX, new ArrayList<>());
        definitions.put(AttributeType.TRANSACTION, new ArrayList<>());
    }

    public void addDefinition(final AttributeType attributeType, final ImportAttributeDefinition definition) {
        definitions.get(attributeType).add(definition);
    }

    /**
     * Get the stored definitionName.
     * @return Stored definitionName string.
     */
    public String getDefinitionName() {
        return definitionName;
    } 
    
    /**
     * The first row that will be imported.
     *
     * @return The first row that will be imported.
     */
    public int getFirstRow() {
        return firstRow;
    }

    public RowFilter getRowFilter() {
        return filter;
    }

    public List<ImportAttributeDefinition> getDefinitions(final AttributeType attributeType) {
        return definitions.get(attributeType);
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append(String.format("[%nFirst row:%d%nFilter:%s%n", firstRow, filter != null ? filter.getScript() : ""));
        for (final AttributeType at : AttributeType.values()) {
            final List<ImportAttributeDefinition> defs = getDefinitions(at);
            defs.stream().forEach(iad -> b.append(String.format("%s %s (column %d)\n", at, iad.toString(), iad.getColumnIndex())));
        }

        b.append("]");

        return b.toString();
    }
}
