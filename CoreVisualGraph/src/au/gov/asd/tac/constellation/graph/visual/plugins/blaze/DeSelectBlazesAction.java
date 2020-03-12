/*
 * Copyright 2010-2019 Australian Signals Directorate. All Rights Reserved.
 *
 * NOTICE: All information contained herein remains the property of the
 * Australian Signals Directorate. The intellectual and technical concepts
 * contained herein are proprietary to the Australian Signals Directorate and
 * are protected by copyright law. Dissemination of this information or
 * reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from the Australian Signals Directorate.
 */
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.SimplePluginAction;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author mimosa
 */
@ActionID(category = "Selection", id = "au.gov.asd.tac.constellation.functionality.blaze.DeSelectBlazesAction")
@ActionRegistration(displayName = "#CTL_DeSelectBlazesAction", iconBase = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/blaze.png", surviveFocusChange = true)
@NbBundle.Messages("CTL_DeSelectBlazesAction=Deselect Blazes")
@ActionReference(path = "Menu/Selection", position = 460)
public class DeSelectBlazesAction extends SimplePluginAction {

    public DeSelectBlazesAction(GraphNode context) {
        super(context, VisualGraphPluginRegistry.DESELECT_BLAZES, true);
    }
}
