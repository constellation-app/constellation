/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.aiagent.tools;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.json.JsonFactoryUtilities;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the graph tools against a small in-memory graph.
 */
public class GraphToolsNGTest {

    private Graph graph;
    private final List<Integer> vertices = new ArrayList<>();
    private final ToolContext context = new ToolContext() {
        @Override
        public Graph getActiveGraph() {
            return graph;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void progress(final String message) {
            // not needed
        }

        @Override
        public int getMaxResultChars() {
            return 10000;
        }
    };

    @BeforeMethod
    public void setUp() throws InterruptedException {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        vertices.clear();
        final WritableGraph wg = graph.getWritableGraph("test", true);
        try {
            final int label = VisualConcept.VertexAttribute.LABEL.ensure(wg);
            final int type = AnalyticConcept.VertexAttribute.TYPE.ensure(wg);
            final int selected = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            final int score = wg.addAttribute(GraphElementType.VERTEX, "float", "Centrality.OutBetweenness", "score", 0F, null);
            final String[] names = {"alice", "bob", "carol", "dave", "erin"};
            final String[] types = {"Person", "Person", "Email", "Person", "Email"};
            final float[] scores = {0.5F, 2F, 10F, 0F, 1F};
            for (int i = 0; i < names.length; i++) {
                final int vertex = wg.addVertex();
                wg.setStringValue(label, vertex, names[i]);
                wg.setStringValue(type, vertex, types[i]);
                wg.setFloatValue(score, vertex, scores[i]);
                wg.setBooleanValue(selected, vertex, i == 0);
                vertices.add(vertex);
            }
            VisualConcept.TransactionAttribute.SELECTED.ensure(wg);
            wg.addTransaction(vertices.get(0), vertices.get(1), true);
            wg.addTransaction(vertices.get(1), vertices.get(2), true);
        } finally {
            wg.commit();
        }
    }

    private static JsonNode args(final String json) throws Exception {
        return JsonFactoryUtilities.getObjectMapper().readTree(json);
    }

    private static JsonNode parse(final ToolResult result) throws Exception {
        assertFalse(result.error(), result.text());
        return JsonFactoryUtilities.getObjectMapper().readTree(result.text());
    }

    @Test
    public void testSummary() throws Exception {
        final JsonNode summary = parse(new GraphSummaryTool().execute(args("{}"), context));
        assertEquals(summary.get("vertex_count").asInt(), 5);
        assertEquals(summary.get("transaction_count").asInt(), 2);
        assertEquals(summary.get("selected_vertices").asInt(), 1);
        assertEquals(summary.get("top_vertex_types").get("Person").asInt(), 3);
    }

    @Test
    public void testQueryFilters() throws Exception {
        final JsonNode result = parse(new QueryElementsTool().execute(
                args("{\"filters\":[{\"attribute\":\"Type\",\"op\":\"equals\",\"value\":\"email\"}],\"attributes\":[\"Label\"]}"), context));
        assertEquals(result.get("total_matches").asInt(), 2);
        assertEquals(result.get("rows").get(0).get("Label").asText(), "carol");
        assertTrue(result.get("rows").get(0).has("id"));
    }

    @Test
    public void testQueryTopNSortsDescendingAndIncludesTheScore() throws Exception {
        final JsonNode result = parse(new QueryElementsTool().execute(args("{\"sort_by\":\"Centrality.OutBetweenness\",\"limit\":2}"), context));
        final JsonNode rows = result.get("rows");
        assertEquals(rows.size(), 2);
        assertEquals(rows.get(0).get("Label").asText(), "carol");
        assertEquals(rows.get(1).get("Label").asText(), "bob");
        assertEquals(rows.get(0).get("Centrality.OutBetweenness").asDouble(), 10.0);
        assertTrue(rows.get(0).get("Centrality.OutBetweenness").isNumber());
        assertEquals(result.get("next_offset").asInt(), 2);
    }

    @Test
    public void testQueryAscendingAndAbbreviatedAttribute() throws Exception {
        final JsonNode result = parse(new QueryElementsTool().execute(
                args("{\"sort_by\":\"centrality.outbetween\",\"descending\":false,\"limit\":1}"), context));
        assertEquals(result.get("rows").get(0).get("Label").asText(), "dave");
    }

    @Test
    public void testQueryTransactionsIncludeEndpoints() throws Exception {
        final JsonNode result = parse(new QueryElementsTool().execute(args("{\"element_type\":\"transaction\"}"), context));
        assertEquals(result.get("total_matches").asInt(), 2);
        assertEquals(result.get("rows").get(0).get("source").asText(), "alice");
        assertEquals(result.get("rows").get(0).get("destination").asText(), "bob");
    }

    @Test
    public void testUnknownAttributeListsAlternatives() {
        final IllegalArgumentException ex = expectThrows(IllegalArgumentException.class,
                () -> new QueryElementsTool().execute(args("{\"sort_by\":\"Nope\"}"), context));
        assertTrue(ex.getMessage().contains("Available attributes"), ex.getMessage());
    }

    @Test
    public void testSelection() throws Exception {
        final JsonNode selection = parse(new GetSelectionTool().execute(args("{}"), context));
        assertEquals(selection.get("vertices").get("total_matches").asInt(), 1);
        assertEquals(selection.get("vertices").get("rows").get(0).get("Label").asText(), "alice");
    }

    @Test
    public void testSelectElements() throws Exception {
        final SelectElementsTool tool = new SelectElementsTool();
        JsonNode result = parse(tool.execute(args("{\"ids\":[" + vertices.get(1) + "," + vertices.get(2) + "]}"), context));
        assertEquals(result.get("selected_before").asInt(), 1);
        assertEquals(result.get("selected_after").asInt(), 2);
        assertEquals(selectedLabels(), List.of("bob", "carol"));

        result = parse(tool.execute(args("{\"mode\":\"add\",\"filters\":[{\"attribute\":\"Label\",\"op\":\"starts_with\",\"value\":\"e\"}]}"), context));
        assertEquals(result.get("selected_after").asInt(), 3);

        tool.execute(args("{\"mode\":\"remove\",\"ids\":[" + vertices.get(1) + "]}"), context);
        assertEquals(selectedLabels(), List.of("carol", "erin"));

        // ids and filters together: the filters narrow the ids
        tool.execute(args("{\"ids\":[" + vertices.get(0) + "," + vertices.get(1) + "],"
                + "\"filters\":[{\"attribute\":\"Label\",\"op\":\"not_equals\",\"value\":\"null\"}]}"), context);
        assertEquals(selectedLabels(), List.of("alice", "bob"));
        tool.execute(args("{\"ids\":[" + vertices.get(0) + "," + vertices.get(1) + "],"
                + "\"filters\":[{\"attribute\":\"Label\",\"op\":\"equals\",\"value\":\"bob\"}]}"), context);
        assertEquals(selectedLabels(), List.of("bob"));

        tool.execute(args("{\"clear\":true}"), context);
        assertTrue(selectedLabels().isEmpty());

        assertTrue(tool.execute(args("{}"), context).error());
    }

    private List<String> selectedLabels() {
        final List<String> labels = new ArrayList<>();
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            final int selected = VisualConcept.VertexAttribute.SELECTED.get(rg);
            final int label = VisualConcept.VertexAttribute.LABEL.get(rg);
            for (final int vertex : vertices) {
                if (rg.getBooleanValue(selected, vertex)) {
                    labels.add(rg.getStringValue(label, vertex));
                }
            }
        }
        return labels;
    }

