/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.functionality.open;


import au.gov.asd.tac.constellation.graph.file.open.FileChooser;
import au.gov.asd.tac.constellation.graph.file.open.OpenFileAction;

import au.gov.asd.tac.constellation.pluginframework.Plugin;
import au.gov.asd.tac.constellation.pluginframework.PluginException;
import au.gov.asd.tac.constellation.pluginframework.PluginGraphs;
import au.gov.asd.tac.constellation.pluginframework.PluginInteraction;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameter;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.pluginframework.templates.SimplePlugin;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author imranraz83
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("OpenGraphPlugin=Open Graph")
public class OpenGraphPlugin extends SimplePlugin{
    public static final String GRAPH_PARAMETER_ID = PluginParameter.buildId(OpenGraphPlugin.class, "graphId");
    /**
     * stores the last current directory of the file chooser
     */
    private static File currentDirectory = null;
    @Override
    public PluginParameters createParameters() {
                final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> graphIdParameter = StringParameterType.build(GRAPH_PARAMETER_ID);
        graphIdParameter.setName("graphId");
        graphIdParameter.setDescription("The Id of the graph");
        parameters.addParameter(graphIdParameter);
        return parameters;
    }
    @Override
    protected void execute(PluginGraphs graphs, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        SwingUtilities.invokeLater(() -> {
            OpenFileAction action = new OpenFileAction();
        action.actionPerformed(null);
        });
        
        return;
    }
    
    protected static JFileChooser prepareFileChooser() {
        JFileChooser chooser = new FileChooser();
        chooser.setCurrentDirectory(getCurrentDirectory());
        //HelpCtx.setHelpIDString(chooser, getHelpCtx().getHelpID());

        return chooser;
    }
    
    private File[] chooseFilesToOpen(final JFileChooser chooser)
            throws UserCancelException {
        File[] files;
        do {
            int selectedOption = chooser.showOpenDialog(
                    WindowManager.getDefault().getMainWindow());

            if (selectedOption != JFileChooser.APPROVE_OPTION) {
                throw new UserCancelException();
            }
            files = chooser.getSelectedFiles();
        } while (files.length == 0);
        return files;
    }
    
    private static File getCurrentDirectory(){
         // Otherwise, use last-selected directory, if any.
        if (currentDirectory != null && currentDirectory.exists()) {
            return currentDirectory;
        }
        // Fall back to default location ($HOME or similar).
        currentDirectory = new File(System.getProperty("user.home"));  // algol
        return currentDirectory;
    }
}
