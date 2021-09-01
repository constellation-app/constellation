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
        // Get the current directory and make the file within the module.
        final String userDir = System.getProperty("user.dir");
        final String sep = File.separator;
        final String analyticViewPath = userDir + sep + ".." + sep + "CoreAnalyticView" + sep + "src" + sep + "au" + sep
                + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "views" + sep + "analyticview";

        final File dir = new File(analyticViewPath);
        final String[] extensions = new String[]{"md"};
        final List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
        final List<String> filePaths = new ArrayList<>();
        for (final File file : files) {
            filePaths.add(file.getPath());
        }
        return filePaths;
    }

    @Override
    public List<String> getHelpResources() {
        // Get the current directory and make the file within the module.
        final String userDir = System.getProperty("user.dir");
        final String sep = File.separator;
        final String analyticViewPath = userDir + sep + ".." + sep + "CoreAnalyticView" + sep + "src" + sep + "au" + sep
                + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "views" + sep + "analyticview";

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
        map.put("au.gov.asd.tac.constellation.views.analyticview.AnalyticViewPane", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-view.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.BestConnectsNetworkQuestion", "au.gov.asd.tac.constellation.views.analyticview.docs.question-best-connects-network.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.HasClosestRelationshipQuestion", "au.gov.asd.tac.constellation.views.analyticview.docs.question-has-closest-relationship.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.MostCentralQuestion", "au.gov.asd.tac.constellation.views.analyticview.docs.question-most-central.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.MostCommunicantsQuestion", "au.gov.asd.tac.constellation.views.analyticview.docs.question-most-communicants.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.MostEasilyReachedQuestion", "au.gov.asd.tac.constellation.views.analyticview.docs.question-most-easily-reached.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.MostInfluentialQuestion", "au.gov.asd.tac.constellation.views.analyticview.docs.question-most-influential.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.questions.MostLikelyCorrelatedQuestion", "au.gov.asd.tac.constellation.views.analyticview.docs.question-most-likely-correlated.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.AdamicAdarIndexAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-adamic-adar-index.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.AverageDegreeAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-average-degree.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.BetweennessCentralityAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-betweenness-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.ClosenessCentralityAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-closeness-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.CommonNeighboursAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-common-neighbours.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.ComponentCountAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-component-count.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.ConnectivityDegreeAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-connectivity-degree.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.CosineSimilarityAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-cosine-similarity.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.DegreeCentralityAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-degree-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.DiceSimilarityAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-dice-similarity.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.EccentricityAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-eccentricity.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.EffectiveResistanceAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-effective-resistance.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.EigenvectorCentralityAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-eigenvector-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.GlobalClusteringCoefficientAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-global-clustering-coefficient.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analyticsGraphDensityAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-graph-density.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.GraphDistanceAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-graph-distance.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.HitsCentralityAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-hits-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.JaccardIndexAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-jaccard-index.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.KatzCentralityAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-katz-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.LevenshteinDistanceAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-levenshtein-distance.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.LocalClusteringCoefficientAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-local-clustering-coefficient.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.MultiplexityAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-multiplexity.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.PagerankCentralityAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-pagerank-centrality.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.PreferentialAttachmentAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-preferential-attachment.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.RatioOfReciprocityAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-ratio-of-reciprocity.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.ResourceAllocationIndexAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-resource-allocation-index.md");
        map.put("au.gov.asd.tac.constellation.views.analyticview.analytics.WeightAnalytic", "au.gov.asd.tac.constellation.views.analyticview.docs.analytic-weight.md");

        return map;
    }

    @Override
    public String getHelpTOC() {
        final String userDir = System.getProperty("user.dir");
        final String sep = File.separator;
        final int count = userDir.length() - 13;
        final String substr = userDir.substring(count);
        final String analyticViewPath;
        if ("constellation".equals(substr)) {
            analyticViewPath = userDir + sep + "CoreAnalyticView" + sep + "src" + sep + "au" + sep
                    + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "views" + sep + "analyticview" + sep + "docs" + sep + "analyticview-toc.xml";

        } else {
            analyticViewPath = userDir + sep + ".." + sep + "CoreAnalyticView" + sep + "src" + sep + "au" + sep
                    + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "views" + sep + "analyticview" + sep + "docs" + sep + "analyticview-toc.xml";
        }

        return analyticViewPath;
    }
}
