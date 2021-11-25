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
package au.gov.asd.tac.constellation.graph.schema.visual;

import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * An object that holds the descriptions of node and connections labels and node
 * decorators.
 *
 * @author algol
 */
public final class GraphLabels {

    public static final int MAX_LABELS = 4;
    private final List<GraphLabel> labels;

    /**
     * ColorValue delimits using "," and GraphLabel delimits using ";", so we'll
     * use "|".
     */
    private static final String DELIMITER = "|";

    public static final GraphLabels NO_LABELS = new GraphLabels(Collections.emptyList());

    /**
     * Construct a new GraphLabels instance from an existing GraphLabels
     * instance.
     *
     * @param graphLabels An existing GraphLabels instance.
     */
    public GraphLabels(final GraphLabels graphLabels) {
        labels = new ArrayList<>();
        graphLabels.labels.forEach(label -> labels.add(new GraphLabel(label)));
    }

    public GraphLabels(final GraphLabels graphLabels, final List<GraphLabel> additionalLabels) {
        final List<GraphLabel> allLabels = new ArrayList<>();
        graphLabels.labels.forEach(label -> allLabels.add(new GraphLabel(label)));
        additionalLabels.forEach(label -> allLabels.add(label));
        labels = allLabels.size() > MAX_LABELS ? allLabels.subList(0, MAX_LABELS) : allLabels;
    }

    public GraphLabels(final List<GraphLabel> labels) {
        this.labels = labels == null ? Collections.emptyList()
                : labels.size() > MAX_LABELS ? labels.subList(0, MAX_LABELS)
                : labels;
    }

    public List<GraphLabel> getLabels() {
        return Collections.unmodifiableList(labels);
    }

    public int getNumberOfLabels() {
        return labels.size();
    }

    @Override
    public String toString() {
        return StringUtilities.escape(labels.stream().map(label -> label.toString()).collect(Collectors.toList()), DELIMITER);
    }

    public static GraphLabels valueOf(final String graphLabelsString) {
        if (StringUtils.isBlank(graphLabelsString)) {
            return NO_LABELS;
        }

        final List<GraphLabel> labels = new ArrayList<>();
        final List<String> labelStrings;
        try {
            labelStrings = StringUtilities.splitEscaped(graphLabelsString, DELIMITER);
            labelStrings.forEach(label -> labels.add(GraphLabel.valueOf(label)));
            } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("String does not represent a graph label: " + graphLabelsString + "\nCaused by: " + ex.getMessage());
        }
        return new GraphLabels(labels);
    }
}
