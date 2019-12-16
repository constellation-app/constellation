/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.preferences;

import au.gov.asd.tac.constellation.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;


/**
 * UI controller for the graph preferences panel
 * 
 * @author aldebaran30701
 */
@OptionsPanelController.SubRegistration(
        location = "constellation",
        displayName = "#GraphOption_DisplayName",
        keywords = "#GraphOption_Keywords",
        keywordsCategory = "constellation/GraphPreferences",
        position = 0)
@org.openide.util.NbBundle.Messages({
    "GraphOption_DisplayName=Graph",
    "GraphOption_Keywords=blaze size blaze opacity blaze colour"
})

public final class GraphOptionsPanelController extends OptionsPanelController {

    private GraphOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public void update() {
        final Preferences prefs = NbPreferences.forModule(GraphPreferenceKeys.class);
        final GraphOptionsPanel graphOptionsPanel = getPanel();
        
        // grabbing blaze size from preferences file, reverting to default if none found
        graphOptionsPanel.setBlazeSize(prefs.getInt(GraphPreferenceKeys.BLAZE_SIZE, GraphPreferenceKeys.BLAZE_SIZE_DEFAULT));
        graphOptionsPanel.setBlazeOpacity(prefs.getInt(GraphPreferenceKeys.BLAZE_OPACITY, GraphPreferenceKeys.BLAZE_OPACITY_DEFAULT));

    }

    // Once valid, and once changed, grabs the current value and saves it into the preferences file
    @Override
    public void applyChanges() {
        if (isValid()) {
            pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);

            if (isChanged()) {
                pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);

                final Preferences prefs = NbPreferences.forModule(GraphPreferenceKeys.class);
                final GraphOptionsPanel graphOptionsPanel = getPanel();
                //50 and 50 (initial values upon startup)
                final int prevBlazeSize = prefs.getInt(GraphPreferenceKeys.BLAZE_SIZE, GraphPreferenceKeys.BLAZE_SIZE_DEFAULT);
                final int prevBlazeopacity = prefs.getInt(GraphPreferenceKeys.BLAZE_OPACITY, GraphPreferenceKeys.BLAZE_OPACITY_DEFAULT);
                
                prefs.putInt(GraphPreferenceKeys.BLAZE_SIZE, graphOptionsPanel.getBlazeSize());
                prefs.putInt(GraphPreferenceKeys.BLAZE_OPACITY, graphOptionsPanel.getBlazeOpacity());
                
                GraphPreferenceKeys.setDefaultBlazeSize(graphOptionsPanel.getBlazeSize());
                // new value and new value
                final float storedBlazeSize = GraphPreferenceKeys.getDefaultBlazeSize();
                final int storedBlazeopacity = prefs.getInt(GraphPreferenceKeys.BLAZE_OPACITY, GraphPreferenceKeys.BLAZE_OPACITY_DEFAULT);
                
                // Sync did not refresh anything in the store. Can delete because it did not change operation of the system
//                try {
//                    if(NbPreferences.forModule(GraphPreferenceKeys.class).parent()!=null){
//                        NbPreferences.forModule(GraphPreferenceKeys.class).parent().flush();
//                        NbPreferences.forModule(GraphPreferenceKeys.class).parent().sync();
//                    }
//                    prefs.flush();
//                    prefs.sync();
//                } catch (BackingStoreException ex) {
//                    Exceptions.printStackTrace(ex);
//                }

            }
        }
    }

    @Override
    public void cancel() {
    }

    // Add code to check valid values. may be needed for expansion of this UI manu.
    @Override
    public boolean isValid() {
        return true;
    }

    // Check if the preference values are changed upon adding to the UI menu
    @Override
    public boolean isChanged() {
        final Preferences prefs = NbPreferences.forModule(GraphPreferenceKeys.class);
        final GraphOptionsPanel graphOptionsPanel = getPanel();
        final boolean changed
                = !(graphOptionsPanel.getBlazeSize() == prefs.getInt(GraphPreferenceKeys.BLAZE_SIZE, GraphPreferenceKeys.BLAZE_SIZE_DEFAULT)
                && graphOptionsPanel.getBlazeOpacity() == prefs.getInt(GraphPreferenceKeys.BLAZE_OPACITY, GraphPreferenceKeys.BLAZE_OPACITY_DEFAULT));

        return changed;
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private GraphOptionsPanel getPanel() {
        if (panel == null) {
            panel = new GraphOptionsPanel(this);
        }
        return panel;
    }

    @Override
    public JComponent getComponent(final Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("au.gov.asd.tac.constellation.preferences.graph");
    }
}