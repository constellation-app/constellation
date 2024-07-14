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
package au.gov.asd.tac.constellation.views.mapview.utilities;

import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;

/**
 * A state object for markers in the Map View.
 *
 * @author cygnus_x-1
 */
public class MarkerState {

    public enum MarkerLabel {
        DEFAULT("No Labels", null, null),
        LABEL("Use Label Attribute", VisualConcept.VertexAttribute.LABEL, VisualConcept.TransactionAttribute.LABEL),
        IDENTIFIER("Use Identifier Attribute", VisualConcept.VertexAttribute.IDENTIFIER, VisualConcept.TransactionAttribute.IDENTIFIER);

        private final String displayName;
        private final SchemaAttribute vertexAttribute;
        private final SchemaAttribute transactionAttribute;

        private MarkerLabel(final String displayName,
                final SchemaAttribute vertexAttribute,
                final SchemaAttribute transactionAttribute) {
            this.displayName = displayName;
            this.vertexAttribute = vertexAttribute;
            this.transactionAttribute = transactionAttribute;
        }

        public String getDisplayName() {
            return displayName;
        }

        public SchemaAttribute getVertexAttribute() {
            return vertexAttribute;
        }

        public SchemaAttribute getTransactionAttribute() {
            return transactionAttribute;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum MarkerColorScheme {
        DEFAULT("Default Colors", null, null),
        COLOR("Use Color Attribute", VisualConcept.VertexAttribute.COLOR, VisualConcept.TransactionAttribute.COLOR),
        OVERLAY("Use Overlay Color", VisualConcept.VertexAttribute.OVERLAY_COLOR, VisualConcept.TransactionAttribute.OVERLAY_COLOR),
        BLAZE("Use Blaze Color", VisualConcept.VertexAttribute.BLAZE, null);

        private final String displayName;
        private final SchemaAttribute vertexAttribute;
        private final SchemaAttribute transactionAttribute;

        private MarkerColorScheme(final String displayName,
                final SchemaAttribute vertexAttribute,
                final SchemaAttribute transactionAttribute) {
            this.displayName = displayName;
            this.vertexAttribute = vertexAttribute;
            this.transactionAttribute = transactionAttribute;
        }

        public String getDisplayName() {
            return displayName;
        }

        public SchemaAttribute getVertexAttribute() {
            return vertexAttribute;
        }

        public SchemaAttribute getTransactionAttribute() {
            return transactionAttribute;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private MarkerLabel labelAttribute;
    private MarkerColorScheme colorScheme;
    private boolean showPointMarkers;
    private boolean showLineMarkers;
    private boolean showPolygonMarkers;
    private boolean showMultiMarkers;
    private boolean showClusterMarkers;
    private boolean showSelectedOnly;

    public MarkerState() {
        reset();
    }

    public final void reset() {
        labelAttribute = MarkerLabel.DEFAULT;
        colorScheme = MarkerColorScheme.DEFAULT;
        showPointMarkers = true;
        showLineMarkers = true;
        showPolygonMarkers = true;
        showMultiMarkers = true;
        showClusterMarkers = false;
        showSelectedOnly = false;
    }

    public final MarkerLabel getLabel() {
        return labelAttribute;
    }

    public final void setLabel(final MarkerLabel labelAttribute) {
        this.labelAttribute = labelAttribute;
    }

    public final MarkerColorScheme getColorScheme() {
        return colorScheme;
    }

    public final void setColorScheme(final MarkerColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    public final boolean isShowPointMarkers() {
        return showPointMarkers;
    }

    public final void setShowPointMarkers(final boolean showPointMarkers) {
        this.showPointMarkers = showPointMarkers;
    }

    public final boolean isShowLineMarkers() {
        return showLineMarkers;
    }

    public final void setShowLineMarkers(final boolean showLineMarkers) {
        this.showLineMarkers = showLineMarkers;
    }

    public final boolean isShowPolygonMarkers() {
        return showPolygonMarkers;
    }

    public final void setShowPolygonMarkers(final boolean showPolygonMarkers) {
        this.showPolygonMarkers = showPolygonMarkers;
    }

    public final boolean isShowMultiMarkers() {
        return showMultiMarkers;
    }

    public final void setShowMultiMarkers(final boolean showMultiMarkers) {
        this.showMultiMarkers = showMultiMarkers;
    }

    public final boolean isShowClusterMarkers() {
        return showClusterMarkers;
    }

    public final void setShowClusterMarkers(final boolean showClusterMarkers) {
        this.showClusterMarkers = showClusterMarkers;
    }

    public final boolean isShowSelectedOnly() {
        return showSelectedOnly;
    }

    public final void setShowSelectedOnly(final boolean showSelectedOnly) {
        this.showSelectedOnly = showSelectedOnly;
    }
}
