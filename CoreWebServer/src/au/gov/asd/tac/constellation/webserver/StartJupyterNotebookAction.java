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
package au.gov.asd.tac.constellation.webserver;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.gui.ScreenWindowsHelper;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import static au.gov.asd.tac.constellation.webserver.WebServer.getNotebookDir;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

@ActionID(category = "Tools", id = "au.gov.asd.tac.constellation.webserver.StartJupyterNotebookAction")
@ActionRegistration(displayName = "#CTL_StartJupyterNotebookAction", iconBase = "au/gov/asd/tac/constellation/webserver/resources/jupyter.png")
@ActionReference(path = "Menu/Tools", position = 1700)
@NbBundle.Messages("CTL_StartJupyterNotebookAction=Start Jupyter Notebook")
public class StartJupyterNotebookAction implements ActionListener {

    private static final String JUPYTER_NOTEBOOK = "jupyter-notebook";
    private static final String JUPYTER_OUTPUT = "Jupyter Notebook";

    private static final String ALERT_HEADER_TEXT = "Unable to start Jupyter Notebook in directory:\n%s\n\nAs this directory does not exist.";
    private static final String ALERT_CONTEXT_TEXT = "Jupyter Notebook will start in %s instead";

    @Override
    public void actionPerformed(final ActionEvent e) {
        WebServer.start();

        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final boolean useDefaultDirectory = prefs.getBoolean(ApplicationPreferenceKeys.CONSTELLATION_DIR_AS_NOTEBOOK_DIR, ApplicationPreferenceKeys.CONSTELLATION_DIR_AS_NOTEBOOK_DIR_DEFAULT);
        String prefDir = useDefaultDirectory ? ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT : prefs.get(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT);
        File dirFile = new File(prefDir);

        // If folder doesnt exist, alert user
        if (!dirFile.exists()) {
            alertUserUnableToStart();
            prefDir = ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT;
            dirFile = new File(prefDir);
        }

        final String dir = prefDir;

        try {
            // Start the jupyter-notebook process with its stderr redirected to
            // its stdout, and stdout being fed into an InputOutput window.
            final InputOutput io = IOProvider.getDefault().getIO(JUPYTER_OUTPUT, false);
            io.select();

            final List<String> exe = new ArrayList<>();
            exe.add(JUPYTER_NOTEBOOK);
            final ProcessBuilder pb = new ProcessBuilder(exe)
                    .directory(dirFile)
                    .redirectErrorStream(true);

            final Process jupyter = pb.start();

            final Thread out = new Thread(() -> {
                final InputStream fromProcess = jupyter.getInputStream();
                final OutputWriter ow = io.getOut();
                ow.format("Starting %s in directory %s ...\n\n", JUPYTER_NOTEBOOK, dir);

                final byte[] buf = new byte[1024];
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        final int len = fromProcess.read(buf);
                        if (len == -1) {
                            break;
                        }
                        final String s = new String(buf, 0, len, StandardCharsets.ISO_8859_1);
                        ow.write(s);
                    } catch (final IOException ex) {
                        break;
                    }
                }
            });
            out.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                out.interrupt();
                jupyter.destroy();
                io.closeInputOutput();
            }));

        } catch (final IOException ex) {
            final String msg = String.format("Failed to start %s: %s", JUPYTER_NOTEBOOK, ex.getMessage());
            NotificationDisplayer.getDefault().notify("Jupyter notebook",
                    UserInterfaceIconProvider.WARNING.buildIcon(16, ConstellationColor.DARK_ORANGE.getJavaColor()),
                    msg,
                    null
            );
        }
    }

    private void alertUserUnableToStart() {
        if (Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty("java.awt.headless"))) {
            return;
        }

        Platform.runLater(() -> {
            final Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText(String.format(ALERT_HEADER_TEXT, getNotebookDir()));
            alert.setContentText(String.format(ALERT_CONTEXT_TEXT, ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT));

            // Make sure alert is centered above main consty window
            final Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            final Point point = ScreenWindowsHelper.getMainWindowCentrePoint();
            if (point != null) {
                stage.setX(point.getX() - alert.getDialogPane().getWidth() / 2);
                stage.setY(point.getY() - alert.getDialogPane().getHeight() / 2);
            }

            stage.setAlwaysOnTop(true);

            alert.getDialogPane().getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());

            alert.showAndWait();
        });
    }
}
