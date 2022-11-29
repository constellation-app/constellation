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
package au.gov.asd.tac.constellation.graph.schema.attribute;

import au.gov.asd.tac.constellation.graph.GraphAttributeMerger;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import org.testng.annotations.Test;

/**
 * Schema Attribute Merger Test.
 *
 * @author sirius
 */
public class SchemaAttributeMergerNGTest {

    /**
     * Tests that when an attribute merger is specified on a schema attribute is
     * is correctly specified on the attribute that is created in the graph.
     */
    @Test
    public void schemaAttributeMergerTest() {
        final SchemaAttribute noMergerAttribute = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "NoMerger")
                .build();

        final SchemaAttribute defaultMergerAttribute = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "defaultMerger")
                .setAttributeMergerId(GraphAttributeMerger.getDefault().getId())
                .build();

        final StoreGraph graph = new StoreGraph();

        final int noMergerId = noMergerAttribute.ensure(graph);
        final int defaultMergerId = defaultMergerAttribute.ensure(graph);

        assert graph.getAttributeMerger(noMergerId) == null;
        assert graph.getAttributeMerger(defaultMergerId) == GraphAttributeMerger.getDefault();
    }
}