    @Test
    public void testAttributeStats() throws Exception {
        JsonNode stats = parse(new AttributeStatsTool().execute(args("{\"attribute\":\"Centrality.OutBetweenness\"}"), context));
        assertEquals(stats.get("min").asDouble(), 0.0);
        assertEquals(stats.get("max").asDouble(), 10.0);
        assertEquals(stats.get("distinct_values").asInt(), 5);
        stats = parse(new AttributeStatsTool().execute(args("{\"attribute\":\"Type\",\"top_n\":1}"), context));
        assertEquals(stats.get("top_values").get("Person").asInt(), 3);
        assertFalse(stats.has("mean"));
    }

    @Test
    public void testGetAttributes() throws Exception {
        final JsonNode attributes = parse(new GetAttributesTool().execute(args("{\"element_type\":\"vertex\"}"), context));
        assertTrue(attributes.has("vertex"));
        assertFalse(attributes.has("transaction"));
        boolean found = false;
        for (final JsonNode attribute : attributes.get("vertex")) {
            found |= "Centrality.OutBetweenness".equals(attribute.get("name").asText());
        }
        assertTrue(found);
    }

    @Test
    public void testGraphDiff() throws Exception {
        final GraphDiff.Snapshot before = GraphDiff.capture(graph);
        final WritableGraph wg = graph.getWritableGraph("change", true);
        try {
            wg.addVertex();
            wg.addAttribute(GraphElementType.VERTEX, "string", "New", null, null, null);
        } finally {
            wg.commit();
        }
        final GraphDiff.Snapshot after = GraphDiff.capture(graph);
        final JsonNode diff = GraphDiff.diff(before, after);
        assertTrue(diff.get("graph_modified").asBoolean());
        assertEquals(diff.get("vertices").asText(), "5 -> 6");
        assertEquals(diff.get("new_vertex_attributes").get(0).asText(), "New");
        assertEquals(GraphDiff.summary(before, after), "vertices 5→6; new attributes New");
    }

    @Test
    public void testNoGraph() {
        graph = null;
        final IllegalStateException ex = expectThrows(IllegalStateException.class, () -> new GraphSummaryTool().execute(args("{}"), context));
        assertEquals(ex.getMessage(), GraphSupport.NO_GRAPH);
    }
}
