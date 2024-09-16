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
package au.gov.asd.tac.constellation.graph.interaction.plugins.io;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.SaveAsCapable;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action to save document under a different file name and/or extension. The action is enabled for editor windows only.
 * This class is a copy of the org.openide.actions.SaveAsAction class except that additional validation was added
 *
 */
@ActionID(category = "File", id = "au.gov.asd.tac.constellation.functionality.save.SaveAsAction")
@ActionRegistration(displayName = "#MSG_SaveAs_SaveAsAction",
        lazy = false)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1100)
})
@Messages({
    "MSG_SaveAs_SaveAsAction=Save As...",
    "# {0} - folder name",
    "MSG_CannotCreateTargetFolder=Unable to create target folder {0}",
    "MSG_SaveAsTitle=Save Graph",
    "# {0} - file name",
    "# {1} - error msg",
    "MSG_SaveAsFailed=Unable to save the file {0}, Error Message {1}",
    "# {0} - file name",
    "MSG_SaveAs_OverwriteQuestion=The file {0} already exists. Do you want to overwrite it?",
    "MSG_SaveAs_OverwriteQuestion_Title=Save Graph",
    "MSG_SaveAs_SameFileSelected=The graph will be saved using the same file",
    "MSG_SaveAs_SameFileSelected_Title=Save Graph",
    "# {0} - file name",
    "MSG_SaveAs_FileInUse=The graph cannot be saved to {0}, since it is already in use by another graph.",
    "MSG_SaveAs_FileInUse_Title=Save Graph"
})
public class SaveAsAction extends AbstractAction implements ContextAwareAction {

    private static final Logger LOGGER = Logger.getLogger(SaveAsAction.class.getName());

    private static final String TITLE = "Save As";

    /**
     * Action to save document under a different file name and/or extension. The action is enabled for editor windows
     * only.
     *
     * @since 6.3
     * @author S. Aubrecht
     */
    private Lookup context;
    private Lookup.Result<SaveAsCapable> lkpInfo;
    private boolean isGlobal = false;
    private boolean isDirty = true;
    private PropertyChangeListener registryListener;
    private LookupListener lookupListener;
    private boolean isSaved = false;

    public SaveAsAction() {
        this(Utilities.actionsGlobalContext(), true);
    }

    public SaveAsAction(final Lookup context, final boolean isGlobal) {
        super(Bundle.MSG_SaveAs_SaveAsAction());
        this.context = context;
        this.isGlobal = isGlobal;
        putValue("noIconInMenu", Boolean.TRUE);
        setEnabled(false);
    }

    /**
     * Method is called from XML layers to create action instance for the main menu/toolbar.
     *
     * @return Global instance for menu/toolbar
     */
    public static ContextAwareAction create() {
        return new SaveAsAction();
    }

    @Override
    public boolean isEnabled() {
        if (isDirty || changeSupport == null || !changeSupport.hasListeners("enabled")) {
            refreshEnabled();
        }
        return super.isEnabled();
    }

    public boolean isSaved() {
        return isSaved;
    }

    @Override
    public synchronized void actionPerformed(final ActionEvent e) {
        refreshListeners();
        final Collection<? extends SaveAsCapable> inst = lkpInfo.allInstances();
        if (!inst.isEmpty()) {
            final SaveAsCapable saveAs = inst.iterator().next();
            FileChooser.openImmediateSaveDialog(getSaveFileChooser()).thenAccept(optionalFile -> optionalFile.ifPresent(file -> {
                try {
                    saveAs.saveAs(FileUtil.toFileObject(file.getParentFile()), file.getName());
                } catch (final IOException ioE) {
                    Exceptions.attachLocalizedMessage(ioE,
                            Bundle.MSG_SaveAsFailed(
                                    file.getName(),
                                    ioE.getLocalizedMessage()));
                    LOGGER.log(Level.SEVERE, null, ioE);
                }
                isSaved = true;
            }));
        }
    }

