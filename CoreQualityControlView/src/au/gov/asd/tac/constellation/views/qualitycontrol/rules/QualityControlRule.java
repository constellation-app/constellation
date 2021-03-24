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
package au.gov.asd.tac.constellation.views.qualitycontrol.rules;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlEvent;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlEvent.QualityCategory;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.Lookup;

/**
 * A Rule is a class defining a test to be run by the Quality Control View.
 *
 * @author cygnus_x-1
 */
public abstract class QualityControlRule {

    private static final int CATEGORY_1_HIGHER = -1;
    private static final int CATEGORY_2_HIGHER = 1;
    private static final int CATEGORIES_EQUAL = 0;

    /**
     * Test the priority between the two categories and determine which priority
     * is highest.
     *
     * @param category1 the QualityCategory to compare with category2
     * @param category2 the QualityCategory to compare with category1
     * @return an Integer as follows below.
     * <p>
     * -1 if category1 is higher priority
     * <p>
     * 0 if both categories are the same priority
     * <p>
     * 1 if category2 is higher priority
     */
    public static int testPriority(final QualityCategory category1, final QualityCategory category2) {
        // both same category
        if (category1 == category2) {
            return CATEGORIES_EQUAL;
        }
        // rule1 is never going to be higher than rule 2
        if (category1 == QualityCategory.DEFAULT) {
            return CATEGORY_2_HIGHER;
        }
        // rule2 is never going to be higher than rule 1
        if (category2 == QualityCategory.DEFAULT) {
            return CATEGORY_1_HIGHER;
        }

        // rule1 is always going to be higher than rule 2
        if (category1 == QualityCategory.FATAL) {
            return CATEGORY_1_HIGHER;
        }
        // rule2 is always going to be higher than rule 1
        if (category2 == QualityCategory.FATAL) {
            return CATEGORY_2_HIGHER;
        }

        // rule1 is info, so rule 2 cannot be default, and is not info because
        // it is not equal to rule 1.
        if (category1 == QualityCategory.INFO) {
            return CATEGORY_2_HIGHER;
        }
        // rule 1 is warning, so rule 2 could be info, severe or fatal
        if (category1 == QualityCategory.WARNING) {
            if (category2 == QualityCategory.INFO) {
                return CATEGORY_1_HIGHER;
            }
            // if not info, then it is higher than warning
            return CATEGORY_2_HIGHER;
        }
        // if severe, rule2 could be info, warning, fatal
        if (category1 == QualityCategory.SEVERE) {
            // if rule2 is fatal, it is higher priority
            if (category2 == QualityCategory.FATAL) {
                return CATEGORY_2_HIGHER;
            }
            // if not fatal, must be lower.
            return CATEGORY_1_HIGHER;
        }
        // default return false
        return CATEGORY_2_HIGHER;
    }

    protected Set<Integer> results;

    /**
     * Construct a Rule.
     */
    public QualityControlRule() {
        results = new HashSet<>();
    }

    /**
     * Get the results of the Rule, consisting of a set of vertex id's for any
     * vertices which matched this Rule.
     *
     * @return A {@link Set} or {@link Integer} objects representing the id's of
     * any vertices who match this Rule.
     */
    public Set<Integer> getResults() {
        return Collections.unmodifiableSet(results);
    }

    /**
     * Clear the results of this Rule.
     * <p>
     * Rules are found using {@link Lookup}, which caches instances of
     * registered classes, meaning the same rules will be reused. Therefore, the
     * results of any previous runs must be cleared before re-running this Rule.
     */
    public void clearResults() {
        results.clear();
    }

    /**
     * Check all specified nodes against this Rule.
     *
     * @param graph The graph to execute the rule on.
     * @param vertexIds The list of vertex ids to execute the rule on.
     */
    public void executeRule(final GraphReadMethods graph, final List<Integer> vertexIds) {
        vertexIds.forEach(vxId -> {
            if (this.executeRule(graph, vxId)) {
                results.add(vxId);
            }
        });
    }

    /**
     * Get the name of this Rule.
     *
     * @return A {@link String} representing the name of this Rule.
     */
    public abstract String getName();

    /**
     * Get a description of this rule for presentation to the user.
     *
     * @return A {@link String} description of this Rule.
     */
    public abstract String getDescription();

    /**
     * Get the quality of the given vertex as identified by this Rule, where
     * higher scores indicate lesser quality.
     *
     * @param vertexId An {@link Integer} object representing the id of a
     * vertex.
     * @return An integer value representing the quality of the specified
     * vertex.
     */
    public abstract int getQuality(final int vertexId);

    /**
     * Overridden toString() used for serializing objects
     *
     * @return the String representation of this object
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Get the QualityCategory which maps to the current score
     *
     * @param vertexId the vertex to fetch the quality for
     * @return QualityCategory for the vertex
     */
    public QualityCategory getCategory(final int vertexId) {
        return getCategoryByScore(getQuality(vertexId));
    }

    /**
     * Get the QualityCategory which maps to the int qualityScore
     *
     * @param qualityScore the int value of the rule
     * @return QualityCategory relating to the qualityScore given.
     */
    public static QualityCategory getCategoryByScore(final int qualityScore) {
        if (qualityScore >= QualityControlEvent.FATAL_VALUE) {
            return QualityCategory.FATAL;
        } else if (qualityScore >= QualityControlEvent.SEVERE_VALUE) {
            return QualityCategory.SEVERE;
        } else if (qualityScore >= QualityControlEvent.WARNING_VALUE) {
            return QualityCategory.WARNING;
        } else if (qualityScore >= QualityControlEvent.INFO_VALUE) {
            return QualityCategory.INFO;
        } else {
            return QualityCategory.DEFAULT;
        }
    }

    /**
     * Execute the logic of this Rule against a single vertex and return a
     * {@link Boolean} object representing whether the vertex passed or not.
     *
     * @param graph A {@link GraphReadMethods} on which to execute this Rule.
     * @param vertexId The id of the vertex to on which to execute this Rule.
     * @return True if the vertex matched this QualityControlRule, false
     * otherwise.
     */
    protected abstract boolean executeRule(final GraphReadMethods graph, final int vertexId);
}
