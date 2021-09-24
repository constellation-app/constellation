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
package au.gov.asd.tac.constellation.graph.file.open;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Open a file.
 *
 * @author canis_majoris
 * @author sol695510
 */
@ServiceProvider(service = Plugin.class)
@Messages("OpenFilePlugin=Open File")
@PluginInfo(pluginType = PluginType.IMPORT, tags = {"LOW LEVEL"})
public class OpenFilePlugin extends SimpleReadPlugin {

    private static final Frame window = WindowManager.getDefault().getMainWindow();
    private static final Preferences preferences = NbPreferences.forModule(ApplicationPreferenceKeys.class);
    private static final boolean REMEMBER_OPEN_AND_SAVE_LOCATION = preferences.getBoolean(ApplicationPreferenceKeys.REMEMBER_OPEN_AND_SAVE_LOCATION, ApplicationPreferenceKeys.REMEMBER_OPEN_AND_SAVE_LOCATION_DEFAULT);

    private final File DEFAULT_DIRECTORY = new File(System.getProperty("user.home"));
    private static File SAVED_DIRECTORY = null;

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(this.getClass().getName());
    }

    @Override
    protected void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        final JFileChooser chooser = new FileChooser();
        chooser.setCurrentDirectory(SAVED_DIRECTORY != null ? SAVED_DIRECTORY : DEFAULT_DIRECTORY);
        HelpCtx.setHelpIDString(chooser, getHelpCtx().getHelpID());

        final List<File> selectedFiles = new ArrayList<>();

        if (chooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
            selectedFiles.addAll(Arrays.asList(chooser.getSelectedFiles()));
        }

        if (!selectedFiles.isEmpty()) {
            SAVED_DIRECTORY = REMEMBER_OPEN_AND_SAVE_LOCATION ? chooser.getCurrentDirectory() : DEFAULT_DIRECTORY;

            selectedFiles.forEach(file -> OpenFile.openFile(file, -1));
        }
    }
}
