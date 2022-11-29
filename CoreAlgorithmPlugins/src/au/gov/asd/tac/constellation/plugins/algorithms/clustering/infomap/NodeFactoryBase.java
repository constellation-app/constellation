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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap;

import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.traits.FlowBase;

/**
 *
 * @author algol
 */
public interface NodeFactoryBase {

    public NodeBase createNode();

    public NodeBase createNode(final String name, final double flow, final double teleWeight);

    public NodeBase createNode(final NodeBase node);

    public NodeBase createNode(final FlowBase data);

    public FlowBase createFlow();
}
