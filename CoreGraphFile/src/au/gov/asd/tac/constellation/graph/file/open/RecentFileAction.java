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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Action that presents list of recently closed files/documents.
 *
 * @author Dafe Simonek
 */
@ActionRegistration(displayName = "#LBL_RecentFileAction_Name", lazy = false)
@ActionID(category = "File", id = "org.netbeans.modules.openfile.RecentFileAction")
@ActionReference(path = "Menu/File", position = 400)
public class RecentFileAction extends AbstractAction
        implements Presenter.Menu, PopupMenuListener, ChangeListener {

    private static final Logger LOGGER = Logger.getLogger(RecentFileAction.class.getName());

    /**
     * property of menu items where we store fileobject to open
     */
    private static final String PATH_PROP
            = "RecentFileAction.Recent_File_Path"; // NOI18N
    private static final String OFMSG_PATH_IS_NOT_DEFINED
            = NbBundle.getMessage(RecentFileAction.class,
                    "OFMSG_PATH_IS_NOT_DEFINED");  // NOI18N
    private static final String OFMSG_FILE_NOT_EXISTS
            = NbBundle.getMessage(RecentFileAction.class,
                    "OFMSG_FILE_NOT_EXISTS");      // NOI18N
    private JMenu menu;

    public RecentFileAction() {
        super(NbBundle.getMessage(RecentFileAction.class,
                "LBL_RecentFileAction_Name")); // NOI18N
    }

    /**
     * ******* Presenter.Menu impl *********
     */
    @Override
    public JMenuItem getMenuPresenter() {
        if (menu == null) {
            menu = new UpdatingMenu(this);
            menu.setMnemonic(NbBundle.getMessage(RecentFileAction.class,
                    "MNE_RecentFileAction_Name").charAt(0)); // NOI18N
            // #115277 - workaround, PopupMenuListener don't work on Mac
            if (!Utilities.isMac()) {
                menu.getPopupMenu().addPopupMenuListener(this);
            } else {
                menu.addChangeListener(this);
            }
        }
        return menu;
    }

    /**
     * ***** PopupMenuListener impl ******
     */

    /* Fills submenu when popup is about to be displayed.
     * Note that argument may be null on Mac due to #115277 fix
     */
    @Override
    public void popupMenuWillBecomeVisible(final PopupMenuEvent arg0) {
        fillSubMenu();
    }

    /* Clears submenu when popup is about to be hidden.
     * Note that argument may be null on Mac due to #115277 fix
     */
    @Override
    public void popupMenuWillBecomeInvisible(final PopupMenuEvent arg0) {
        menu.removeAll();
    }

    @Override
    public void popupMenuCanceled(final PopupMenuEvent arg0) {
        // Required to implement PopupMenuListener
    }

    /**
     * ****** ChangeListener impl ********
     */
    /**
     * Delegates to popupMenuListener based on menu current selection status
     */
    @Override
    public void stateChanged(final ChangeEvent e) {
        if (menu.isSelected()) {
            popupMenuWillBecomeVisible(null);
        } else {
            popupMenuWillBecomeInvisible(null);
        }
    }

    /**
     * Fills submenu with recently closed files got from RecentFiles support
     */
    private void fillSubMenu() {
        RecentFiles.getUniqueRecentFiles().forEach(hItem -> {
            // Attempt to create a menu item for the history item.
            // We will skip the creation if the file object corresponding to the
            // history item is null (indicating that the file no longer exists)
            // or if there is an issue loading the icon corresponding to the file.
            try {
                final FileObject fo = RecentFiles.convertPath2File(hItem.getPath());
                if (fo != null) {
                    final JMenuItem jmi = newSubMenuItem(fo);
                    menu.add(jmi);
                }
            } catch (final DataObjectNotFoundException ex) {
                LOGGER.log(Level.FINE, "Icon for file was not found, skipping the creation of the menu item");
            }
        });
        ensureSelected();
    }

    /**
     * Creates and configures an item of the submenu according to the given
     * {@code HistoryItem}.
     *
     * @param hItem the {@code HistoryItem}.
     * @return the munu item.
     */
    private JMenuItem newSubMenuItem(final FileObject fo)
            throws DataObjectNotFoundException {
        // Try and find the icon for the history item. If this can't be done
        // eg. because the file no longer exists, then a DataObjectNotFoundException
        // is thrown. The caller should catch this exception and skip the history item
        // it was trying to create a menu item for.
        final Icon icon = getIcon(fo);
        final String path = fo.getPath();
        final JMenuItem jmi = new JMenuItem(fo.getName()) {
            public @Override
            void menuSelectionChanged(final boolean isIncluded) {
                super.menuSelectionChanged(isIncluded);
                if (isIncluded) {
                    StatusDisplayer.getDefault().setStatusText(path);
                }
            }
        };
        jmi.putClientProperty(PATH_PROP, path);
        jmi.addActionListener(this);
        jmi.setIcon(icon);
        return jmi;
    }

    private Icon getIcon(final FileObject fo) throws DataObjectNotFoundException {
        final DataObject dObj = DataObject.find(fo);
        return new ImageIcon(dObj.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
    }

    /**
     * Workaround for JDK bug 6663119, it ensures that first item in submenu is
     * correctly selected during keyboard navigation.
     */
    private void ensureSelected() {
        if (menu.getMenuComponentCount() <= 0) {
            return;
        }

        final Component first = menu.getMenuComponent(0);
        if (!(first instanceof JMenuItem)) {
            return;
        }

        final Point loc = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(loc, menu);
        final MenuElement[] selPath = MenuSelectionManager.defaultManager().getSelectedPath();

        // apply workaround only when mouse is not hovering over menu
        // (which signalizes mouse driven menu traversing) and only
        // when selected menu path contains expected value - submenu itself
        if (!menu.contains(loc) && selPath.length > 0
                && menu.getPopupMenu() == selPath[selPath.length - 1]) {
            // select first item in submenu through MenuSelectionManager
            final MenuElement[] newPath = new MenuElement[selPath.length + 1];
            System.arraycopy(selPath, 0, newPath, 0, selPath.length);
            final JMenuItem firstItem = (JMenuItem) first;
            newPath[selPath.length] = firstItem;
            MenuSelectionManager.defaultManager().setSelectedPath(newPath);
        }
    }

    /**
     * Opens recently closed file, using OpenFile support.
     *
     * Note that method works as action handler for individual submenu items
     * created in fillSubMenu, not for whole RecentFileAction.
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        final Object source = evt.getSource();
        if (source instanceof JMenuItem) {
            final JMenuItem menuItem = (JMenuItem) source;
            final String path = (String) menuItem.getClientProperty(PATH_PROP);
            final String msg = openFile(path);
            if (msg != null) {
                StatusDisplayer.getDefault().setStatusText(msg);
                Toolkit.getDefaultToolkit().beep();
                RecentFiles.pruneHistory();
            }
        }
        // TODO: processing of pressing of a shortcut key that can be associated
        // with this action. Note, in this case, any UI component can be passed
        // as the source.
    }

    /**
     * Open a file.
     *
     * @param path the path to the file or {@code null}.
     * @return error message or {@code null} on success.
     */
    private String openFile(final String path) {
        if (StringUtils.isBlank(path)) {
            return OFMSG_PATH_IS_NOT_DEFINED;
        }
        final File f = new File(path);
        if (!f.exists()) {
            return OFMSG_FILE_NOT_EXISTS;
        }
        final File nf = FileUtil.normalizeFile(f);
        return OpenFile.open(FileUtil.toFileObject(nf), -1);
    }

    /**
     * Menu that checks its enabled state just before is populated
     */
    private class UpdatingMenu extends JMenu implements DynamicMenuContent {

        private final JComponent[] content = new JComponent[]{this};

        public UpdatingMenu(final Action action) {
            super(action);
        }

        @Override
        public JComponent[] getMenuPresenters() {
            setEnabled(RecentFiles.hasRecentFiles());
            return content;
        }

        @Override
        public JComponent[] synchMenuPresenters(final JComponent[] items) {
            return getMenuPresenters();
        }
    }
}