    /**
     * verify whether any other graph has this file opened
     *
     * @param filename the proposed filename
     * @return boolean
     */
    public boolean isFileInUse(final File filename) {
        boolean fileInUse = false;
        try {
            Iterator<Graph> iter = GraphNode.getAllGraphs().values().iterator();
            while (!fileInUse && iter.hasNext()) {
                GraphNode node = GraphNode.getGraphNode(iter.next());
                if (!node.getDataObject().isInMemory()) {
                    File existingFile = new File(node.getDataObject().getPrimaryFile().getPath());

                    fileInUse = filename.getCanonicalPath().equalsIgnoreCase(existingFile.getCanonicalPath());
                }
            }
        } catch (final IOException ex) {
            LOGGER.log(Level.WARNING, "Error occurred while attempting to retrieve file path");
        }
        return fileInUse;
    }

    @Override
    public Action createContextAwareInstance(final Lookup actionContext) {
        return new SaveAsAction(actionContext, false);
    }

    @Override
    public synchronized void addPropertyChangeListener(final PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        refreshListeners();
    }

    @Override
    public synchronized void removePropertyChangeListener(final PropertyChangeListener listener) {
        super.removePropertyChangeListener(listener);
        Mutex.EVENT.readAccess(this::refreshListeners // might be called off EQ by WeakListeners
        );
    }

    private PropertyChangeListener createRegistryListener() {
        return WeakListeners.propertyChange(evt -> isDirty = true, TopComponent.getRegistry());
    }

    private LookupListener createLookupListener() {
        return WeakListeners.create(LookupListener.class, (LookupListener) (final LookupEvent ev) -> isDirty = true, lkpInfo);
    }

    private void refreshEnabled() {
        if (lkpInfo == null) {
            //The thing we want to listen for the presence or absence of
            //on the global selection
            Lookup.Template<SaveAsCapable> tpl = new Lookup.Template<>(SaveAsCapable.class);
            lkpInfo = context.lookup(tpl);
        }

        TopComponent tc = TopComponent.getRegistry().getActivated();
        boolean isEditorWindowActivated = tc != null && WindowManager.getDefault().isEditorTopComponent(tc);
        setEnabled(lkpInfo != null && !lkpInfo.allItems().isEmpty() && isEditorWindowActivated);
        isDirty = false;
    }

    private void refreshListeners() {
        assert SwingUtilities.isEventDispatchThread() : "this shall be called just from AWT thread";

        if (lkpInfo == null) {
            //The thing we want to listen for the presence or absence of
            //on the global selection
            Lookup.Template<SaveAsCapable> tpl = new Lookup.Template<>(SaveAsCapable.class);
            lkpInfo = context.lookup(tpl);
        }

        if (changeSupport == null || !changeSupport.hasListeners("enabled")) { //NOI18N
            if (isGlobal && registryListener != null) {
                TopComponent.getRegistry().removePropertyChangeListener(registryListener);
                registryListener = null;
            }
            if (lookupListener != null) {
                lkpInfo.removeLookupListener(lookupListener);
                lookupListener = null;
            }
        } else {
            if (registryListener == null) {
                registryListener = createRegistryListener();
                TopComponent.getRegistry().addPropertyChangeListener(registryListener);
            }

            if (lookupListener == null) {
                lookupListener = createLookupListener();
                lkpInfo.addLookupListener(lookupListener);
            }
            refreshEnabled();
        }
    }

    //for unit testing
    boolean _isEnabled() {
        return super.isEnabled();
    }

    /**
     * Creates a new file chooser.
     *
     * @return the created file chooser.
     */
    public FileChooserBuilder getSaveFileChooser() {
        return FileChooser.createFileChooserBuilder(TITLE, FileExtensionConstants.STAR, "Constellation Files (" + FileExtensionConstants.STAR + ")", true);
    }
}
