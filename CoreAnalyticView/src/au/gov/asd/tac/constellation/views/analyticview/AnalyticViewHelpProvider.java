/*
 * Copyright 2010-2023 Australian Signals Directorate
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
import java.io.File;
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

    private static final String CODEBASE_NAME = "constellation";
    private static final String SEP = File.separator;

    /**
     * Provides a map of all the help files Maps the file name to the md file name
     *
     * @return Map of the file names vs md file names
     */
    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();
        final String analyticModulePath = ".." + SEP + "ext" + SEP + "docs" + SEP + "CoreAnalyticView" + SEP + "src" + SEP + "au" + SEP + "gov" + SEP
                + "asd" + SEP + "tac" + SEP + CODEBASE_NAME + SEP + "views" + SEP + "analyticview" + SEP;

        map.put("au.gov.asd.tac.constellation.views.analyticview.AnalyticViewPane", analyticModulePath + "analytic-view.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.BestConnectsNetworkQuestion", analyticModulePath + "question-best-connects-network.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.HasClosestRelationshipQuestion", analyticModulePath + "question-has-closest-relationship.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.MostCentralQuestion", analyticModulePath + "question-most-central.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.MostCommunicantsQuestion", analyticModulePath + "question-most-communicants.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.MostEasilyReachedQuestion", analyticModulePath + "question-most-easily-reached.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.MostInfluentialQuestion", analyticModulePath + "question-most-influential.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.MostLikelyCorrelatedQuestion", analyticModulePath + "question-most-likely-correlated.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.AdamicAdarIndexAnalytic", analyticModulePath + "analytic-adamic-adar-index.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.AverageDegreeAnalytic", analyticModulePath + "analytic-average-degree.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.BetweennessCentralityAnalytic", analyticModulePath + "analytic-betweenness-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.ClosenessCentralityAnalytic", analyticModulePath + "analytic-closeness-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.CommonNeighboursAnalytic", analyticModulePath + "analytic-common-neighbours.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.ComponentCountAnalytic", analyticModulePath + "analytic-component-count.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.ConnectivityDegreeAnalytic", analyticModulePath + "analytic-connectivity-degree.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.CosineSimilarityAnalytic", analyticModulePath + "analytic-cosine-similarity.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.DegreeCentralityAnalytic", analyticModulePath + "analytic-degree-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.DiceSimilarityAnalytic", analyticModulePath + "analytic-dice-similarity.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.EccentricityAnalytic", analyticModulePath + "analytic-eccentricity.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.EffectiveResistanceAnalytic", analyticModulePath + "analytic-effective-resistance.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.EigenvectorCentralityAnalytic", analyticModulePath + "analytic-eigenvector-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.GlobalClusteringCoefficientAnalytic", analyticModulePath + "analytic-global-clustering-coefficient.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.GraphDensityAnalytic", analyticModulePath + "analytic-graph-density.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.GraphDistanceAnalytic", analyticModulePath + "analytic-graph-distance.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.HitsCentralityAnalytic", analyticModulePath + "analytic-hits-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.JaccardIndexAnalytic", analyticModulePath + "analytic-jaccard-index.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.KatzCentralityAnalytic", analyticModulePath + "analytic-katz-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.LevenshteinDistanceAnalytic", analyticModulePath + "analytic-levenshtein-distance.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.LocalClusteringCoefficientAnalytic", analyticModulePath + "analytic-local-clustering-coefficient.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.MultiplexityAnalytic", analyticModulePath + "analytic-multiplexity.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.PagerankCentralityAnalytic", analyticModulePath + "analytic-pagerank-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.PreferentialAttachmentAnalytic", analyticModulePath + "analytic-preferential-attachment.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.RatioOfReciprocityAnalytic", analyticModulePath + "analytic-ratio-of-reciprocity.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.ResourceAllocationIndexAnalytic", analyticModulePath + "analytic-resource-allocation-index.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.WeightAnalytic", analyticModulePath + "analytic-weight.md");
        return map;
    }

    /**
     * Provides a location as a string of the TOC xml file in the module
     *
     * @return List of help resources
     */
    @Override
    public String getHelpTOC() {
        final String analyticViewPath;
        analyticViewPath = "ext" + SEP + "docs" + SEP + "CoreAnalyticView" + SEP + "src" + SEP + "au" + SEP + "gov" + SEP + "asd" + SEP
                + "tac" + SEP + CODEBASE_NAME + SEP + "views" + SEP + "analyticview" + SEP + "analyticview-toc.xml";

        return analyticViewPath;
    }
}
