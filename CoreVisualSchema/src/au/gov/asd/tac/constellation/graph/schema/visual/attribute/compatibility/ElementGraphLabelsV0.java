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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.compatibility;

import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An object that holds the descriptions of node and connections labels and node
 * decorators.
 * <p>
 * Note that this should no longer be used and only remains to support legacy
 * graph files.
 *
 * @author algol
 */
@Deprecated
public final class ElementGraphLabelsV0 {

    public static final int MAX_LABELS = 4;
    private final List<ElementGraphLabelV0> labels;

    private static final char LABEL_DELIMITER = ';';
    public static final ElementGraphLabelsV0 NO_LABELS = new ElementGraphLabelsV0(Collections.emptyList());

    /**
     * Construct a new GraphLabels instance from an existing GraphLabels
     * instance.
     *
     * @param graphLabels An existing GraphLabels instance.
     */
    public ElementGraphLabelsV0(final ElementGraphLabelsV0 graphLabels) {
        labels = new ArrayList<>();
        graphLabels.labels.forEach(label -> labels.add(new ElementGraphLabelV0(label)));
    }

    public ElementGraphLabelsV0(final ElementGraphLabelsV0 graphLabels, final List<ElementGraphLabelV0> additionalLabels) {
        final List<ElementGraphLabelV0> allLabels = new ArrayList<>();
        graphLabels.labels.forEach(label -> allLabels.add(new ElementGraphLabelV0(label)));
        additionalLabels.forEach(label -> allLabels.add(label));
        labels = allLabels.size() > MAX_LABELS ? allLabels.subList(0, MAX_LABELS) : allLabels;
    }

    public ElementGraphLabelsV0(final List<ElementGraphLabelV0> labels) {
        if (labels == null) {
            this.labels = Collections.emptyList();
        } else {
            this.labels = labels.size() > MAX_LABELS ? labels.subList(0, MAX_LABELS) : labels;
        }
    }

    public List<ElementGraphLabelV0> getLabels() {
        return Collections.unmodifiableList(labels);
    }

    public int getNumberOfLabels() {
        return labels.size();
    }

    @Override
    public String toString() {
        return StringUtilities.quoteAndDelimitString(labels.stream().map(label -> label.toString()).toList(), LABEL_DELIMITER);
    }

    public static ElementGraphLabelsV0 valueOf(final String graphLabelsString) {
        if (graphLabelsString == null) {
            return NO_LABELS;
        }
        final List<ElementGraphLabelV0> labels = new ArrayList<>();
        final List<String> labelStrings;
        try {
            labelStrings = StringUtilities.unquoteAndSplitString(graphLabelsString, LABEL_DELIMITER);
            labelStrings.forEach(label -> labels.add(ElementGraphLabelV0.valueOf(label)));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("String does not represent a graph label: " + graphLabelsString + "\nCaused by: " + ex.getMessage());
        }
        return new ElementGraphLabelsV0(labels);
    }
}
