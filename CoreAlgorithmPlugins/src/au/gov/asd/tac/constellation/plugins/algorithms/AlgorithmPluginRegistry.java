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
package au.gov.asd.tac.constellation.plugins.algorithms;

import au.gov.asd.tac.constellation.plugins.algorithms.clustering.chinesewhispers.ChineseWhispersPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.InfoMapPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.paths.DirectedShortestPathsPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.paths.ShortestPathsPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.BetweennessCentralityPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.ClosenessCentralityPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.DegreeCentralityPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.EigenvectorCentralityPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.HitsCentralityPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.KatzCentralityPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.PagerankCentralityPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.global.AverageDegreePlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.global.ComponentCountPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.global.GlobalClusteringCoefficientPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.global.GraphDensityPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.global.GraphDistancePlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.metrics.ConnectivityDegreePlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.metrics.EccentricityPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.metrics.EffectiveResistancePlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.metrics.LocalClusteringCoefficientPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.metrics.MultiplexityPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.metrics.RatioOfReciprocityPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.metrics.WeightPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity.AdamicAdarIndexPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity.CommonNeighboursPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity.CosineSimilarityPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity.DiceSimilarityPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity.JaccardIndexPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity.LevenshteinDistancePlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity.PreferentialAttachmentPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity.ResourceAllocationIndexPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.tree.SpanningTreePlugin;

/**
 * Registry of Algorithmic plugins.
 *
 * @author cygnus_x-1
 */
public final class AlgorithmPluginRegistry {

    // centrality
    public static final String BETWEENNESS_CENTRALITY = BetweennessCentralityPlugin.class.getName();
    public static final String CLOSENESS_CENTRALITY = ClosenessCentralityPlugin.class.getName();
    public static final String DEGREE_CENTRALITY = DegreeCentralityPlugin.class.getName();
    public static final String EIGENVECTOR_CENTRALITY = EigenvectorCentralityPlugin.class.getName();
    public static final String HITS_CENTRALITY = HitsCentralityPlugin.class.getName();
    public static final String KATZ_CENTRALITY = KatzCentralityPlugin.class.getName();
    public static final String PAGERANK_CENTRALITY = PagerankCentralityPlugin.class.getName();

    // clustering
    public static final String CLUSTER_CHINESE_WHISPERS = ChineseWhispersPlugin.class.getName();
    public static final String CLUSTER_INFO_MAP = InfoMapPlugin.class.getName();

    // global
    public static final String AVERAGE_DEGREE = AverageDegreePlugin.class.getName();
    public static final String COMPONENT_COUNT = ComponentCountPlugin.class.getName();
    public static final String GLOBAL_CLUSTERING_COEFFICIENT = GlobalClusteringCoefficientPlugin.class.getName();
    public static final String GRAPH_DENSITY = GraphDensityPlugin.class.getName();
    public static final String GRAPH_DISTANCE = GraphDistancePlugin.class.getName();

    // metrics
    public static final String CONNECTIVITY_DEGREE = ConnectivityDegreePlugin.class.getName();
    public static final String ECCENTRICITY = EccentricityPlugin.class.getName();
    public static final String EFFECTIVE_RESISTANCE = EffectiveResistancePlugin.class.getName();
    public static final String LOCAL_CLUSTERING_COEFFICIENT = LocalClusteringCoefficientPlugin.class.getName();
    public static final String MULTIPLEXITY = MultiplexityPlugin.class.getName();
    public static final String RATIO_OF_RECIPROCITY = RatioOfReciprocityPlugin.class.getName();
    public static final String WEIGHT = WeightPlugin.class.getName();

    // paths
    public static final String SHORTEST_PATHS = ShortestPathsPlugin.class.getName();
    public static final String DIRECTED_SHORTEST_PATHS = DirectedShortestPathsPlugin.class.getName();

    // similarity
    public static final String COSINE_SIMILARITY = CosineSimilarityPlugin.class.getName();
    public static final String DICE_SIMILARITY = DiceSimilarityPlugin.class.getName();
    public static final String JACCARD_INDEX = JaccardIndexPlugin.class.getName();
    public static final String COMMON_NEIGHBOURS = CommonNeighboursPlugin.class.getName();
    public static final String RESOURCE_ALLOCATION_INDEX = ResourceAllocationIndexPlugin.class.getName();
    public static final String ADAMIC_ADAR_INDEX = AdamicAdarIndexPlugin.class.getName();
    public static final String PREFERENTIAL_ATTACHMENT = PreferentialAttachmentPlugin.class.getName();
    public static final String LEVENSHTEIN_DISTANCE = LevenshteinDistancePlugin.class.getName();

    // tree
    public static final String SPANNING_TREE = SpanningTreePlugin.class.getName();
}
