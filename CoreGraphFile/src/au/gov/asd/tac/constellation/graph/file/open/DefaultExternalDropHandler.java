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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package au.gov.asd.tac.constellation.graph.file.open;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.ExternalDropHandler;
import org.openide.windows.TopComponent;

/**
 *
 * @author S. Aubrecht
 */
@org.openide.util.lookup.ServiceProvider(service = org.openide.windows.ExternalDropHandler.class)
public class DefaultExternalDropHandler extends ExternalDropHandler {
    
    private static final Logger LOGGER = Logger.getLogger(DefaultExternalDropHandler.class.getName());

    @Override
    public boolean canDrop(final DropTargetDragEvent e) {
        return canDrop(e.getCurrentDataFlavors());
    }

    @Override
    public boolean canDrop(final DropTargetDropEvent e) {
        return canDrop(e.getCurrentDataFlavors());
    }

    boolean canDrop(final DataFlavor[] flavors) {
        for (int i = 0; flavors != null && i < flavors.length; i++) {
            if (DataFlavor.javaFileListFlavor.equals(flavors[i]) || getUriListDataFlavor().equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleDrop(final DropTargetDropEvent e) {
        final Transferable t = e.getTransferable();
        if (t == null) {
            return false;
        }
        final List<File> fileList = getFileList(t);
        if (fileList == null || fileList.isEmpty()) {
            return false;
        }

        //#158473: Activate target TC to inform winsys in which mode new editor
        //component should be opened. It assumes that openFile opens new editor component
        //in some editor mode. If there would be problem with activating another TC first
        //then another way how to infrom winsys must be used.
        Component c = e.getDropTargetContext().getComponent();
        while (c != null) {
            if (c instanceof TopComponent tc) {
                tc.requestActive();
                break;
            }
            c = c.getParent();
        }

        Object errMsg = null;
        if (fileList.size() == 1) {
            errMsg = openFile(fileList.get(0));
        } else {
            boolean hasSomeSuccess = false;
            List<String> fileErrs = null;
            for (final File file : fileList) {
                final String fileErr = openFile(file);
                if (fileErr == null) {
                    hasSomeSuccess = true;
                } else {
                    if (fileErrs == null) {
                        fileErrs = new ArrayList<>(fileList.size());
                    }
                    fileErrs.add(fileErr);
                }
            }
            if (fileErrs != null) { //some file could not be opened
                final String mainMsgKey = hasSomeSuccess ? "MSG_could_not_open_some_files" 
                        : "MSG_could_not_open_any_file"; //NOI18N
                final String mainMsg = NbBundle.getMessage(OpenFile.class, mainMsgKey);
                final JComponent msgPanel = new JPanel();
                msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.PAGE_AXIS));
                msgPanel.add(new JLabel(mainMsg));
                msgPanel.add(Box.createVerticalStrut(12));
                for (final String fileErr : fileErrs) {
                    msgPanel.add(new JLabel(fileErr));
                }
                errMsg = msgPanel;
            }
        }
        if (errMsg != null) {
            DialogDisplayer.getDefault()
                    .notify(new NotifyDescriptor.Message(errMsg, NotifyDescriptor.WARNING_MESSAGE));
            return false;
        }
        return true;
    }

    protected List<File> getFileList(final Transferable t) {
        try {
            if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                //windows & mac
                @SuppressWarnings("unchecked") //transferData will be a list of files which extends from Object type
                final List<File> transferData = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                return transferData;
            } else if (t.isDataFlavorSupported(getUriListDataFlavor())) {
                //linux
                final String uriList = (String) t.getTransferData(getUriListDataFlavor());
                return textURIListToFileList(uriList);
            }
        } catch (final UnsupportedFlavorException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (final IOException ex) {
            // Ignore. Can be just "Owner timed out" from sun.awt.X11.XSelection.getData.
            LOGGER.log(Level.FINE, null, ex);
        }
        return null;
    }

    /**
     * Opens the given file.
     *
     * @param file file to be opened
     * @return {@code null} if the file was successfully opened; or a localized
     * error message in case of failure
     */
    protected String openFile(final File file) {
        final FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fo == null) {
            return NbBundle.getMessage(OpenFile.class, "MSG_FilePathTypeNotSupported", file.toString()); //NOI18N
        }
        return OpenFile.open(fo, -1);
    }
    
    private static DataFlavor uriListDataFlavor;

    protected static DataFlavor getUriListDataFlavor() {
        if (uriListDataFlavor == null) {
            try {
                uriListDataFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
            } catch (final ClassNotFoundException cnfE) {
                //cannot happen
                throw new AssertionError(cnfE);
            }
        }
        return uriListDataFlavor;
    }

    protected List<File> textURIListToFileList(final String data) {
        final List<File> list = new ArrayList<>(1);
        for (final StringTokenizer st = new StringTokenizer(data, "\r\n"); st.hasMoreTokens();) {
            final String s = st.nextToken();
            if (s.startsWith("#")) {
                // the line is a comment (as per the RFC 2483)
                continue;
            }
            try {
                final URI uri = new URI(s);
                final File file = new File(uri);
                list.add(file);
            } catch (final URISyntaxException | IllegalArgumentException e) {
                // malformed URI
            }
            // the URI is not a valid 'file:' URI

        }
        return list;
    }
}
