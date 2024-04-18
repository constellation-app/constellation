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
package au.gov.asd.tac.constellation.graph.node.gui;

import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.preferences.GraphOptionsPanelController;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 * This class represents the base/abstract class for all actions to be created
 * in the menu bar. These actions are only required for each toolbar action.
 *
 * @author altair
 * @author capricornuncorn123
 */
public abstract class MenuBaseAction extends AbstractAction implements Presenter.Menu, ActionListener, LookupListener, GraphChangeListener {

    protected final Lookup.Result<GraphNode> graphNodeSet;
    protected final Lookup lookup;
    protected JMenuItem menuButton;
    private GraphNode currentGraphNode;

    /**
     * This constructor must be called by each child class to ensure that
     * the look-ups are properly initialized
     */
    protected MenuBaseAction() {
        lookup = Utilities.actionsGlobalContext();
        graphNodeSet = Utilities.actionsGlobalContext().lookupResult(GraphNode.class);
        graphNodeSet.addLookupListener(WeakListeners.create(LookupListener.class, this, graphNodeSet));
        resultChanged(new LookupEvent(graphNodeSet));
    }

    /**
     * Return the current context.
     *
     * @return graphNode
     */
    protected GraphNode getContext() {
        return lookup.lookup(GraphNode.class);
    }

    /**
     * Occurs when an action is initiated by a user.
     * For {@link ActionListener} implementation.
     * 
     * @param event 
     */
    @Override
    public void actionPerformed(final ActionEvent event) {
        if (menuButton != null && isEnabled()) {
            this.updateValue();
        }
    }
    
    /**
     * Update the value of the widget based on the specific action.
     * This method may trigger a plugin to do the "work" of the action.
     */
    protected abstract void updateValue();

    /**
     * Occurs when a change to a graph is made.
     * For {@link GraphChangeListener} implementation.
     * 
     * @param event 
     */
    @Override
    public void graphChanged(final GraphChangeEvent event) {
        if (menuButton != null && isEnabled()) {
            this.displayValue();
        }
    }

    /**
     * Update the displayed value of the widget.
     */
    protected abstract void displayValue();

    /**
     * Create a new radio button widget
     *
     * @param label string
     * @param group button group
     * @param defaultValue value
     */
    protected void initRadioButton(final String label, final String group, final boolean defaultValue) {
        putValue(Action.NAME, label);
        putValue(Action.SHORT_DESCRIPTION, label);
        menuButton = new JRadioButtonMenuItem(this);
        menuButton.setSelected(defaultValue);
    }

    /**
     * Create a new checkbox widget
     *
     * @param label string
     * @param defaultValue value
     */
    protected void initCheckBox(final String label, final boolean defaultValue) {
        putValue(Action.NAME, label);
        putValue(Action.SHORT_DESCRIPTION, label);
        menuButton = new JCheckBoxMenuItem(this);
        menuButton.setSelected(defaultValue);
    }
    

    @Override
    public JMenuItem getMenuPresenter() {
        return menuButton;
    }

    /**
     * Checks if a MenuBaseAction is enabled.
     * Updates the menuButton if the button exists.
     * For {@link AbstractAction} extension.
     * @return 
     */
    @Override
    public final boolean isEnabled() {
        final boolean flag = getEnabled() && this.getContext() != null;
        if (menuButton != null) {
            menuButton.setEnabled(flag);
        }
        return flag;
    }
    
    /**
     * An method that can be overwritten to check if the menu button is enabled
     * based on action specific requirements.  
     * 
     * @return 
     */
    public boolean getEnabled(){
        return enabled;
    }

    /**
     * For {@link LookupListener} implementation.
     * 
     * @param le 
     */
    @Override
    public void resultChanged(final LookupEvent le) {
        if (currentGraphNode != null) {
            currentGraphNode.getGraph().removeGraphChangeListener(this);
            currentGraphNode = null;
        }

        if (this.getContext() != null) {
            currentGraphNode = this.getContext();
            currentGraphNode.getGraph().addGraphChangeListener(this);
            if (menuButton != null) {
                displayValue();
            }
        }
        
        isEnabled();
    }
}
