/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.tableview.api;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import java.util.List;

/**
 * An interface for defining the attributes that will be presented in default 
 * columns for the Table View.
 *
 * @author Andromeda-224
 */
public interface TableDefaultColumns {
       
    /**
     * Gets the default graph attributes.
     * 
     * @param graph The graph to get the attributes from
     * 
     * @return List of default graph attributes.
     */
    public List<GraphAttribute> getDefaultAttributes(final Graph graph);
    
}
