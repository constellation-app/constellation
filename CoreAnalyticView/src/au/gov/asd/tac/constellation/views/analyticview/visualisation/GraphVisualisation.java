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
package au.gov.asd.tac.constellation.views.analyticview.visualisation;

import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import java.util.List;

/**
 * A visualisation of AnalyticResult on the graph.
 *
 * @author cygnus_x-1
 */
public abstract class GraphVisualisation extends AnalyticVisualisation {

    public abstract List<SchemaAttribute> getAffectedAttributes();

    public abstract void deactivate();

    public abstract boolean isActive();

    public abstract void setSelected(final boolean selected);
}
