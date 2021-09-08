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
package au.gov.asd.tac.constellation.views.analyticview;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the analytic view
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class)
@NbBundle.Messages("AnalyticViewHelpProvider=Analytic View Help Provider")
public class AnalyticViewHelpProvider extends HelpPageProvider {

    @Override
    public List<String> getHelpPages() {
        final List<String> filePaths = new ArrayList<>();
        return filePaths;
    }

    @Override
    public List<String> getHelpResources() {
        // Get the current directory and make the file within the module.
        final String userDir = System.getProperty("user.dir");
        final String sep = File.separator;
        final int count = userDir.length() - 13;
        final String substr = userDir.substring(count);
        final String analyticViewPath;
        if ("constellation".equals(substr)) {
            analyticViewPath = userDir + sep + "CoreAnalyticView" + sep + "src" + sep + "au" + sep
                    + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "views" + sep + "analyticview";
        } else {
            analyticViewPath = userDir + sep + ".." + sep + "CoreAnalyticView" + sep + "src" + sep + "au" + sep
                    + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "views" + sep + "analyticview";
        }
        final File dir = new File(analyticViewPath);
        final String[] extensions = new String[]{"png"};
        final List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
        final List<String> filePaths = new ArrayList<>();
        for (final File file : files) {
            filePaths.add(file.getPath());
        }
        return filePaths;
    }

    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();
        final String userDir = System.getProperty("user.dir");
        final String sep = File.separator;
        final int count = userDir.length() - 13;
        final String substr = userDir.substring(count);
        final String analyticModulePath = ".." + sep + "constellation" + sep + "CoreAnalyticView" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "views" + sep + "analyticview" + sep + "docs" + sep;

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
        map.put("au.gov.asd.tac.constellation.views.analyticview.analyticsGraphDensityAnalytic", analyticModulePath + "analytic-graph-density.md");
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

    @Override
    public String getHelpTOC() {
        final String sep = File.separator;
        final String analyticViewPath = "constellation" + sep + "CoreAnalyticView" + sep + "src" + sep + "au" + sep
                + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "views" + sep + "analyticview" + sep + "docs" + sep + "analyticview-toc.xml";

        return analyticViewPath;
    }
}
