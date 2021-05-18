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
package au.gov.asd.tac.constellation.preferences;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * UI controller for the font preferences panel
 * 
 * @author Delphinus8821
 */
@OptionsPanelController.SubRegistration(
        location = "constellation",
        displayName = "#ApplicationFontOptions_DisplayName",
        keywords = "#ApplicationFontOptions_Keywords",
        keywordsCategory = "constellation/Preferences",
        position = 100)
@org.openide.util.NbBundle.Messages({
    "ApplicationFontOptions_DisplayName=Application Font",
    "ApplicationFontOptions_Keywords=font size"
})
public class FontOptionsPanelController extends OptionsPanelController{
    
    private FontOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private static final Logger LOGGER = Logger.getLogger(FontOptionsPanelController.class.getName());

    @Override
    public void update() {
        final Preferences prefs = NbPreferences.forModule(FontPreferenceKeys.class);
        final FontOptionsPanel fontOptionsPanel = getPanel();
        
        fontOptionsPanel.setCurrentFont(prefs.get(FontPreferenceKeys.FONT_FAMILY, FontPreferenceKeys.FONT_FAMILY_DEFAULT));
        fontOptionsPanel.setFontSize(prefs.get(FontPreferenceKeys.FONT_SIZE, FontPreferenceKeys.FONT_SIZE_DEFAULT));
    }

    @Override
    public void applyChanges() {
        if (isValid()) {
            pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
            
            if (isChanged()) {
                pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
                
                final Preferences prefs = NbPreferences.forModule(FontPreferenceKeys.class);
                final FontOptionsPanel fontOptionsPanel = getPanel();
                
                prefs.put(FontPreferenceKeys.FONT_FAMILY, fontOptionsPanel.getCurrentFont());
                prefs.put(FontPreferenceKeys.FONT_SIZE, fontOptionsPanel.getFontSize());
            }
        }
    }

    @Override
    public void cancel() {
        // Intentionally left blank
    }

    @Override
    public boolean isValid() {
        final FontOptionsPanel fontOptionsPanel = getPanel();
        return fontOptionsPanel.getCurrentFont() != null 
                && fontOptionsPanel.getFontSize() != null;  
    }

    @Override
    public boolean isChanged() {
        final Preferences prefs = NbPreferences.forModule(FontPreferenceKeys.class);
        final FontOptionsPanel fontOptionsPanel = getPanel();
        return !(fontOptionsPanel.getCurrentFont().equals(prefs.get(FontPreferenceKeys.FONT_FAMILY, FontPreferenceKeys.FONT_FAMILY_DEFAULT))
                && fontOptionsPanel.getFontSize().equals(prefs.get(FontPreferenceKeys.FONT_SIZE, FontPreferenceKeys.FONT_SIZE_DEFAULT)));
    }

    @Override
    public JComponent getComponent(final Lookup lookup) {
        return getPanel();
    }
    
    @Override
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("au.gov.asd.tac.constellation.preferences.font");
    }

    
    private FontOptionsPanel getPanel() {
        if (panel == null){
            panel = new FontOptionsPanel(this);
        }
        return panel;
    }
}
