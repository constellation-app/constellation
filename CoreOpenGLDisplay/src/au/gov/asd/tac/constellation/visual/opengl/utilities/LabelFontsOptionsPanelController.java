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
package au.gov.asd.tac.constellation.visual.opengl.utilities;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * A controller for {@link LabelFontsOptionsPanel}
 *
 * @author algol
 * @author cygnus_x-1
 */
@OptionsPanelController.SubRegistration(
        location = "constellation",
        displayName = "#LabelFontsOptions_DisplayName",
        keywords = "#LabelFontsOptions_Keywords",
        keywordsCategory = "constellation/Preferences",
        position = 800)
@org.openide.util.NbBundle.Messages({
    "LabelFontsOptions_DisplayName=Label Fonts",
    "LabelFontsOptions_Keywords=label font"
})
public final class LabelFontsOptionsPanelController extends OptionsPanelController implements HelpCtx.Provider {

    private LabelFontsOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public void update() {
        final Preferences prefs = NbPreferences.forModule(LabelFontsPreferenceKeys.class);
        final LabelFontsOptionsPanel labelFontsOptionsPanel = getPanel();

        labelFontsOptionsPanel.setUseMultiFonts(prefs.getBoolean(LabelFontsPreferenceKeys.USE_MULTI_FONTS, LabelFontsPreferenceKeys.USE_MULTI_FONTS_DEFAULT));
        labelFontsOptionsPanel.setFontList(prefs.get(LabelFontsPreferenceKeys.FONT_LIST, LabelFontsPreferenceKeys.FONT_LIST_DEFAULT));

        // do this last to ensure panel updates correctly
        labelFontsOptionsPanel.setUseDefaultSettings(prefs.getBoolean(LabelFontsPreferenceKeys.USE_DEFAULTS, LabelFontsPreferenceKeys.USE_DEFAULTS_DEFAULT));

        // Put the font loading here (rather than the constructor) so the user can install a new font and see it without restarting.
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames(Locale.getDefault());
        final String os = System.getProperty("os.name");
        if (StringUtils.containsIgnoreCase(os, "win")) {
            availableFonts = otfFontFilesWindows(availableFonts);
        }

        // TODO: look for unix fonts.
        Arrays.sort(availableFonts);
        labelFontsOptionsPanel.setAvailableFonts(availableFonts);

        // TODO: read settings and initialize GUI
        // Example:
        // someCheckBox.setSelected(Preferences.userNodeForPackage(LabelFontsPanel.class).getBoolean("someFlag", false));
        // or for org.openide.util with API spec. version >= 7.4:
        // someCheckBox.setSelected(NbPreferences.forModule(LabelFontsPanel.class).getBoolean("someFlag", false));
        // or:
        // someTextField.setText(SomeSystemOption.getDefault().getSomeStringProperty());
    }

    private String[] otfFontFilesWindows(final String[] existing) {
        final String local = System.getenv("LOCALAPPDATA");
        if (local != null) {
            final File fontDir = new File(local, "Microsoft/Windows/Fonts");
            if (fontDir.isDirectory()) {
                final File[] files = fontDir.listFiles((dir, name) -> StringUtils.endsWithIgnoreCase(name, FileExtensionConstants.OPEN_TYPE_FONT));
                if (files.length > 0) {
                    final List<String> names = Arrays.stream(existing).collect(Collectors.toList());
                    for (final File f : files) {
                        names.add(f.getName());
                    }

                    return names.toArray(new String[names.size()]);
                }
            }
        }

        return existing;
    }

    @Override
    public void applyChanges() {
        if (isValid()) {
            pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);

            if (isChanged()) {
                pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);

                final Preferences prefs = NbPreferences.forModule(LabelFontsPreferenceKeys.class);
                final LabelFontsOptionsPanel labelFontsOptionsPanel = getPanel();

                prefs.putBoolean(LabelFontsPreferenceKeys.USE_DEFAULTS, labelFontsOptionsPanel.getUseDefaultSettings());
                prefs.putBoolean(LabelFontsPreferenceKeys.USE_MULTI_FONTS, labelFontsOptionsPanel.getUseMultiFonts());
                prefs.put(LabelFontsPreferenceKeys.FONT_LIST, labelFontsOptionsPanel.getFontList());

                // TODO: store modified settings
                // Example:
                // Preferences.userNodeForPackage(LabelFontsPanel.class).putBoolean("someFlag", someCheckBox.isSelected());
                // or for org.openide.util with API spec. version >= 7.4:
                // NbPreferences.forModule(LabelFontsPanel.class).putBoolean("someFlag", someCheckBox.isSelected());
                // or:
                // SomeSystemOption.getDefault().setSomeStringProperty(someTextField.getText());
            }
        }
    }

    @Override
    public void cancel() {
        // DO NOTHING
    }

    @Override
    public boolean isValid() {
        getPanel();
        // TODO: check whether form is consistent and complete
        return true;
    }

    @Override
    public boolean isChanged() {
        final Preferences prefs = NbPreferences.forModule(LabelFontsPreferenceKeys.class);
        final LabelFontsOptionsPanel labelFontsOptionsPanel = getPanel();
        return !(labelFontsOptionsPanel.getUseDefaultSettings() == prefs.getBoolean(LabelFontsPreferenceKeys.USE_DEFAULTS, LabelFontsPreferenceKeys.USE_DEFAULTS_DEFAULT)
                && labelFontsOptionsPanel.getUseMultiFonts() == prefs.getBoolean(LabelFontsPreferenceKeys.USE_MULTI_FONTS, LabelFontsPreferenceKeys.USE_MULTI_FONTS_DEFAULT)
                && labelFontsOptionsPanel.getFontList().equals(prefs.get(LabelFontsPreferenceKeys.FONT_LIST, LabelFontsPreferenceKeys.FONT_LIST_DEFAULT)));
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    private LabelFontsOptionsPanel getPanel() {
        if (panel == null) {
            panel = new LabelFontsOptionsPanel(this);
        }
        return panel;
    }

    @Override
    public HelpCtx getHelpCtx() {
//        return new HelpCtx("au.gov.asd.tac.constellation.functionality.labelfonts");
        return new HelpCtx("au.gov.asd.tac.constellation.visual.opengl.labelfonts");
    }
}
