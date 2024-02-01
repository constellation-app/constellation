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
package au.gov.asd.tac.constellation.views.wordcloud.ui;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * Concept class for the Word Cloud View 
 * 
 * @author Delphinus8821
 */
@ServiceProvider(service = SchemaConcept.class)
public class WordCloudConcept extends SchemaConcept {

    @Override
    public String getName() {
        return "Word Cloud";
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
        parentSet.add(AnalyticConcept.class);
        return Collections.unmodifiableSet(parentSet);
    }
    
    public static class MetaAttribute {
        
        public static final SchemaAttribute WORD_CLOUD_ATTRIBUTE = new SchemaAttribute.Builder(GraphElementType.META, WordCloudAttributeDescription.ATTRIBUTE_NAME, WordCloud.WORD_CLOUD_ATTR).build();
        
    }
    
    @Override
    public Collection<SchemaAttribute> getSchemaAttributes() {
        final List<SchemaAttribute> schemaAttributes = new ArrayList<>();
        schemaAttributes.add(MetaAttribute.WORD_CLOUD_ATTRIBUTE);
        return Collections.unmodifiableCollection(schemaAttributes);
    }
    
}
