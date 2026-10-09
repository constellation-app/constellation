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
package au.gov.asd.tac.constellation.views.aiagent.ui;

import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Tests for the table handling in {@link MarkdownFxRenderer}.
 */
public class MarkdownFxRendererNGTest {

    @Test
    public void testSplitFindsTables() {
        final String markdown = """
                Intro text

                | Name | Score |
                |------|------:|
                | a    | 1     |
                | b    | 2     |

                After""";
        final List<MarkdownFxRenderer.Segment> segments = MarkdownFxRenderer.split(markdown);
        assertEquals(segments.size(), 3);
        assertFalse(segments.get(0).table());
        assertTrue(segments.get(1).table());
        assertEquals(segments.get(1).lines().size(), 4);
        assertFalse(segments.get(2).table());
    }

    @Test
    public void testTablesInCodeBlocksAreIgnored() {
        final String markdown = """
                ```
                | a | b |
                |---|---|
                ```""";
        assertTrue(MarkdownFxRenderer.split(markdown).stream().noneMatch(MarkdownFxRenderer.Segment::table));
    }

    @Test
    public void testCells() {
        assertEquals(MarkdownFxRenderer.cells("| a | b \\| c |  |"), List.of("a", "b | c", ""));
        assertEquals(MarkdownFxRenderer.cells("a|b"), List.of("a", "b"));
    }
}
