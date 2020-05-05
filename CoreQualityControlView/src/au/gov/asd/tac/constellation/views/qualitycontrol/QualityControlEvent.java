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
package au.gov.asd.tac.constellation.views.qualitycontrol;

import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.QualityControlRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * QualityControlEvent for estimating the quality of the data encompassed by a
 * vertex.
 *
 * @author cygnus_x-1
 */
public class QualityControlEvent implements Comparable<QualityControlEvent> {

    private int quality = 0;
    private final int vertex;
    private final String identifier;
    private final String type;
    private final List<QualityControlRule> rules;
    private final List<String> reasons;

    /**
     * Constructor for QualityControlEvent.
     *
     * @param vertex An Integer representing a vertex in a Graph.
     * @param identifier The Identifier attribute of the vertex as a String
     * @param type The Type attribute of the vertex as a VType
     * @param rules the list of rules that apply to this event.
     */
    public QualityControlEvent(final int vertex, final String identifier, final SchemaVertexType type, final List<QualityControlRule> rules) {
        this.vertex = vertex;
        this.identifier = identifier;
        this.type = type != null ? type.getName() : null;
        this.rules = rules;
        reasons = new ArrayList<>();
        updateEvent(rules);
    }

    /**
     * Of the given rules, find all for which the vertex might be considered to
     * have low quality, as well as the lowest quality associated with these
     * rules.
     *
     * @param rules the list of rules to consider.
     * @return a list of rules that are relevant.
     */
    private List<String> updateEvent(final List<QualityControlRule> rules) {
        for (final QualityControlRule rule : rules) {
            if (rule.getResults().contains(vertex)) {
                reasons.add(rule.getName());
                if (rule.getQuality(vertex) > quality) {
                    quality = rule.getQuality(vertex);
                }
            }
        }

        return Collections.unmodifiableList(reasons);
    }

    /**
     * Get the vertex referred to by this QualityControlEvent as an Integer.
     *
     * @return the vertex referred to by this QualityControlEvent as an Integer.
     */
    public int getVertex() {
        return vertex;
    }

    /**
     * Get the String value of the identifier associated with this
     * QualityControlEvent.
     *
     * @return the String value of the identifier associated with this
     * QualityControlEvent.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get the String value of the type associated with this
     * QualityControlEvent.
     *
     * @return the String value of the type associated with this
     * QualityControlEvent.
     */
    public String getType() {
        return type;
    }

    /**
     * The rules associated with this QualityControlEvent.
     *
     * @return The rules associated with this QualityControlEvent.
     */
    public List<QualityControlRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    /**
     * Get the String value of the reasons associated with this
     * QualityControlEvent.
     *
     * @return the String value of the reasons associated with this
     * QualityControlEvent.
     */
    public String getReasons() {
        final StringBuilder buf = new StringBuilder();
        for (final String reason : reasons) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(reason);
        }

        return buf.toString();
    }

    /**
     * Get the Integer value of the quality associated with this
     * QualityControlEvent.
     *
     * @return the Integer value of the quality associated with this
     * QualityControlEvent.
     */
    public int getQuality() {
        return quality;
    }

    @Override
    public String toString() {
        return identifier + "<" + type + ">, " + quality + ", " + reasons.toString();
    }

    @Override
    public int compareTo(final QualityControlEvent o) {
        return Integer.compare(quality, o.quality);
    }
}
