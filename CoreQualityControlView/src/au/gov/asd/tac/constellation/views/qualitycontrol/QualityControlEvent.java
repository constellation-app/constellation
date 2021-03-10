/*
 * Copyright 2010-2020 Australian Signals Directorate
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
import org.apache.commons.lang3.StringUtils;

/**
 * QualityControlEvent for estimating the quality of the data encompassed by a
 * vertex.
 *
 * @author cygnus_x-1
 */
public class QualityControlEvent implements Comparable<QualityControlEvent> {

    public enum QualityCategory {
        DEFAULT,
        INFO,
        WARNING,
        SEVERE,
        FATAL
    }
    public static final int DEFAULT_VALUE = 1;
    public static final int INFO_VALUE = 30;
    public static final int WARNING_VALUE = 60;
    public static final int SEVERE_VALUE = 90;
    public static final int FATAL_VALUE = 95;

    private int quality = 0;
    private final int vertex;
    private final String identifier;
    private final String type;
    private final List<QualityControlRule> rules;
    private final List<String> reasons;
    private QualityCategory category = QualityCategory.DEFAULT;

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
        updateEvent(List.copyOf(rules));
    }

    /**
     * Of the given rules, find all for which the vertex might be considered to
     * have low quality, as well as the lowest quality associated with these
     * rules. Stores the category which is the new highest priority as the event
     * basis.
     *
     * @param rules the list of rules to consider.
     * @return a list of rules that are relevant.
     */
    private void updateEvent(final List<QualityControlRule> rules) {
        for (final QualityControlRule rule : rules) {
            if (rule.getResults().contains(vertex)) {
                reasons.add(rule.getName());
                if (QualityControlRule.testPriority(QualityControlViewPane.getPriorities().get(rule), category) < 0) {
                    category = QualityControlViewPane.getPriorities().get(rule);

                    switch (category) {
                        case DEFAULT:
                            quality = DEFAULT_VALUE;
                            break;
                        case INFO:
                            quality = INFO_VALUE;
                            break;
                        case WARNING:
                            quality = WARNING_VALUE;
                            break;
                        case SEVERE:
                            quality = SEVERE_VALUE;
                            break;
                        case FATAL:
                            quality = FATAL_VALUE;
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    /**
     * Grabs the QualityCategory represented by the string value
     *
     * @param category the String representation of the category
     * @return QualityCategory enum result
     */
    public static QualityCategory getCategoryFromString(final String category) {
        if (StringUtils.isNotEmpty(category)) {
            switch (category.toLowerCase()) {
                case "default":
                    return QualityCategory.DEFAULT;
                case "info":
                    return QualityCategory.INFO;
                case "warning":
                    return QualityCategory.WARNING;
                case "severe":
                    return QualityCategory.SEVERE;
                case "fatal":
                    return QualityCategory.FATAL;
                default:
                    // default to default case when not readable.
                    return QualityCategory.DEFAULT;
            }
        }
        return QualityCategory.DEFAULT;
    }

    /**
     * Grabs the rule loaded by lookup, if one exists with the same name.
     *
     * @param ruleName the name of the rule to return
     * @return the rule object which has the same name. Null if it doesn't exist
     */
    public static QualityControlRule getRuleByString(final String ruleName) {
        if (StringUtils.isNotEmpty(ruleName)) {
            for (final QualityControlRule rule : QualityControlViewPane.getLookup().lookupAll(QualityControlRule.class)) {
                if (ruleName.equals(rule.getName())) {
                    return rule;
                }
            }
        }
        return null;
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

    /**
     * Get the QualityCategory related to the category of this
     * QualityControlEvent.
     *
     * @return the QualityCategory of the quality associated with this
     * QualityControlEvent.
     */
    public QualityCategory getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return identifier + "<" + type + ">, " + quality + ", " + reasons.toString();
    }

    @Override
    public int compareTo(final QualityControlEvent o) {
        return QualityControlRule.testPriority(o.getCategory(), category);
    }
}
