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
package au.gov.asd.tac.constellation.views.analyticview;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the analytic view
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class, position = 700)
@NbBundle.Messages("AnalyticViewHelpProvider=Analytic View Help Provider")
public class AnalyticViewHelpProvider extends HelpPageProvider {
    
    private static final String MODULE_PATH = "ext" + SEP + "docs" + SEP + "CoreAnalyticView" + SEP;

    /**
     * Provides a map of all the help files Maps the file name to the md file name
     *
     * @return Map of the file names vs md file names
     */
    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();

        map.put("au.gov.asd.tac.constellation.views.analyticview.AnalyticViewPane", MODULE_PATH + "analytic-view.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.BestConnectsNetworkQuestion", MODULE_PATH + "question-best-connects-network.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.HasClosestRelationshipQuestion", MODULE_PATH + "question-has-closest-relationship.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.MostCentralQuestion", MODULE_PATH + "question-most-central.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.MostCommunicantsQuestion", MODULE_PATH + "question-most-communicants.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.MostEasilyReachedQuestion", MODULE_PATH + "question-most-easily-reached.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.MostInfluentialQuestion", MODULE_PATH + "question-most-influential.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.MostLikelyCorrelatedQuestion", MODULE_PATH + "question-most-likely-correlated.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.AdamicAdarIndexAnalytic", MODULE_PATH + "analytic-adamic-adar-index.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.AverageDegreeAnalytic", MODULE_PATH + "analytic-average-degree.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.BetweennessCentralityAnalytic", MODULE_PATH + "analytic-betweenness-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.ClosenessCentralityAnalytic", MODULE_PATH + "analytic-closeness-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.CommonNeighboursAnalytic", MODULE_PATH + "analytic-common-neighbours.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.ComponentCountAnalytic", MODULE_PATH + "analytic-component-count.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.ConnectivityDegreeAnalytic", MODULE_PATH + "analytic-connectivity-degree.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.CosineSimilarityAnalytic", MODULE_PATH + "analytic-cosine-similarity.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.DegreeCentralityAnalytic", MODULE_PATH + "analytic-degree-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.DiceSimilarityAnalytic", MODULE_PATH + "analytic-dice-similarity.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.EccentricityAnalytic", MODULE_PATH + "analytic-eccentricity.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.EffectiveResistanceAnalytic", MODULE_PATH + "analytic-effective-resistance.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.EigenvectorCentralityAnalytic", MODULE_PATH + "analytic-eigenvector-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.GlobalClusteringCoefficientAnalytic", MODULE_PATH + "analytic-global-clustering-coefficient.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.GraphDensityAnalytic", MODULE_PATH + "analytic-graph-density.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.GraphDistanceAnalytic", MODULE_PATH + "analytic-graph-distance.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.HitsCentralityAnalytic", MODULE_PATH + "analytic-hits-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.JaccardIndexAnalytic", MODULE_PATH + "analytic-jaccard-index.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.KatzCentralityAnalytic", MODULE_PATH + "analytic-katz-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.LevenshteinDistanceAnalytic", MODULE_PATH + "analytic-levenshtein-distance.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.LocalClusteringCoefficientAnalytic", MODULE_PATH + "analytic-local-clustering-coefficient.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.MultiplexityAnalytic", MODULE_PATH + "analytic-multiplexity.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.PagerankCentralityAnalytic", MODULE_PATH + "analytic-pagerank-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.PreferentialAttachmentAnalytic", MODULE_PATH + "analytic-preferential-attachment.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.RatioOfReciprocityAnalytic", MODULE_PATH + "analytic-ratio-of-reciprocity.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.ResourceAllocationIndexAnalytic", MODULE_PATH + "analytic-resource-allocation-index.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.WeightAnalytic", MODULE_PATH + "analytic-weight.md");
        return map;
    }

    /**
     * Provides a location as a string of the TOC xml file in the module
     *
     * @return List of help resources
     */
    @Override
    public String getHelpTOC() {
        return MODULE_PATH + "analyticview-toc.xml";
    }
}
