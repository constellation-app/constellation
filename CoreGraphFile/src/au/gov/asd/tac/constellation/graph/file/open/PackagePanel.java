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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import org.openide.util.NbBundle;

/**
 * Panel offering mounting points to user, when opening .java file.
 *
 * TODO: this entire class should be refactored using form.
 */
public class PackagePanel extends JPanel {

    private final File f;
    private final int pkgLevel;
    private final List<File> dirs;
    private final List<String> pkgs;

    public PackagePanel(final File f, final int pkgLevel, final List<File> dirs, final List<String> pkgs) {
        this.f = f;
        this.pkgLevel = pkgLevel;
        this.dirs = dirs;
        this.pkgs = pkgs;

        initComponents2();

        initAccessibility();
    }

    protected JButton getOKButton() {
        return okButton;
    }

    protected JButton getCancelButton() {
        return cancelButton;
    }

    protected JList<String> getList() {
        return list;
    }

    /**
     *
     */
    private void initComponents2() {
        okButton = new JButton(NbBundle.getMessage(PackagePanel.class, "LBL_okButton"));
        cancelButton = new JButton(NbBundle.getMessage(PackagePanel.class, "LBL_cancelButton"));
        list = new JList<>(pkgs.toArray(new String[pkgs.size()]));

        setLayout(new BorderLayout(0, 5));
        setBorder(new javax.swing.border.EmptyBorder(8, 8, 8, 8));

        textArea = new JTextArea();
        textArea.setDisabledTextColor(javax.swing.UIManager.getColor("Label.foreground"));
        textArea.setFont(javax.swing.UIManager.getFont("Label.font"));
        textArea.setText(NbBundle.getMessage(PackagePanel.class, pkgLevel == -1 ? "TXT_whereMountNoSuggest" : "TXT_whereMountSuggest", f.getName()));
        textArea.setEditable(false);
        textArea.setEnabled(false);
        textArea.setOpaque(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        add(textArea, BorderLayout.NORTH);

        list.setVisibleRowCount(5);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (pkgLevel != -1) {
            list.setSelectedIndex(pkgLevel);
        }
        list.setCellRenderer(new ListCellRenderer<>() {
            private final Icon folderIcon = new ImageIcon(OpenFile.class.getResource("resources/folder.gif")); // NOI18N
            private final Icon rootFolderIcon = new ImageIcon(OpenFile.class.getResource("resources/rootFolder.gif")); // NOI18N
            private final JLabel lab = new JLabel();

            @Override
            public Component getListCellRendererComponent(final JList<? extends String> lst, final String value, final int index, final boolean isSelected, final boolean cellHasFocus) {
                final String pkg2 = value;
                if (pkg2.isEmpty()) { // NOI18N
                    lab.setText(NbBundle.getMessage(PackagePanel.class, "LBL_packageWillBeDefault"));
                    lab.setIcon(rootFolderIcon);
                } else {
                    lab.setText(NbBundle.getMessage(PackagePanel.class, "LBL_packageWillBe", pkg2));
                    lab.setIcon(folderIcon);
                }
                if (isSelected) {
                    lab.setBackground(lst.getSelectionBackground());
                    lab.setForeground(lst.getSelectionForeground());
                } else {
                    lab.setBackground(lst.getBackground());
                    lab.setForeground(lst.getForeground());
                }
                lab.setEnabled(lst.isEnabled());
                lab.setFont(lst.getFont());
                lab.setOpaque(true);
                return lab;
            }
        });
        add(new JScrollPane(list), BorderLayout.CENTER);

        // Name of mount point:
        final JTextField field = new JTextField();
        field.setEditable(false);
        field.setEnabled(true);
        
        // Accessibility
        field.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PackagePanel.class, "ACS_Field"));
        field.selectAll();
        field.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(final FocusEvent e) {
                field.selectAll();
            }

            @Override
            public void focusLost(final java.awt.event.FocusEvent e) {
                // Required for implementation of the FocusListener
            }
        });
        add(field, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(450, 300));

        list.addListSelectionListener(ev -> updateLabelEtcFromList(field, list, dirs, okButton));
        updateLabelEtcFromList(field, list, dirs, okButton);
    }

    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(textArea.getText());
        okButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PackagePanel.class, "ACS_LBL_okButton"));
        cancelButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PackagePanel.class, "ACS_LBL_cancelButton"));
        list.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PackagePanel.class, "ACSN_List"));
        list.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PackagePanel.class, "ACSD_List"));
    }

    /**
     * Updates label and enables/disables ok button.
     */
    private static void updateLabelEtcFromList(final JTextField field, final JList<String> list, final List<File> dirs, final JButton okButton) {
        final int idx = list.getSelectedIndex();
        if (idx == -1) {
            field.setText(" "); // NOI18N
            field.getAccessibleContext().setAccessibleName(" ");
            okButton.setEnabled(false);
        } else {
            final File dir = dirs.get(idx);
            field.setText(NbBundle.getMessage(PackagePanel.class, "LBL_dirWillBe", dir.getAbsolutePath()));
            field.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PackagePanel.class, "LBL_dirWillBe", dir.getAbsolutePath()));
            okButton.setEnabled(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    private JButton okButton;
    private JButton cancelButton;
    private JList<String> list;
    private JTextArea textArea;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
