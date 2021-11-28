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
package au.gov.asd.tac.constellation.views.scripting;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.importexport.ImportExportPluginRegistry;
import au.gov.asd.tac.constellation.plugins.importexport.text.ExportToTextPlugin;
import au.gov.asd.tac.constellation.plugins.parameters.DefaultPluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.file.ConstellationInstalledFileLocator;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.python.core.PyException;
import org.python.core.PyTraceback;

/**
 *
 * @author cygnus_x-1
 */
public class ScriptingViewPane extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(ScriptingViewPane.class.getName());
    private static final File GET_STARTED_FILE = ConstellationInstalledFileLocator.locate(
            "modules/ext/scripting_getting_started.txt",
            "au.gov.asd.tac.constellation.views.scripting",
            ScriptingViewPane.class.getProtectionDomain());
    private static final String SCRIPTING_VIEW_THREAD_NAME = "Scripting View";

    private static final String LANGUAGE = "Python";

    private final ScriptingViewTopComponent topComponent;
    private final ScriptingParser scriptParser;
    private final RSyntaxTextArea scriptEditor;
    private final JPanel scriptPane;
    private final JPopupMenu optionsMenu;
    private final JButton optionsButton;
    private final JButton executeButton;

    private File scriptFile;
    private boolean newOutput;

    public ScriptingViewPane(final ScriptingViewTopComponent topComponent) {
        this.topComponent = topComponent;
        this.scriptFile = null;
        this.newOutput = false;

        this.scriptParser = new ScriptingParser();

        this.scriptEditor = new RSyntaxTextArea(5, 80);
        scriptEditor.addParser(scriptParser);
        scriptEditor.setAnimateBracketMatching(true);
        scriptEditor.setAntiAliasingEnabled(true);
        scriptEditor.setAutoIndentEnabled(true);
        scriptEditor.setCodeFoldingEnabled(true);
        scriptEditor.setCurrentLineHighlightColor(new Color(224, 232, 241));
        scriptEditor.setTabsEmulated(true);
        scriptEditor.setTabSize(4);
        scriptEditor.setWhitespaceVisible(true);
        scriptEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        scriptEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(final KeyEvent ke) {
                final boolean isCtrl = ke.isControlDown();
                if (isCtrl) {
                    switch (ke.getKeyCode()) {
                        case KeyEvent.VK_ENTER:
                            executeScript();
                            break;
                        case KeyEvent.VK_S:
                            saveScript();
                            break;
                        default:
                            break;
                    }
                }
            }
        });

        try {
            if (GET_STARTED_FILE == null) {
                throw new FileNotFoundException("The file could not be located.");
            }
            try (final BufferedReader reader = new BufferedReader(new FileReader(new File(GET_STARTED_FILE.getPath())))) {
                StringBuilder getStartedText = new StringBuilder();
                reader.lines().forEach(line -> getStartedText.append(line).append(SeparatorConstants.NEWLINE));
                scriptEditor.setText(getStartedText.toString());
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        } catch (final FileNotFoundException e) {
            scriptEditor.setText("# SORRY, THERE WAS A PROBLEM LOADING THE 'GET STARTED' FILE\n");
        }

        final RTextScrollPane scriptScrollPane = new RTextScrollPane(scriptEditor);
        scriptScrollPane.setLineNumbersEnabled(true);
        scriptScrollPane.setFoldIndicatorEnabled(true);

        this.scriptPane = new JPanel();
        scriptPane.setBorder(BorderFactory.createTitledBorder("Script"));
        scriptPane.setLayout(new BorderLayout());
        scriptPane.add(scriptScrollPane, BorderLayout.CENTER);

        this.optionsMenu = new JPopupMenu();

        final JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(e -> openScript());
        optionsMenu.add(openItem);

        final JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.addActionListener(e -> saveScript());
        optionsMenu.add(saveAsItem);

        final JMenuItem newOutputItem = new JCheckBoxMenuItem("New Output Window", newOutput);
        newOutputItem.setSelected(newOutput);
        newOutputItem.addActionListener(e -> newOutput = newOutputItem.isSelected());
        optionsMenu.add(newOutputItem);

        final JMenuItem apiItem = new JMenuItem("API Documentation");
        apiItem.addActionListener(e -> new HelpCtx(this.getClass().getPackage().getName()).display());
        optionsMenu.add(apiItem);

        final Collection<? extends ScriptingAbstractAction> scriptingActions = Lookup.getDefault().lookupAll(ScriptingAbstractAction.class);
        scriptingActions.forEach(action -> {
            final JMenuItem item = new JMenuItem(action.getLabel());
            item.addActionListener(e -> action.performAction(this));
            optionsMenu.add(item);
        });

        this.optionsButton = new JButton();
        optionsButton.setText("Options");
        optionsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent me) {
                optionsMenu.show(optionsButton, me.getX(), me.getY());
            }
        });

        this.executeButton = new JButton();
        executeButton.setText("Execute");
        executeButton.setIcon(ImageUtilities.loadImageIcon("execute.png", false));
        executeButton.addActionListener(e -> executeScript());

        final GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                        .addComponent(scriptPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(optionsButton)
                                                .addPreferredGap(ComponentPlacement.RELATED, 344, Short.MAX_VALUE)
                                                .addComponent(executeButton)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(scriptPane, GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(executeButton)
                                        .addComponent(optionsButton))
                                .addContainerGap())
        );
        ScriptingViewPane.this.setLayout(layout);
        ScriptingViewPane.this.setPreferredSize(new Dimension());
    }

    public void setText(final String text) {
        scriptEditor.setText(text);
        scriptEditor.setCaretPosition(0);
    }

    public void setScriptFile(final File file) {
        scriptFile = file;
        ((TitledBorder) scriptPane.getBorder()).setTitle(String.format("Script %s", scriptFile.getName()));
    }

    public void update(final Graph graph) {
        executeButton.setEnabled(graph != null);
    }

    private void openScript() {
        final JFileChooser fileChooser = new FileChooserBuilder(ScriptingViewTopComponent.class)
                .setTitle(String.format("Save %s script", LANGUAGE))
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(final File pathName) {
                        final String name = pathName.getName().toLowerCase();
                        if (pathName.isFile() &&StringUtils.endsWithIgnoreCase(name, FileExtensionConstants.PYTHON)) {
                            return true;
                        }
                        return pathName.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return String.format("%s script file", LANGUAGE);
                    }
                })
                .createFileChooser();

        final int state = fileChooser.showOpenDialog(this);
        if (state == JFileChooser.APPROVE_OPTION) {
            PluginExecution.withPlugin(new LoadScriptPlugin(fileChooser, scriptEditor, this)).executeLater(null);
        }
    }

    private void saveScript() {
        final JFileChooser fileChooser = new FileChooserBuilder(ScriptingViewTopComponent.class)
                .setTitle(String.format("Save %s script", LANGUAGE))
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(final File pathName) {
                        final String name = pathName.getName().toLowerCase();
                        if (pathName.isFile() && StringUtils.endsWithIgnoreCase(name, FileExtensionConstants.PYTHON)) {
                            return true;
                        }
                        return pathName.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return String.format("%s script file", LANGUAGE);
                    }
                })
                .createFileChooser();

        if (scriptFile != null) {
            fileChooser.setSelectedFile(scriptFile);
        }

        final int state = fileChooser.showSaveDialog(this);
        if (state == JFileChooser.APPROVE_OPTION) {
            String fileName = fileChooser.getSelectedFile().getPath();
            if (!StringUtils.endsWithIgnoreCase(fileName, FileExtensionConstants.PYTHON)) {
                fileName += FileExtensionConstants.PYTHON;
            }
            final String text = scriptEditor.getText();
            PluginExecution.withPlugin(ImportExportPluginRegistry.EXPORT_TEXT)
                    .withParameter(ExportToTextPlugin.FILE_NAME_PARAMETER_ID, fileName)
                    .withParameter(ExportToTextPlugin.TEXT_PARAMETER_ID, text)
                    .executeLater(topComponent.getCurrentGraph());
            setScriptFile(new File(fileName));
        }
    }

    private void executeScript() {
        if (topComponent.getCurrentGraph() != null) {
            scriptParser.clearError();
            scriptEditor.forceReparsing(scriptParser);

            final Plugin plugin = PluginRegistry.get(ScriptingRegistry.SCRIPT_EXECUTOR_PLUGIN);
            final PluginParameters parameters = DefaultPluginParameters.getDefaultParameters(plugin);
            parameters.getParameters().get(ScriptingExecutePlugin.SCRIPT_PARAMETER_ID)
                    .setStringValue(scriptEditor.getText());
            parameters.getParameters().get(ScriptingExecutePlugin.NEW_OUTPUT_PARAMETER_ID)
                    .setBooleanValue(newOutput);
            parameters.getParameters().get(ScriptingExecutePlugin.GRAPH_NAME_PARAMETER_ID)
                    .setStringValue(GraphNode.getGraphNode(topComponent.getCurrentGraph()).getDisplayName());
            final Future<?> f = PluginExecution.withPlugin(plugin)
                    .withParameters(parameters).executeLater(topComponent.getCurrentGraph());

            new Thread(() -> {
                try {
                    setName(SCRIPTING_VIEW_THREAD_NAME);
                    f.get();
                } catch (final InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, "Script Execution was interrupted", ex);
                    Thread.currentThread().interrupt();
                } catch (final ExecutionException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                }

                final Exception ex = (Exception) parameters.getParameters()
                        .get(ScriptingExecutePlugin.OUTPUT_EXCEPTION_PARAMETER_ID).getObjectValue();
                if (ex != null) {
                    LOGGER.severe(ex.getLocalizedMessage());
                    int line = -1;
                    final String msg = ex.getMessage();
                    final Throwable cause = ex.getCause();
                    if (cause != null) {
                        LOGGER.log(Level.SEVERE, "Cause of error is {0}", cause.getClass());

                        // If this is Jython, we can walk the traceback to find the line where things went bad.
                        if (cause instanceof PyException) {
                            final PyException pyex = (PyException) cause;
                            PyTraceback tb = pyex.traceback;
                            while (tb != null) {
                                line = tb.tb_lineno - 1;
                                LOGGER.log(Level.SEVERE, "{0} traceback {1}", new Object[]{LANGUAGE, tb.tb_lineno});
                                tb = (PyTraceback) tb.tb_next;
                            }
                        }
                    }

                    if (line >= 0) {
                        scriptParser.setError(msg, line);
                    } else {
                        // Search the exception message to find the line (and possibly column) where the error occurred.
                        final Pattern linePattern = Pattern.compile(" at line number (\\d+)");
                        final Matcher lineMatcher = linePattern.matcher(msg);
                        if (lineMatcher.find()) {
                            final String ln = lineMatcher.group(1);
                            line = Integer.parseInt(ln) - 1;

                            final Pattern columnPattern = Pattern.compile(" at column number (\\d+)");
                            final Matcher columnMatcher = columnPattern.matcher(msg);
                            if (columnMatcher.find()) {
                                final String col = columnMatcher.group(1);
                                final int column = Integer.parseInt(col) - 1;
                                try {
                                    final int lso = scriptEditor.getLineStartOffset(line);
                                    final int leo = scriptEditor.getLineEndOffset(line);
                                    final int length = (leo - lso) - column;
                                    scriptParser.setError(msg, line, lso + column, length);
                                } catch (final BadLocationException ex1) {
                                    scriptParser.setError(msg, line);
                                }
                            } else {
                                scriptParser.setError(msg, line);
                            }
                        }
                    }

                    scriptEditor.forceReparsing(scriptParser);
                }
            }).start();
        } else {
            final NotifyDescriptor notifyDescriptor = new NotifyDescriptor(
                    "Scripts require a graph.",
                    "Scripting",
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object[]{NotifyDescriptor.OK_OPTION},
                    NotifyDescriptor.OK_OPTION);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
        }
    }

    @PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
    public static class LoadScriptPlugin extends SimplePlugin {

        final JFileChooser fileChooser;
        final RSyntaxTextArea scriptEditor;
        final ScriptingViewPane pane;

        public LoadScriptPlugin(final JFileChooser fileChooser, final RSyntaxTextArea scriptEditor, final ScriptingViewPane pane) {
            this.fileChooser = fileChooser;
            this.scriptEditor = scriptEditor;
            this.pane = pane;
        }

        @Override
        public String getName() {
            return "Scripting View: Load Script";
        }

        @Override
        protected void execute(final PluginGraphs _graphs, final PluginInteraction _interaction, final PluginParameters _parameters) throws InterruptedException, PluginException {
            try {
                try (final BufferedReader reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(fileChooser.getSelectedFile()), StandardCharsets.UTF_8.name()))) {
                    final StringBuilder b = new StringBuilder();
                    while (true) {
                        final String s = reader.readLine();
                        if (s == null) {
                            break;
                        }
                        b.append(s).append(SeparatorConstants.NEWLINE);
                    }

                    SwingUtilities.invokeLater(() -> {
                        pane.setScriptFile(fileChooser.getSelectedFile());
                        scriptEditor.setText(b.toString());
                    });
                }
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }

    }

}
